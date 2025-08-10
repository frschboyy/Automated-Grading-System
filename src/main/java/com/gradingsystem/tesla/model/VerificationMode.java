package com.gradingsystem.tesla.model;

public enum VerificationMode {
    EMAIL_DOMAIN,   // strict email domain check
    INVITE_CODE,    // invite code required
    ADMIN_APPROVAL, // open signup, but admin must approve
    EMAIL_AND_CODE, // email domain AND invite code
    OPEN            // anyone can register freely
}