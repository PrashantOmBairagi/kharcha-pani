# Kharcha Pani — Backend API Documentation

Complete, code-verified reference for building the frontend client (Android / React Native / web)
against **both** backend services. Every method, path, status code, and JSON shape below was read
from the controllers, services, DTOs, and exception handlers — not guessed.

- **core-services** (auth, users, expenses, financial months): `http://localhost:8091`, prefix `/api/v1`
- **LenDen-services** (borrow/lend clients + transactions): `http://localhost:8092`, prefix `api/v2/lenden`
  (the `@RequestMapping` has no leading `/` — behaves identically)
- **Auth model**: JWT Bearer token issued by **core**. LenDen has NO login/register — log in via core
  and send the same `accessToken` to both services (both verify with the same `JWT_ACCESS_SECRET` env value).
  Header on every protected call: `Authorization: Bearer <accessToken>`.
- **Token lifetimes**: access token default **5 min**, tunable per deploy via `JWT_ACCESS_EXPIRATION`
  (300000 ms default; 5–30 min is the supported range). Refresh token default **10 days**
  (`JWT_REFRESH_EXPIRATION: 864000000`). The ONLY refresh endpoint is core's `POST /api/v1/auth/refresh`.
  Frontend must implement `401/403 → refresh → retry once`.
- **Conventions for this doc**:
  - `Auth` column: `PUBLIC` = no header needed, `JWT` = Bearer required.
  - `UUID` fields are JSON strings. `BigDecimal` money fields are JSON **numbers**. `LocalDate` is
    ISO `yyyy-MM-dd`. `LocalDateTime` (`createdAt`) is ISO date-time.
  - Error JSON keys are capitalized `Message` / `Status` — EXCEPT the cases called out per endpoint.

---

## 0. Service map (which backend owns what)

| Capability | Service | Base |
|---|---|---|
| Register / login / refresh tokens | core | `:8091/api/v1/auth` |
| Health / info (public) | core | `:8091/api/v1/public` |
| User profile + complete-profile | core | `:8091/api/v1/users` |
| Expenses CRUD + paged list | core | `:8091/api/v1/expenses` |
| Financial months, budgets, dashboard | core | `:8091/api/v1/fmonth` |
| Borrow/lend clients CRUD | LenDen | `:8092/api/v2/lenden/client` |
| Borrow/lend transactions CRUD | LenDen | `:8092/api/v2/lenden/transaction` |
| LenDen health (**auth required**, unlike core) | LenDen | `:8092/api/v2/lenden/client/health` |

There is NO `POST /api/v1/users` endpoint (older drafts of this doc listed one — it does not exist
in `UserController`). The user flow is `register → complete-profile → profile`.

---

## 1. Standard error shapes (core)

All core errors come from `GlobalExceptionHandler`. Frontend must branch on the key casing:

| Situation | HTTP | Body |
|---|---|---|
| Resource not found | 404 | `{ "Message": "...", "Status": 404 }` |
| Illegal argument / conflict (dup month, foreign month, bad `pageNo`, `Invalid month`) | 409 | `{ "Message": "...", "Status": 409 }` |
| Bean validation failure (`@Valid` on body) | 400 | `{ "Message": "<first violation message>", "Status": 400 }` — FIRST message only |
| Register duplicate email | 400 | `{ "message": "Email already exists! Try Login." }` — lowercase `message`, NOT standard shape |
| Unauthorized (bad/expired refresh token) | 401 | `{ "Message": "...", "Status": 401 }` |
| Bad/expired JWT (access token path) | 401 | `{ "Message": "Refresh token expired" }` or `{ "Message": "Invalid refresh token" }`, `Status: 401` |
| Phone already taken (complete-profile) | 409 | `{ "Message": "This phone number'...' is already linked to another account.", "Status": 409 }` |
| FMONTH_REQUIRED (expense in a month with no record) | 400 | `{ "Message": "Financial month required for 2025-7", "year": 2025, "month": 7, "code": "FMONTH_REQUIRED", "Status": 400 }` |

Shapes NOT covered by the handler (frontend sees Spring's default
`{timestamp,status,error,path}` instead of `{Message,Status}`):
bad-UUID path params, unknown enum values (e.g. bad `category`), missing `by-date` query params,
and `monthlyIncome: null` on month create (DB non-null → 500 via unhandled path). Validate these
client-side (rules are listed per endpoint below).

