package com.gradingsystem.tesla.controller;

import org.springframework.web.bind.annotation.*;

import com.gradingsystem.tesla.service.OTPService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OTPController {

    private final OTPService otpService;

    @PostMapping("/send")
    public String sendOTP(@RequestParam String email) {
        otpService.generateOTP(email);
        return "OTP sent to " + email;
    }

    @PostMapping("/verify")
    public String verifyOTP(@RequestParam String email, @RequestParam String otp) {
        boolean valid = otpService.verifyOTP(email, otp);
        return valid ? "OTP verified successfully" : "Invalid or expired OTP";
    }
}
