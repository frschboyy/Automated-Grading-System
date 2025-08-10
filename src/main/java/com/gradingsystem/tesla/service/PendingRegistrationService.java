package com.gradingsystem.tesla.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.gradingsystem.tesla.model.PendingRegistration;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PendingRegistrationService {

    private final RedisTemplate<String, PendingRegistration> redisTemplate;

    private static final String PREFIX = "pending:";

    public void add(PendingRegistration registration) {
        String key = PREFIX + registration.getEmail();
        redisTemplate.opsForValue().set(key, registration, 5, TimeUnit.MINUTES);
    }

    public PendingRegistration get(String email) {
        return redisTemplate.opsForValue().get(PREFIX + email);
    }

    public void remove(String email) {
        redisTemplate.delete(PREFIX + email);
    }

    public boolean exists(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + email));
    }
}
