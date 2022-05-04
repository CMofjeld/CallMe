package com.callme.services.userservice.security;

import com.callme.services.userservice.model.AppUser;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class JWTProvider {
    @Value("${JWT_SECRET}")
    private String jwtSecret;
    @Value("${JWT_EXPIRE_MS}")
    private int jwtExpirationInMs;

    public String generateToken(AppUser appUser) {
        // Determine expiration time
        Date curTime = new Date();
        Date expireTime = new Date(curTime.getTime() + jwtExpirationInMs);

        // Build and return the JWT
        return Jwts.builder()
                .setSubject(appUser.getUsername())
                .setIssuedAt(curTime)
                .setExpiration(expireTime)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException se) {
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException mje) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException eje) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException uje) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException iae) {
            System.err.println("JWT string is empty");
        }
        return false;
    }

    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
}