## 1b. Standard error shapes (LenDen)

LenDen's handler covers 404 (`ResourceNotFound`), 409 (generic `IllegalArgumentException`), and 400
(first validation message) with the same capitalized `{Message,Status}` keys. Differences from core:

- There is NO `UnauthorizedException`/`JwtException` handler — 401s (e.g. bad `getCurrentUserId`)
  do NOT use `{Message,Status}`; handle any 401 generically (refresh-and-retry, then logout).
- The `FMONTH_REQUIRED` branch is copied into LenDen's handler but never thrown there — ignore it.
- `pageSize` is NOT capped server-side — keep it ≤ 50 client-side.
- `pageNo <= 0` hits the generic handler as **409**, not 400 — validate `pageNo >= 1` client-side.

---

## 2. Auth — `POST :8091/api/v1/auth/*` (PUBLIC)

Shared request type `AuthRequest`:

```json
{ "email": "user@example.com", "password": "secret123" }
```

Validation (both fields always validated): `email` required + valid email format
(`"Email is required."` / `"Enter valid Email address."`); `password` required, 6–50 chars
(`"Password is required."` / `"Password must be 6 or greater."`).

### `POST /api/v1/auth/register`

Creates the account (BCrypt-hashed password, `profileComplete: false`) and returns tokens.

- **Auth:** PUBLIC. **Request:** `AuthRequest`. **Response 200** — `AuthResponse`:
```json
{
  "accessToken": "<jwt-access, 5-30 min life>",
  "refreshToken": "<jwt-refresh, 10 day life>",
  "message": "Registration Successful",
  "profileComplete": false
}
```
- **Response 400 duplicate email** (lowercase key): `{ "message": "Email already exists! Try Login." }`
- Frontend: if `profileComplete == false`, route to the complete-profile screen. Persist BOTH tokens.

### `POST /api/v1/auth/login`

- **Auth:** PUBLIC. **Request:** `AuthRequest` (same shape).
- **Response 200** — same `AuthResponse` shape, `message: "Login Successful"`,
  `profileComplete: true/false` depending on the account.
- **Response 404 unknown email:** `{ "Message": "No account exists with this email. Try signing up.", "Status": 404 }`
- **Response 404 wrong password:** `{ "Message": "Invalid credentials", "Status": 404 }`
  (login failures are 404, NOT 401 — and the two messages differ, so don't show them verbatim if
  you want to avoid account-enumeration hints).

### `POST /api/v1/auth/refresh`

Mints a fresh access token. PUBLIC (no header). **The only refresh endpoint — LenDen has none.**

- **Request** `RefreshTokenRequest`: `{ "refreshToken": "<jwt-refresh>" }` (required, `@NotBlank`).
- **Response 200** `RefreshResponse`: `{ "accessToken": "<new-jwt-access>" }`
- **Response 401:** `{ "Message": "Refresh token expired", "Status": 401 }` (expired) or
  `{ "Message": "Invalid refresh token", "Status": 401 }` (malformed/wrong-secret).
- **Response 404:** `{ "Message": "User Not Found or Token is broken", "Status": 404 }`
  (user deleted since login).
- Frontend: on any API `401/403`, call this once with the stored refresh token, swap the access
  token, retry the original call once. If refresh itself fails → force logout.

---

## 3. Public — `GET :8091/api/v1/public/*` (PUBLIC)

### `GET /api/v1/public/health`
- **Auth:** PUBLIC. **Response 200:** plain body `"OK"` (JSON string). Use for uptime checks.

### `GET /api/v1/public/info`
- **Auth:** PUBLIC. **Response 200:**
```json
{
  "application": "KHARCHA PANI - India's new finance buddy",
  "version": "v3.0",
  "status": "DEV-ACTIVE",
  "timestamp": "Tue Oct 06 ...",
  "Author": "Prashant Bairagi",
  "Portfolio": "https://prashant-bairagi-portfolio.vercel.app"
}
```

---

## 4. Users — `:8091/api/v1/users` (JWT)

### `POST /api/v1/users/complete-profile`
Completes the profile after registration (sets names/phone/budget, flips `profileComplete` to `true`).

