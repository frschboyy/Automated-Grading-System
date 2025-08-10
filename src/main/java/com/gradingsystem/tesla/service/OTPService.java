package com.gradingsystem.tesla.service;

import java.util.Random;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OTPService {
    
    @Value("${spring.mail.username}")
    private String emailSender;

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender javaMailSender;

    private static final long EXPIRATION_MINUTES = 5;

    public String generateOTP(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        redisTemplate.opsForValue().set(email, otp, EXPIRATION_MINUTES, TimeUnit.MINUTES);
        log.info("Generated OTP {} for email {} (expires in {} min)", otp, email, EXPIRATION_MINUTES);

        sendOTP(email, otp);
        return otp;
    }

    public boolean verifyOTP(String email, String submittedOtp) {
        String storedOtp = redisTemplate.opsForValue().get(email);
        log.info("Verifying OTP for {} - submitted: {}, stored: {}", email, submittedOtp, storedOtp);

        if (storedOtp != null && storedOtp.equals(submittedOtp)) {
            redisTemplate.delete(email);
            log.info("OTP verified successfully for {}", email);
            return true;
        } else {
            log.warn("Invalid or expired OTP for {}", email);
            return false;
        }
    }

    private void sendOTP(String email, String otp) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailSender);
        mailMessage.setTo(email);
        mailMessage.setSubject("Your OTP Code");
        mailMessage.setText("Your OTP is: " + otp + "\nThis code expires in " + EXPIRATION_MINUTES + " minutes.");
        
        javaMailSender.send(mailMessage);
        log.info("Sent OTP email to {}", email);
    }
}
