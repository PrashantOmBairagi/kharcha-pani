# Kharcha Pani — Backend API Documentation

Complete reference for building the frontend (Android / React Native client) against the backend.

- **Base URL (local)**: `http://localhost:8080`
- **API prefix**: `/api/v1`
- **Auth**: JWT Bearer token. All routes under `/api/v1/users`, `/api/v1/expenses`, `/api/v1/fmonth` require `Authorization: Bearer <token>`. `/api/v1/auth/*` and `/api/v1/public/*` are public.
- **Pagination convention**: 1‑based `pageNo` (default `1`) and `pageSize` (default `10`, **max `50`**, server caps silently).
- **Money fields**: `BigDecimal` → serialized as JSON **numbers** (e.g. `50000.00` appears as `50000.00`).
- **Dates**: `LocalDate` → ISO `yyyy-MM-dd`. `createdAt` → ISO date-time.
- **IDs**: `UUID` → strings.
- **Enums**: uppercase strings (`FOOD`, `TRAVEL`, ...).

---

## 1. Standard error shapes

All errors come from `GlobalExceptionHandler`. Two key things:

1. **JSON keys are capitalized**: `Message` and `Status` (not `message`/`status`).
2. The one exception is **register duplicate-email**, which returns a lowercase `message` (see Auth).

### 404 — Resource not found
```json
{ "Message": "Expense not found", "Status": 404 }
```

### 409 — Conflict / illegal argument (generic)
```json
{ "Message": "Financial month for 2026-9 already exists", "Status": 409 }
```

### 400 — Bean validation failure (request body failed `@Valid`)
```json
{ "Message": "Amount must be positive.", "Status": 400 }
```
`Message` is the **first** validation error message.

### 400 — FMONTH_REQUIRED (special)
Thrown when an expense's date is NOT the current month and there is no financial month for it.
```json
{
  "Message": "Financial month required for 2025-7",
  "year": 2025,
  "month": 7,
  "code": "FMONTH_REQUIRED",
  "Status": 400
}
```
This is the signal for the mobile app to show "Create financial month?" then retry.

---

## 2. Auth (public) — `/api/v1/auth`

### POST `/api/v1/auth/register`
Create account and get a token.

**Request**
```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```
Validation:
- `email`: required, valid email
- `password`: required, 6–50 characters

**Response 200**
```json
{
  "token": "<jwt>",
  "message": "...",
  "profileComplete": false
}
```
`profileComplete` tells the client whether to route to the profile-completion flow.

**Response 400 — email already exists** (note: lowercase `message`, NOT standard shape)
```json
{ "message": "Email already exists! Try Login." }
```

### POST `/api/v1/auth/login`
**Request** — same `AuthRequest` body as register (`email`, `password`).

**Response 200**
```json
{
  "token": "<jwt>",
  "message": "...",
  "profileComplete": boolean
}
```

**Response 404 — bad credentials**
```json
{ "Message": "Invalid credentials", "Status": 404 }
```
Login failure is a **404**, not 401.

---

## 3. Public — `/api/v1/public`

### GET `/api/v1/public/health`
- No auth.
- **Response 200**: plain body `"OK"`.

### GET `/api/v1/public/info`
- No auth.
- **Response 200**:
```json
{
  "application": "Smart Finance Tracker",
  "version": "2.0",
  "status": "ACTIVE",
  "timestamp": "Tue Sep 02 ...",
  "Author": "Prashant Bairagi"
}
```

---

## 4. User — `/api/v1/users` (auth required)

### POST `/api/v1/users/complete-profile`
Complete the profile after registration (sets first/last name, phone, budget; marks `profileComplete = true`).

**Request**
```json
{
  "firstName": "Prashant",
  "lastName": "Bairagi",
  "phone": "9876543210",
  "budget": 30000
}
```
Validation:
- `firstName`: 3–20 characters
- `lastName`: max 20 characters
- `phone`: `^[6-9][0-9]{9}$` (10-digit Indian mobile)
- `budget`: `Long`, must be >= 0

**Response 200**: empty body. (No response payload — check `profileComplete` from a subsequent `GET /users/profile` or by another login.)

### GET `/api/v1/users/profile`
Fetch the current user's profile.

**Response 200** — `UserResponse`:
```json
{
  "id": "<uuid>",
  "email": "user@example.com",
  "firstName": "Prashant",
  "lastName": "Bairagi",
  "phone": "9876543210",
  "budget": 30000,
  "createdAt": "2026-09-02T12:00:00",
  "profileComplete": true
}
```
> ✅ Password leak fixed (2026-09-02): endpoint now returns `UserResponse`, NOT the raw `User` entity — `password` is never serialized.

### POST `/api/v1/users` (create raw user)
- Auth required.
- Body: a raw `User` entity (see `GET /profile` shape). Echoes back a `UserResponse` (same shape as `/profile` — no `password`) with HTTP 200.
- **Do not use from the app** — exists for admin/backend use. `register` + `complete-profile` is the app flow.

---

## 5. Expenses — `/api/v1/expenses` (auth required)