- **Auth:** JWT. **Response 200:** EMPTY body. Re-read `GET /users/profile` (or re-login) to see the result.
- **Request** `CompleteProfileRequest` — ALL fields are nullable at the validation layer,
  so the frontend MUST send real values (sending `{}` returns 200 but marks a blank profile complete):
```json
{
  "firstName": "Prashant",   // optional in code, but send it: 3-20 chars when present
  "lastName": "Bairagi",     // optional in code, max 20 chars when present
  "phone": "9876543210",     // optional in code, but must match ^[6-9][0-9]{9}$ when present
  "budget": 30000            // optional in code, integer >= 0 when present (Long, NOT decimal)
}
```
- **Response 409 phone taken:** `{ "Message": "This phone number'...' is already linked to another account.", "Status": 409 }`
- Flow: `register` (profileComplete=false) → this call → `profileComplete=true`.

### `GET /api/v1/users/profile`
- **Auth:** JWT. **Response 200** — `UserResponse` (never contains `password`):
```json
{
  "id": "<uuid>",
  "email": "user@example.com",
  "firstName": "Prashant",
  "lastName": "Bairagi",
  "phone": "9876543210",
  "budget": 30000,
  "createdAt": "2026-10-06T20:42:30.231715",
  "profileComplete": true
}
```
- Unfinished profiles return the same shape with `null` names/phone/budget and
  `profileComplete: false`. `budget` is an integer (`Long`).

---

## 5. Expenses — `:8091/api/v1/expenses` (JWT)

Shared type `ExpenseResponse`:
```json
{
  "id": "<uuid>",
  "description": "Chai + samosa",
  "amount": 45.00,
  "category": "FOOD",
  "expenseDate": "2026-10-06",
  "userId": "<uuid>"
}
```
Valid `category` values: `FOOD, TRAVEL, SHOPPING, BILLS, HEALTH, ENTERTAINMENT, EDUCATION, OTHER`
(anything else → unhandled Spring default error shape, NOT `{Message,Status}` — use a dropdown).

### `POST /api/v1/expenses` → 201
- **Request** `ExpenseRequest`:
```json
{
  "description": "Chai + samosa",  // optional, BUT keep <= 100 chars (no create-side cap; longer strings 500)
  "category": "FOOD",              // required, enum above
  "amount": 45.00,                 // required, positive number
  "expenseDate": "2026-10-06",     // required, yyyy-MM-dd, never in the future
  "financialMonthId": "<uuid>"     // optional
}
```
- Behavior: with `financialMonthId`, it must belong to the caller (else **409**). Without it, the
  server uses the month of `expenseDate`: current month → **auto-created** (budget = previous month's
  budget or `0`, `monthlyIncome = 0`); any other month without a record → **400 FMONTH_REQUIRED**
  `{Message,year,month,code,Status}` (see §1) — show the "Create this month?" dialog, then
  `POST /fmonth {budget, monthlyIncome, year, month}` with the returned year/month, then retry.
- **Response 201** — `ExpenseResponse`.

### `GET /api/v1/expenses` → 200 (custom 1-based page)
| param | default | rule |
|---|---|---|
| `pageNo` | `1` | 1-based; `0`/negative → 409, validate client-side |
| `pageSize` | `10` | server caps with `min(pageSize,50)` — silently capped, no error |
| `sortBy` | `expenseDate` | whitelist `expenseDate, amount, category, createdAt, updatedAt`; anything else falls back to `expenseDate` |
| `sortDir` | `desc` | `asc` (any case) → ASC, everything else → DESC |
| `financialMonthId` | — | optional UUID filter for one month |
```json
{
  "expenses": [ { "...ExpenseResponse..." } ],
  "currentPage": 1,
  "totalPages": 5,
  "totalElements": 43,
  "hasNext": true,
  "hasPrevious": false
}
```

### `GET /api/v1/expenses/{id}` → 200 | 404 `{Message:"Expense not found",Status:404}`
Foreign ids also 404 (ownership-checked, safe to treat as "not found").

### `PUT /api/v1/expenses/{id}` → 200 plain string (NOT an object)
- **Request** `ExpenseUpdateRequest` — same rules as create EXCEPT: `description` capped at 100
  chars, and `financialMonthId` is NOT accepted (unknown-property → 400):
