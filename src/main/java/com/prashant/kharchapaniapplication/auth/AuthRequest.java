package com.prashant.kharchapaniapplication.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

        @NotBlank(message = "Email is required.")
        @Email(message = "Enter valid Email address.")
        private String email;

        @NotBlank(message = "Password is required.")
        @Size(min = 6, max = 50,
                message = "Password must be 6 or greater.")
        private String password;

}
