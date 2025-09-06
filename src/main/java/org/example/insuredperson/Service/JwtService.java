package org.example.insuredperson.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.insuredperson.Entity.InsuredPerson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String SECRET_KEY; // stored in application.properties or .env

    // Generate token with custom claims
    public String generateToken(InsuredPerson person) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", person.getUserId());
        claims.put("firstName", person.getFirstName());
        claims.put("lastName", person.getLastName());
        claims.put("policyNumber", person.getPolicyNumber());
        claims.put("age", person.getAge());
        claims.put("role", person.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(person.getPolicyNumber()) // sub = policyNumber
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5)) // 5 min expiry
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Validate token and extract policyNumber (subject)
    public String validateTokenAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject(); // policyNumber
        } catch (Exception e) {
            return null; // invalid/expired
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUserRole(String token) {
        return (String) extractClaim(token, claims -> claims.get("role"));
    }
}
