package com.prashant.kharchapaniapplication.user;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompleteProfileRequest {
    @Size(min = 3, max = 20,
            message = "First name must be between 3 and 20 characters.")
    private String firstName;

    @Size(max = 20,
            message = "Last name cannot exceed 20 characters.")
    private String lastName;

    @Column(unique = true)
    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid 10-digit Indian mobile number."
    )
    private String phone;

    @PositiveOrZero(message = "Budget cannot be negative.")
    private Long budget;
}
