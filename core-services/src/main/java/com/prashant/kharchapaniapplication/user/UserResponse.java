package com.prashant.kharchapaniapplication.user;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter(AccessLevel.PROTECTED)
@Getter
public class UserResponse {
    private UUID id;

    private String email; //Using as Username

    private String firstName;

    private String lastName;

    private String phone;

    private Long budget;

    private LocalDateTime createdAt;

    private boolean profileComplete;
}
