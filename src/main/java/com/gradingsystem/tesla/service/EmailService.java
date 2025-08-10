package com.gradingsystem.tesla.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Sending email
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        javaMailSender.send(mailMessage);
    }

    // Generate Email Content
    public void sendApprovalEmail(String name, String email, String password, String role) {
        String subject = role + " REGISTERED";
        String body = 
            "We wish to inform you that `" + name.toUpperCase() + "` has been registered with our platform:\n\n" +
            "Login Credentials:\n" +
            "Email: " + email + "\n" +
            "Password: " + password;
        sendEmail(email, subject, body);
    }

    public void sendRemovalEmail(String email) {
        String subject = "USER REMOVED";
        String body = "We wish to inform you that your account has been removed.";
        sendEmail(email, subject, body);
    }

    public void sendDenialEmail(String email) {
        String subject = "USER REGISTRATION DENIED";
        String body = "We wish to inform you that your registration has been denied.";
        sendEmail(email, subject, body);
    }
}
