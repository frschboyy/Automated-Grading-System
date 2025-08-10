package com.gradingsystem.tesla.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gradingsystem.tesla.model.Institution;
import com.gradingsystem.tesla.model.User;
import com.gradingsystem.tesla.repository.InstitutionRepository;
import com.gradingsystem.tesla.service.EmailService;
import com.gradingsystem.tesla.service.UserService;
import com.gradingsystem.tesla.util.PasswordGenerator;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final InstitutionRepository institutionRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public AdminController(InstitutionRepository institutionRepository,
                           UserService userService,
                           EmailService emailService) {
        this.institutionRepository = institutionRepository;
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("/manage-institutions")
    public String showInstitutionsPage() {
        return "manageInstitution";
    }

    @PostMapping("/register-institution")
    public String registerInstitution(@ModelAttribute Institution institution, Model model) {

        // check email validity
        if (institution.getEmail() == null || institution.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Institution email is required and cannot be empty.");
            return "manage-institutions";
        }

        institutionRepository.save(institution);

        String rawPassword = PasswordGenerator.generateAdminPassword();

        // Create Institutional Admin User
        User newUser = User.builder()
                .firstName(institution.getName())
                .email(institution.getEmail())
                .password(rawPassword)
                .verified(true)
                .approvedByAdmin(true)
                .role("INSTITUTION_ADMIN")
                .institution(institution)
                .build();

        userService.saveUser(newUser);

        emailService.sendApprovalEmail(newUser.getFirstName(), newUser.getEmail(), rawPassword, newUser.getRole());

        return "redirect:/admin/manage-institutions?success=true"; 
    }
}
