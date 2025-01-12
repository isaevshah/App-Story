package kz.app.appstore.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret; // Должен быть Base64-encoded строкой

    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    // Генерация Access Token
    public String generateAccessToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CUSTOMER");
        return generateToken(userDetails.getUsername(), role, accessTokenExpiration);
    }

    // Генерация Refresh Token
    public String generateRefreshToken(UserDetails userDetails) {
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CUSTOMER");
        return generateToken(userDetails.getUsername(), role,  refreshTokenExpiration);
    }

    private String generateToken(String username, String role, long expiration) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Проверка токена
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Получение имени пользователя из токена
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
