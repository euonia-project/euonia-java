package com.euonia.sample.controller;

import com.euonia.core.ObjectId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${security.jwt.secret}")
    private String signingKey;

    @GetMapping("grant")
    public String grant() {
        var builder = Jwts.builder();
        builder.subject(ObjectId.newRandomId()).id(String.valueOf(ObjectId.newSnowflakeId()))
               .issuer("euonia.sample")
               .issuedAt(Date.from(Instant.now()))
               .expiration(Date.from(Instant.now().plusSeconds(3600)))
               .claim("name", "user");
        builder.claim("role", new String[]{"user", "admin", "SA"});

        builder.signWith(Keys.hmacShaKeyFor(signingKey.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        return builder.compact();
    }
}
