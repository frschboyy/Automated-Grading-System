package com.gradingsystem.tesla.service.notUsedService;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;

// import io.jsonwebtoken.Claims;
// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.SignatureAlgorithm;
// import io.jsonwebtoken.security.Keys;

// import java.util.Date;

// @Service
public class JwtService {
    // @Value("${app.jwt.secret}")
    // private String secret;

    // public String generateApprovalToken(Long studentId) {
    //     return Jwts.builder()
    //             .setSubject(studentId.toString())
    //             .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
    //             .setExpiration(new Date(System.currentTimeMillis() + 6000 * 60 * 60)) // 6 hr expiry
    //             .compact();
    // }

    // public Long validateApprovalToken(String token) {
    //     Claims claims = Jwts.parserBuilder()
    //             .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
    //             .build()
    //             .parseClaimsJws(token)
    //             .getBody();
    //     return Long.valueOf(claims.getSubject());
    // }
}
