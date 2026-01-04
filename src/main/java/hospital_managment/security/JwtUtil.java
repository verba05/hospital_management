package hospital_managment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET_KEY = "HgkdOihm592NbpwhQpumGjKLGJHdflkgnsdkGGkglksjgjkldfjfsdoiiugsehGkjdfg9oHIGU";
    private static final long EXPIRATION_TIME = 8 * 60 * 60 * 1000;
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public static String generateToken(int userId, String role) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY)
                .compact();
    }

    public static Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return Integer.parseInt(claims.getSubject());
        }
        return null;
    }

    public static String getRoleFromToken(String token) {
        Claims claims = validateToken(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }
}
