package com.gradingsystem.tesla.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PendingRegistration {
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private String registrationId;
    private String Password;
    private Long institutionId;
    private VerificationMode verificationMode;
}
