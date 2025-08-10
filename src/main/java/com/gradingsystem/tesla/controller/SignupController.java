package com.gradingsystem.tesla.controller;

import com.gradingsystem.tesla.dto.UserDto;
import com.gradingsystem.tesla.model.Institution;
import com.gradingsystem.tesla.model.PendingRegistration;
import com.gradingsystem.tesla.model.User;
import com.gradingsystem.tesla.model.VerificationMode;
import com.gradingsystem.tesla.repository.InstitutionRepository;
import com.gradingsystem.tesla.service.OTPService;
import com.gradingsystem.tesla.service.PendingRegistrationService;
import com.gradingsystem.tesla.service.UserService;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class SignupController {

    private AuthenticationManager authenticationManager;
    private final UserService userService;
    private final OTPService otpService;
    private final InstitutionRepository institutionRepository;
    private final PendingRegistrationService pendingRegistrationService;

    private static final Logger log = LoggerFactory.getLogger(OTPService.class);

    @Autowired
    public SignupController(AuthenticationManager authenticationManager,
            UserService userService,
            OTPService otpService,
            InstitutionRepository institutionRepository,
            PendingRegistrationService pendingRegistrationService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.otpService = otpService;
        this.institutionRepository = institutionRepository;
        this.pendingRegistrationService = pendingRegistrationService;
    }

    @GetMapping("/signup")
    public String showSignupPage(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            return "redirect:/dashboard";
        }

        model.addAttribute("student", new UserDto());
        model.addAttribute("institutions", institutionRepository.findAll());
        return "signup";
    }

    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleSignup(
            @Valid @ModelAttribute("student") UserDto userDto,
            BindingResult bindingResult,
            @RequestParam(value = "inviteCode", required = false) String inviteCode) {

        log.info("Signup attempt for email: {}", userDto.getEmail());

        Map<String, Object> response = new HashMap<>();

        // Validation
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors for email {}: {}", userDto.getEmail(), bindingResult.getAllErrors());
            response.put("success", false);
            response.put("message", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return ResponseEntity.badRequest().body(response);
        }

        // Check if email already exists in User table
        if (userService.existsByEmail(userDto.getEmail())) {
            log.warn("Email already registered: {}", userDto.getEmail());
            response.put("success", false);
            response.put("message", "Email is already registered.");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if email already pending registration
        if (pendingRegistrationService.exists(userDto.getEmail())) {
            log.warn("Email already pending verification: {}", userDto.getEmail());
            response.put("success", false);
            response.put("message", "A registration for this email is already pending verification.");
            return ResponseEntity.badRequest().body(response);
        }

        // Lookup Institution
        Institution institution = institutionRepository.findById(userDto.getInstitutionId()).orElse(null);
        log.debug("Institution lookup for ID {} returned: {}", userDto.getInstitutionId(), institution);
        if (institution == null) {
            response.put("success", false);
            response.put("message", "Invalid institution");
            return ResponseEntity.badRequest().body(response);
        }

        // Handle institution verification logic
        VerificationMode mode = institution.getVerificationMode();
        log.info("Verification mode for institution {}: {}", institution.getName(), mode);

        switch (mode) {
            case EMAIL_DOMAIN:
                if (!userDto.getEmail().endsWith("@" + institution.getEmailDomain())) {
                    log.warn("Email {} does not match institution domain {}", userDto.getEmail(),
                            institution.getEmailDomain());
                    response.put("success", false);
                    response.put("message", "Email does not match institution domain");
                    return ResponseEntity.badRequest().body(response);
                }
                break;
            case INVITE_CODE:
                if (inviteCode == null || !inviteCode.equals(institution.getInviteCode())) {
                    log.warn("Invalid invite code {} for institution {}", inviteCode, institution.getName());
                    response.put("success", false);
                    response.put("message", "Invalid invite code");
                    return ResponseEntity.badRequest().body(response);
                }
                break;
            case EMAIL_AND_CODE:
                if (!userDto.getEmail().endsWith("@" + institution.getEmailDomain()) ||
                        inviteCode == null || !inviteCode.equals(institution.getInviteCode())) {
                    log.warn("Email domain or invite code invalid. Email: {}, InviteCode: {}", userDto.getEmail(),
                            inviteCode);
                    response.put("success", false);
                    response.put("message", "Email or invite code invalid");
                    return ResponseEntity.badRequest().body(response);
                }
                break;
            case ADMIN_APPROVAL:
                userDto.setApprovedByAdmin(false);
                break;
            case OPEN:
                break;
        }

        if (mode != VerificationMode.OPEN) {
            log.info("Creating pending registration for {}", userDto.getEmail());

            // Store pending registration
            PendingRegistration pending = new PendingRegistration(
                    userDto.getFirstName(),
                    userDto.getMiddleName(),
                    userDto.getLastName(),
                    userDto.getEmail(),
                    userDto.getRegistrationId(),
                    userDto.getPassword(),
                    userDto.getInstitutionId(),
                    mode);

            // Log pending object
            log.debug("PendingRegistration to save: {}", pending);

            // Save to Redis
            pendingRegistrationService.add(pending);

            // Check if saved successfully
            boolean exists = pendingRegistrationService.exists(userDto.getEmail());
            log.debug("PendingRegistration saved? {}", exists);

            PendingRegistration retrieved = pendingRegistrationService.get(userDto.getEmail());
            log.debug("Retrieved from Redis after save: {}", retrieved);
            otpService.generateOTP(userDto.getEmail());
            log.info("OTP generated and pending registration saved for {}", userDto.getEmail());

            response.put("success", true);
            response.put("requireOtp", true);
            response.put("message", "OTP sent to your email");
            return ResponseEntity.ok(response);
        }

        log.info("No OTP required for institution {}, completing registration immediately", institution.getName());

        // No OTP required, finish registration and login
        return finishRegistrationWithoutOtp(mapUserDtoToUser(userDto), institution);
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyOtp(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp) {
        log.info("OTP verification attempt for email: {}", email);

        Map<String, Object> response = new HashMap<>();

        PendingRegistration pending = pendingRegistrationService.get(email);
        log.debug("Pending registration lookup for {} returned: {}", email, pending);

        if (pending == null) {
            log.warn("No pending registration found for {}", email);
            response.put("success", false);
            response.put("message", "No pending registration for this email.");
            return ResponseEntity.badRequest().body(response);
        }

        if (!otpService.verifyOTP(email, otp)) {
            log.warn("Invalid or expired OTP for {}", email);
            response.put("success", false);
            response.put("message", "Invalid or expired OTP");
            return ResponseEntity.badRequest().body(response);
        }

        Institution institution = institutionRepository.findById(pending.getInstitutionId()).orElseThrow();
        log.info("Pending registration email {} belongs to institution {}", pending.getEmail(), institution.getName());

        User newUser = User.builder()
                .firstName(pending.getFirstName())
                .middleName(pending.getMiddleName())
                .lastName(pending.getLastName())
                .registrationId(pending.getRegistrationId())
                .email(pending.getEmail())
                .password(pending.getPassword())
                .institution(institution)
                .role("STUDENT")
                .verified(true)
                .approvedByAdmin(pending.getVerificationMode() != VerificationMode.ADMIN_APPROVAL)
                .build();

        try {
            User savedUser = userService.saveUser(newUser);
            log.info("New user {} saved successfully", savedUser.getEmail());

            pendingRegistrationService.remove(email);
            log.info("New user {} saved successfully", savedUser.getEmail());

            if (savedUser.isApprovedByAdmin()) {
                response.put("success", true);
                response.put("redirect", "/dashboard");
                return ResponseEntity.ok(response);
            }

            response.put("success", true);
            response.put("pendingApproval", true);
            response.put("message", "Waiting for admin approval");
            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            log.error("DataIntegrityViolationException while saving user {}: {}", email, e.getMessage());
            response.put("success", false);
            response.put("message", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> finishRegistrationWithoutOtp(User student, Institution institution) {
        Map<String, Object> response = new HashMap<>();
        try {
            student.setInstitution(institution);
            student.setRole("STUDENT");
            student.setVerified(true);
            student.setApprovedByAdmin(true);

            userService.saveUser(student);

            // Auto-login using Spring Security
            autoLogin(student.getEmail(), student.getPassword());

            response.put("success", true);
            response.put("redirect", "/dashboard");
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            response.put("success", false);
            response.put("message", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void autoLogin(String email, String password) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, password);
        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User mapUserDtoToUser(UserDto dto) {
        return User.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .verified(dto.getVerified() != null ? dto.getVerified() : false)
                .approvedByAdmin(dto.getApprovedByAdmin() != null ? dto.getApprovedByAdmin() : false)
                .role(dto.getRole())
                .build();
    }
}
