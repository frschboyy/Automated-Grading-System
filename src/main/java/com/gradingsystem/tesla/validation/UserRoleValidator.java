package com.gradingsystem.tesla.validation;

import com.gradingsystem.tesla.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserRoleValidator {

    public void validate(User user) {
        if (user == null || user.getRole() == null) {
            throw new IllegalArgumentException("User or role cannot be null");
        }

        switch (user.getRole()) {
            case "STUDENT":
            case "TEACHER":
                if (user.getRegistrationId() == null || user.getRegistrationId().isBlank()) {
                    throw new IllegalArgumentException("Registration ID is required for STUDENT and TEACHER");
                }
                if (user.getInstitution() == null) {
                    throw new IllegalArgumentException("Institution is required for STUDENT and TEACHER");
                }
                break;

            case "INSTITUTION_ADMIN":
                if (user.getInstitution() == null) {
                    throw new IllegalArgumentException("Institution is required for INSTITUTION_ADMIN");
                }
                break;

            case "ADMIN":
                break;
        }
    }
}
