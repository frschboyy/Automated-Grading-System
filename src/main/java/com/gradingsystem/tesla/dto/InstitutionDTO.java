package com.gradingsystem.tesla.dto;

import com.gradingsystem.tesla.model.VerificationMode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionDTO {
    private Long id;
    private String name;
    private String email;
    private String emailDomain;
    private String inviteCode;
    private VerificationMode VerificationMode;
}