package com.edunest.configuration;

import com.edunest.entity.Student;
import com.edunest.entity.Teacher;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtHelper {

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshTokenExpiration;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Teacher teacher) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("teacherId", teacher.getTeacherId());
        claims.put("tenantId", teacher.getTenantId());
        claims.put("roleId", teacher.getRoleId());
        claims.put("teacherName", teacher.getFirstName() + " " + teacher.getLastName());

        return Jwts.builder()
                .claims(claims)
                .subject(teacher.getEmail())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateRefreshToken(Teacher teacher) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("teacherId", teacher.getTeacherId());
        claims.put("tenantId", teacher.getTenantId());

        return Jwts.builder()
                .claims(claims)
                .subject(teacher.getEmail())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateStudentAccessToken(Student student) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("studentId", student.getStudentId());
        claims.put("tenantId", student.getTenantId());
        claims.put("userType", "STUDENT");

        return Jwts.builder()
                .claims(claims)
                .subject(student.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String generateStudentRefreshToken(Student student) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("studentId", student.getStudentId());
        claims.put("tenantId", student.getTenantId());
        claims.put("userType", "STUDENT");

        return Jwts.builder()
                .claims(claims)
                .subject(student.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Integer extractStudentId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("studentId", Integer.class);
    }

    public String renewSessionJwt(Teacher teacher, String refreshToken) {

        if (!validateRefreshToken(refreshToken, teacher)) {
            throw new RuntimeException("Invalid refresh token.");
        }
        return generateAccessToken(teacher);
    }

    public boolean validateRefreshToken(String refreshToken, Teacher teacher) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        Integer teacherId = claims.get("teacherId", Integer.class);

        return teacher.getTeacherId().equals(teacherId);
    }

    public Teacher parseToken(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Teacher teacher = new Teacher();
        teacher.setEmail(claims.getSubject());
        teacher.setTeacherId(claims.get("teacherId", Integer.class));
        teacher.setTenantId(claims.get("tenantId", Integer.class));
        teacher.setRoleId(claims.get("roleId", Integer.class));
        teacher.setTeacherName(claims.get("teacherName", String.class));

        return teacher;
    }

    public String cleanToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    public Integer extractTeacherId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("teacherId", Integer.class);
    }

    public Integer extractTenantId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("tenantId", Integer.class);
    }

}