`ExpenseResponse` (shared):
```json
{
  "id": "<uuid>",
  "description": "Chai + samosa",
  "amount": 45.00,
  "category": "FOOD",
  "expenseDate": "2026-09-02",
  "userId": "<uuid>"
}
```

### POST `/api/v1/expenses`
Create an expense.

**Request**
```json
{
  "description": "Chai + samosa",
  "category": "FOOD",
  "amount": 45.00,
  "expenseDate": "2026-09-02",
  "financialMonthId": "<uuid>"   // optional
}
```
Validation:
- `description`: optional, max 100 chars (write path)
- `category`: required, one of `FOOD TRAVEL SHOPPING BILLS HEALTH ENTERTAINMENT EDUCATION OTHER`
- `amount`: required, positive
- `expenseDate`: required, cannot be in the future (`@PastOrPresent`)
- `financialMonthId`: optional

Behavior:
- If `financialMonthId` given, it must belong to the current user (else 409).
- If omitted, the server **auto-creates** a financial month for the **current** month (budget inherits previous month's budget, `monthlyIncome = 0`).
- If the expense date is a different month → **400 FMONTH_REQUIRED** (see §1). This is the trigger for the "Start a new month" dialog.

**Response 201** — `ExpenseResponse`.

### GET `/api/v1/expenses`
List expenses (paginated, sortable, filterable by month).

**Query params**
| param | default | allowed |
|---|---|---|
| `pageNo` | `1` | 1-based |
| `pageSize` | `10` | max 50 |
| `sortBy` | `expenseDate` | `expenseDate`, `amount`, `category`, `createdAt`, `updatedAt` (anything else falls back to `expenseDate`) |
| `sortDir` | `desc` | `asc`, `desc` |
| `financialMonthId` | — (optional) | filters to one month |

**Response 200** — custom page (NOT a Spring Page):
```json
{
  "expenses": [ ...ExpenseResponse ],
  "currentPage": 1,
  "totalPages": 5,
  "totalElements": 43,
  "hasNext": true,
  "hasPrevious": false
}
```

### GET `/api/v1/expenses/{id}`
Fetch one expense.
- **Response 200** — `ExpenseResponse`.
- **Response 404** — `{ "Message": "Expense not found", "Status": 404 }`.

### PUT `/api/v1/expenses/{id}`
Update an expense. **Method is PUT.**

**Request** — `ExpenseUpdateRequest` (note: does NOT accept `financialMonthId`; sending it will 400 because no unknown-property tolerance)
```json
{
  "description": "Updated chai",
  "category": "FOOD",
  "amount": 50.00,
  "expenseDate": "2026-09-02"
}
```
Validation: `description` max 100; `amount` required positive; `category` required; `expenseDate` required, not future.

**Response 200** — plain JSON string:
```json
"Expense Updated Successfully"
```
**Response 404** if not found / not owned.

### DELETE `/api/v1/expenses/{id}`
Delete an expense.
- **Response 200** — plain JSON string:
```json
"Expense deleted"
```
- **Response 404** if not found / not owned.

---

## 6. Financial Month — `/api/v1/fmonth` (auth required)

`FinancialMonthSummaryResponse` (shared):
```json
{
  "id": "<uuid>",
  "year": 2026,
  "month": 9,
  "budget": 30000.00,
  "monthlyIncome": 50000.00,
  "totalSpent": 4520.00,
  "remaining": 25480.00,
  "expenseCount": 12,
  "lastExpenseDate": "2026-09-02"
}
```
> `remaining = budget − totalSpent` — can be **negative** when over budget (client must not clamp blindly if it wants to show "over budget").

### POST `/api/v1/fmonth`
Create a financial month manually.

**Request**
```json
{
  "budget": 30000.00,
  "monthlyIncome": 50000.00,
  "year": 2026,
  "month": 9
}
```
Validation:
- `budget`: required, >= 0
- `monthlyIncome`: optional, >= 0
- `year`: required, 2000–2050
- `month`: required, 1–12

**Response 201** — `FinancialMonthSummaryResponse` (fresh summary).
**Response 409** — if that `year`+`month` already exists for this user:
```json
{ "Message": "Financial month for 2026-9 already exists", "Status": 409 }
```

### PATCH `/api/v1/fmonth/{id}/budget`
Update just the budget.

**Request**
```json
{ "budget": 35000.00 }
```
Validation: `budget` required, >= 0.

**Response 200** — `FinancialMonthSummaryResponse` (updated).
**Response 409** — if month not found / not owned:
```json
{ "Message": "Financial month not found: <id>", "Status": 409 }
```

### GET `/api/v1/fmonth/current`
Current month's summary.
- **Response 200** — `FinancialMonthSummaryResponse`.
- **Response 404** — no current month yet: `{ "Message": "Financial month not found for 2026-9", "Status": 404 }` (read-only; GET never auto-creates).

### GET `/api/v1/fmonth/by-date`
Summary for a specific year/month.
```
GET /api/v1/fmonth/by-date?year=2026&month=8
```
- **Response 200** — `FinancialMonthSummaryResponse`.
- **Response 404** — month missing: `{ "Message": "Financial month not found for 2026-8", "Status": 404 }`.
- **Response 409** — `month` outside 1–12: `{ "Message": "Invalid month: 13", "Status": 409 }`.

### GET `/api/v1/fmonth/list`
Paginated history of all months for the user (newest first: year desc, then month desc).

**Query params**: `pageNo` (default 1), `pageSize` (default 10, max 50).

**Response 200** — **raw Spring Data `Page`**:
```json
{
  "content": [ ...FinancialMonthSummaryResponse ],
  "pageable": {
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 3,
  "totalPages": 1,
  "last": true,
  "first": true,
  "sort": { "sorted": true, "unsorted": false, "empty": false },
  "number": 0,
  "size": 10,
  "numberOfElements": 3,
  "empty": false
}
```
> ⚠️ Pagination fields are **0-based** here (`number`, `pageable.pageNumber`) — different from the custom expenses page which is 1-based (`currentPage`).

### GET `/api/v1/fmonth/{id}/expenses`
Paginated expenses belonging to one month.

**Query params**: `pageNo`, `pageSize` (same defaults/cap).

**Response 200** — **raw Spring Data `Page`** with `content` = array of `ExpenseResponse` (same shape as above). Sorted `expenseDate` desc.
**Response 409** — month not found / not owned.

### GET `/api/v1/fmonth/{id}/detail`
Full dashboard payload for one month (summary + breakdowns + recent expenses).

**Query params**: `pageNo`, `pageSize` (same defaults/cap) — `pageSize` controls how many `recentExpenses` come back.

**Response 200** — `FinancialMonthDetailResponse`:
```json
{
  "summary": { ...FinancialMonthSummaryResponse },
  "categoryBreakdown": [
    { "category": "FOOD", "total": 2200.00, "percentage": 48.7 },
    { "category": "TRAVEL", "total": 1500.00, "percentage": 33.2 }
  ],
  "dailyTrend": [
    { "date": "2026-09-01", "total": 0.00 },
    { "date": "2026-09-02", "total": 820.00 }
  ],
  "recentExpenses": [ ...ExpenseResponse ]
}
```
- `categoryBreakdown`: sorted by `total` desc; `percentage` rounded to 1 decimal; `0.0` when nothing spent.
- `dailyTrend`: one entry for **every day** of the month — zero-filled (`0.00`) for days with no expenses; for the current month it stops at **today** (does not include future days).
- `recentExpenses`: sorted `expenseDate` desc then `createdAt` desc, limited to `pageSize`.

**Response 409** — month not found / not owned.

---

## 7. Frontend cheat-sheet (how the screens map to endpoints)

| Screen / action | Endpoint(s) |
|---|---|
| Login / Register | `POST /api/v1/auth/login`, `POST /api/v1/auth/register` |
| Complete profile | `POST /api/v1/users/complete-profile` |
| Load user profile | `GET /api/v1/users/profile` |
| Dashboard current month | `GET /api/v1/fmonth/current` + `GET /api/v1/fmonth/{id}/detail` |
| Analytics (any month) | `GET /api/v1/fmonth/by-date?year&month` + `GET /api/v1/fmonth/{id}/detail` |
| Month history list | `GET /api/v1/fmonth/list` |
| Month's expenses | `GET /api/v1/fmonth/{id}/expenses` |
| All-expenses screen | `GET /api/v1/expenses` |
| Create expense | `POST /api/v1/expenses` |
| Edit expense | `PUT /api/v1/expenses/{id}` |
| Delete expense | `DELETE /api/v1/expenses/{id}` |
| Create month manually | `POST /api/v1/fmonth` |
| Edit budget | `PATCH /api/v1/fmonth/{id}/budget` |

### Key flows
1. **Expense on current month, no month exists yet** → `POST /expenses` auto-creates the month (budget inherited from previous month) and returns 201.
2. **Expense on a different month, no month exists** → 400 `FMONTH_REQUIRED` with `year`/`month` → show "Create this month?" dialog → `POST /fmonth` → retry `POST /expenses`.
3. **Current-month budget edit** → `GET /fmonth/current` (get id) → `PATCH /fmonth/{id}/budget`.
4. **404 on `/fmonth/current` or `/by-date`** = month doesn't exist → treat as zero state (no data), never as a hard error.

---

## 8. Gotchas & quirks to remember

- Error keys are `Message` / `Status` (capitalized) — **except** register duplicate email (`message`, lowercase).
- Login failure = **404**, not 401.
- `PUT /expenses/{id}` and `DELETE /expenses/{id}` return plain JSON **strings**, not objects.
- Custom expense page uses **1-based** `currentPage`; Spring `Page` endpoints use **0-based** `number`/`pageable.pageNumber`.
- `remaining` can be negative (over budget).
- `GET /users/profile` and `POST /users` return `UserResponse` (no `password`) — a serialization-safety fix; frontend's `User` type should drop any `password`/`username` expectations.
- Valid expense categories: `FOOD, TRAVEL, SHOPPING, BILLS, HEALTH, ENTERTAINMENT, EDUCATION, OTHER`.
- Monthly auto-created budgets inherit the **previous month's budget** (or `0` if none), `monthlyIncome = 0`.