```json
{ "description": "Updated chai", "category": "FOOD", "amount": 50.00, "expenseDate": "2026-10-06" }
```
- **Response 200 body:** `"Expense Updated Successfully"` (a JSON string). **404** if missing/foreign.
- Note: updating `expenseDate` across months does NOT move the expense to another month — keep edits
  within the same month or delete + recreate.

### `DELETE /api/v1/expenses/{id}` → 200 plain string `"Expense deleted"` | 404.

---

## 6. Financial months — `:8091/api/v1/fmonth` (JWT)

Shared type `FinancialMonthSummaryResponse`:
```json
{
  "id": "<uuid>",
  "year": 2026,
  "month": 10,
  "budget": 30000.00,
  "monthlyIncome": 50000.00,
  "totalSpent": 4520.00,
  "remaining": 25480.00,
  "expenseCount": 12,
  "lastExpenseDate": "2026-10-06"
}
```
`remaining = budget − totalSpent` and CAN be negative (over budget — render it, don't clamp).

### `POST /api/v1/fmonth` → 201 (manual month create)
```json
{
  "budget": 30000.00,        // required, >= 0
  "monthlyIncome": 50000.00,  // REQUIRED IN PRACTICE: DTO says optional, but the DB column is
                             // NOT NULL — omitting it 500s. Always send a number (0 if none).
  "year": 2026,               // required, 2000-2050
  "month": 10                 // required, 1-12
}
```
- **Response 201** — fresh `FinancialMonthSummaryResponse` (all totals zero).
- **Response 409** dup: `{ "Message": "Financial month for 2026-10 already exists", "Status": 409 }`.

### `PATCH /api/v1/fmonth/{id}/budget` → 200
- **Request:** `{ "budget": 35000.00 }` (required, >= 0 — only the budget is editable here).
- **Response 200** — updated summary. **Missing/foreign id → 409**
  `{ "Message": "Financial month not found: <id>", "Status": 409 }` (note: 409, not 404).

### `GET /api/v1/fmonth/current` → 200 | 404
Current calendar month's summary. **404** `{ "Message": "Financial month not found for 2026-10", "Status": 404 }`
means "no data yet" — render the zero state, and note this GET never auto-creates (only `POST /expenses` does).

### `GET /api/v1/fmonth/by-date?year=2026&month=8` → 200 | 404 | 409
- `year` + `month` are REQUIRED ints. `month` outside 1–12 → **409** `{ "Message": "Invalid month: 13", "Status": 409 }`.
- Month missing → **404**. (Omitting a param yields Spring's default error shape, not `{Message,Status}` —
  always send both.)

### `GET /api/v1/fmonth/list?pageNo=1&pageSize=10` → 200 raw Spring `Page`
Newest first (year desc, month desc). `pageSize` capped at 50 server-side. Unlike the expenses page,
this is a RAW Spring Data page — pagination fields are **0-based** (`number`, `pageable.pageNumber`):
```json
{
  "content": [ { "...FinancialMonthSummaryResponse..." } ],
  "totalElements": 3, "totalPages": 1, "number": 0, "size": 10,
  "first": true, "last": true, "numberOfElements": 3, "empty": false,
  "pageable": { "pageNumber": 0, "pageSize": 10, "offset": 0, "paged": true, "unpaged": false,
                "sort": { "sorted": true, "unsorted": false, "empty": false } }
}
```

### `GET /api/v1/fmonth/{id}/expenses?pageNo=1&pageSize=10` → 200 raw Spring `Page<ExpenseResponse>`
Same raw-page shape with `content` = `ExpenseResponse[]`, sorted `expenseDate` desc.
Missing/foreign month → **409**.

### `GET /api/v1/fmonth/{id}/detail?pageNo=1&pageSize=10` → 200 dashboard payload
```json
{
  "summary": { "...FinancialMonthSummaryResponse..." },
  "categoryBreakdown": [
    { "category": "FOOD", "total": 2200.00, "percentage": 48.7 }
  ],
  "dailyTrend": [
    { "date": "2026-10-01", "total": 0.00 },
    { "date": "2026-10-06", "total": 820.00 }
  ],
  "recentExpenses": [ { "...ExpenseResponse..." } ]
}
```
- `categoryBreakdown`: sorted by `total` desc; `percentage` 1-decimal (`0.0` when nothing spent).
- `dailyTrend`: one entry per day, zero-filled; current month stops at TODAY (no future days).
- `recentExpenses`: `expenseDate` desc then `createdAt` desc, limited to **`pageSize` only —
  `pageNo` is IGNORED** (dead param, any value behaves the same).
- Missing/foreign month → **409**.

---

## 7. LenDen clients — `:8092/api/v2/lenden/client` (JWT, core token)

`Client` is returned as the raw entity (no DTO). `transactions` is always omitted (`@JsonIgnore`) —
call the transaction list per client for balances (there is no balance/summary endpoint; aggregate client-side).

```json
{
  "id": "<uuid>",
  "userId": "<uuid>",
  "clientFirstName": "Rahul",
  "clientLastName": "Sharma",
  "clientMobileNumber": "9876543210",
  "clientDescription": "College friend",
  "clientStatus": "ACTIVE",
  "clientAlertsActive": false,
  "nextSettlementDate": "2026-10-15"
}
```
`clientStatus` is always `ACTIVE` on create (enum also has `INACTIVE, DEFAULTER, INFORMAL, DELETED`
but nothing transitions it — treat as display-only). `clientAlertsActive` is always `false` on create
and not editable. No uniqueness on phone/name — duplicates allowed.

### `GET /api/v2/lenden/client/health` → 200 (JWT REQUIRED)
Unlike core's public health, this needs the Bearer token (no token → 403). Body `"OK"`.

### `POST /api/v2/lenden/client` → 200 (NOT 201)
```json
{
  "clientFirstName": "Rahul",        // 3-20 chars WHEN SENT (null passes validation — always send it)
  "clientLastName": "Sharma",        // max 20 chars when sent
  "clientMobileNumber": "9876543210",// must match ^[6-9][0-9]{9}$ when sent
  "clientDescription": "College friend", // max 200 chars when sent
  "nextSettlementDate": "2026-10-15" // free date, optional
}
```
- Validation annotations ignore `null` (no `@NotNull`), so send every field you want stored.
- **Response 200** — created `Client`.

### `GET /api/v2/lenden/client` → 200 `Client[]` (own clients only).

### `GET /api/v2/lenden/client/{clientId}` → 200 `Client` | 404 `{Message:"Client not found",Status:404}`
Foreign ids → 404. Note: raw `Client` return (not wrapped in ResponseEntity — same JSON either way).

### `GET /api/v2/lenden/client/{clientId}/summary` → 200 | 404
Per-client ledger balance, computed server-side in one aggregate query (no stored column, never drifts).
`SENT` = money I gave him, `RECEIVED` = money he gave back. `netAmount = |totalSent − totalReceived|`.
- **Response 200** — `ClientBalanceResponse`:
```json
{
  "clientId": "<uuid>",
  "totalSent": 5000.00,
  "totalReceived": 2000.00,
  "netAmount": 3000.00,
  "balanceStatus": "RECEIVE",
  "transactionCount": 3
}
```
- `balanceStatus`: `RECEIVE` = he owes me (`sent > received`), `GIVE` = I owe him
  (`received > sent`), `SETTLED` = even (including ledgers with zero transactions, all zeros).
- **Response 404** `{ "Message": "Client not found", "Status": 404 }` if missing/foreign.
- Frontend: render `netAmount` with the status tag ("You receive ₹3,000" / "You give ₹3,000" /
  "Settled") instead of summing transaction pages.

### `PATCH /api/v2/lenden/client/{clientId}` → 200 updated `Client` | 404
`ClientUpdateRequest` — every field optional; ONLY non-null fields are applied (partial update):
```json
{
  "clientFirstName": "Rahul",
  "clientLastName": null,
  "clientMobileNumber": null,
  "clientDescription": "Updated note",
  "nextSettlementDate": "2026-11-01"
}
```
Same length/pattern rules as create. `clientStatus`/`clientAlertsActive` cannot be changed here.

### `DELETE /api/v2/lenden/client` → 200 `"Client deleted"` | 404
Takes the **raw UUID as a JSON string in the request BODY** (no path id):
```json
"3fa85f64-5717-4562-b3fc-2c963f66afa6"
```
Response is the plain string `"Client deleted"`. 404 if missing/foreign.
WARNING: deleting a client cascade-deletes ALL its transactions (no balance guard) — confirm in UI.

---

## 8. LenDen transactions — `:8092/api/v2/lenden/transaction` (JWT, core token)

Shared type `TransactionResponse`:
```json
{
  "id": "<uuid>",
  "userId": "<uuid>",
  "clientId": "<uuid>",
  "amount": 1500.00,
  "dateAndTime": "2026-10-06",
  "description": "Lent for books",
  "transactionType": "SENT"
}
```
`transactionType`: `RECEIVED, SENT`. `dateAndTime` is a `LocalDate` (date only, despite the name).
`TransactionRequest` has NO validation and the controller has no `@Valid` — the frontend MUST validate:
`amount` present (send positive), `dateAndTime` present, `transactionType` present. Nulls persist and
break later math.

### `POST /api/v2/lenden/transaction/{clientId}` → 201
`clientId` comes from the **PATH** — a `clientId` field inside the JSON body (if sent) is IGNORED:
```json
{ "amount": 1500.00, "dateAndTime": "2026-10-06", "description": "Lent for books", "transactionType": "SENT" }
```
Foreign/unknown `clientId` → **404** `{ "Message": "Client not found", "Status": 404 }`
(ownership-checked — writing into another user's client is impossible).

### `GET /api/v2/lenden/transaction/{transactionId}` → 200 | 404 `{Message:"Couldn't find Transaction!!",Status:404}`

### `PATCH /api/v2/lenden/transaction/{id}` → 200 | 404 (same message)
Same body shape as create. Only amount/date/description/type change — the client association is fixed.

### `DELETE /api/v2/lenden/transaction/{id}` → 204 (no usable body) | 404
Deletes via find-then-delete (ownership-checked; foreign ids → 404 `"Couldn't find Transaction!!"`).
The code sends status 204 with a `"Delete Success!!"` string, but HTTP forbids bodies on 204 —
**treat 204 as success and parse no body**.

### `GET /api/v2/lenden/transaction/all/{clientId}?pageNo=1&pageSize=10&sortBy=dateAndTime&sortDir=desc` → 200
| param | default | rule |
|---|---|---|
| `pageNo` | `1` | 1-based; `0`/negative → 409 |
| `pageSize` | `10` | NOT capped server-side — keep ≤ 50 |
| `sortBy` | `dateAndTime` | whitelist `dateAndTime, amount`, else `dateAndTime` |
| `sortDir` | `desc` | `asc` → ASC, else DESC |
```json
{
  "expenses": [ { "...TransactionResponse..." } ],
  "currentPage": 1, "totalPages": 3, "totalElements": 25,
  "hasNext": true, "hasPrevious": false
}
```
The list key is literally `"expenses"` (copy-paste from core) — parse it as the transaction list.

---

## 9. Auth wiring across services (read this once)

| Item | core `:8091` | LenDen `:8092` |
|---|---|---|
| Login / register / refresh | `POST /api/v1/auth/*` | none — reuse core token |
| Health | `GET /api/v1/public/health` PUBLIC | `GET /api/v2/lenden/client/health` JWT required |
| Swagger | `:8091/swagger-ui.html`, `/v3/api-docs` PUBLIC | same paths permitted (+ a dead `/Swagger/**` pattern — use lowercase) |
| JWT env | `JWT_ACCESS_SECRET` + `JWT_REFRESH_SECRET` (Base64, 32B+, no fallback — boot fails without them) | `JWT_ACCESS_SECRET` (same value as core's; has a dev fallback — still set it explicitly) |
| DB | `xpensetrackerdb` | `lendendb` (separate; `userId` is a logical link, no FK) |
| Error-page dispatch | permitted (real JSON errors) | permitted (same as core) |
| CORS | none configured | none configured — browser apps must proxy or the backend must add origins |

Canonical frontend auth flow:
1. `register`/`login` (core) → store `accessToken` (5–30 min) + `refreshToken` (10 d) + `profileComplete`.
2. If `!profileComplete` → `POST /users/complete-profile` with REAL values (all fields optional in code).
3. Call core `:8091` and LenDen `:8092` with `Authorization: Bearer <accessToken>`.
4. On `401/403` → `POST core/auth/refresh` → swap token → retry once → else logout.

---

## 10. Frontend screen map + key flows

| Screen / action | Endpoint(s) |
|---|---|
| Register / login | `POST core/auth/register`, `POST core/auth/login` |
| Token refresh (the ONLY one) | `POST core/auth/refresh` |
| Complete profile | `POST core/users/complete-profile` → `GET core/users/profile` |
| Dashboard (current month) | `GET core/fmonth/current` → `GET core/fmonth/{id}/detail` |
| Analytics (any month) | `GET core/fmonth/by-date?year&month` → `GET core/fmonth/{id}/detail` |
| Month history | `GET core/fmonth/list` (0-based page!) |
| Month's expenses | `GET core/fmonth/{id}/expenses` (0-based page!) |
| All expenses | `GET core/expenses` (1-based `currentPage`) |
| Create expense | `POST core/expenses` |
| Edit / delete expense | `PUT core/expenses/{id}` (string reply) / `DELETE core/expenses/{id}` (string reply) |
| Manual month / edit budget | `POST core/fmonth` / `PATCH core/fmonth/{id}/budget` |
| LenDen people list / add | `GET` / `POST lenden/client` |
| LenDen person detail / balance / edit / remove | `GET` / `GET lenden/client/{id}/summary` / `PATCH lenden/client/{id}` / `DELETE lenden/client` (UUID in body) |
| LenDen ledger / add entry | `GET lenden/transaction/all/{clientId}?…` / `POST lenden/transaction/{clientId}` |
| LenDen entry detail / edit / remove | `GET` / `PATCH` / `DELETE lenden/transaction/{id}` (204, no body) |

Key flows:
1. **First expense of the month** → `POST /expenses` auto-creates the month (inherits previous budget) → 201.
2. **Expense in another month with no record** → 400 `FMONTH_REQUIRED {year,month}` → "Create this month?" dialog → `POST /fmonth {budget, monthlyIncome, year, month}` → retry `POST /expenses`.
3. **Budget edit** → `GET /fmonth/current` (take `id`) → `PATCH /fmonth/{id}/budget`.
4. **404 on `/fmonth/current` or `/by-date`** = zero state, not an error screen.
5. **LenDen balances** = `GET lenden/client/{id}/summary` (`totalSent`, `totalReceived`,
   `netAmount`, `balanceStatus RECEIVE/GIVE/SETTLED`). Do NOT re-sum transaction pages client-side.

---

## 11. Gotcha checklist (things that will bite a generated client)

- Auth fields are `accessToken` + `refreshToken`, never `token`. Access lives minutes — refresh-and-retry.
- Error keys `Message`/`Status` (capital) — except register-dup (`message`) and the unhandled-shape cases in §1.
- Login failures are **404**, not 401. Month-not-found on budget/expense-month/detail routes is **409**, not 404.
- `PUT /expenses/{id}` and `DELETE /expenses/{id}` return bare JSON **strings**. LenDen `DELETE /client`
  wants a bare UUID **in the body**; LenDen `DELETE /transaction/{id}` returns **204 with no body**.
- Two pagination dialects: 1-based custom pages (`GET /expenses` → `currentPage`; LenDen `/all` →
  `currentPage` under the `expenses` key) vs 0-based raw Spring pages (`/fmonth/list`, `/fmonth/{id}/expenses` → `number`).
- `pageSize` capped at 50 core-side (silent) but UNCAPPED LenDen-side — clamp everywhere to 50.
- `GET /fmonth/{id}/detail` ignores `pageNo` (only `pageSize` = recent count).
- Always send `monthlyIncome` (use `0`) on `POST /fmonth`; keep expense `description` ≤ 100 chars.
- Always send real values to `complete-profile` (empty `{}` still returns 200 and locks the profile "complete").
- LenDen `TransactionRequest` is unvalidated server-side — validate amount/date/type client-side.
- LenDen `updateClient` only applies non-null fields; status/alerts are read-only.
- No CORS on either service — browser builds need a proxy or backend origins configured.
- Deleting a LenDen client deletes its transactions with no guard — confirm first.
