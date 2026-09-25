package com.KharchaPani.LenDenMicroservice.client;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClientUpdateRequest {

    @Size(max = 20,
            message = "First name must be between 3 and 20 characters.")
    private String clientFirstName;

    @Size(max = 20,
            message = "Last name cannot exceed 20 characters.")
    private String clientLastName;

    @Pattern(
            regexp = "^[6-9][0-9]{9}$",
            message = "Enter a valid 10-digit Indian mobile number."
    )
    private String clientMobileNumber;

    @Size(max = 200,
            message = "Client Description cannot exceed 200 characters.")
    private String clientDescription;


    private LocalDate nextSettlementDate;
}
