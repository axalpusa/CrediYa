package co.com.pragma.api.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties props;

    public String generateToken(UUID idUSer,UUID idRol) {
        return io.jsonwebtoken.Jwts.builder()
                .setSubject(String.valueOf(idUSer))
                .claim("idRol", String.valueOf(idRol))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + props.expirationMs ()))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS256, props.secret ())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(props.secret ())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public UUID extractRoleId(String token) {
        return UUID.fromString(extractAllClaims(token).get("idRol", String.class));
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException ex) {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
}
