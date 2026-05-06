package top.starwindv.WindForum.forum.Backend;


import java.security.Key;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import io.jsonwebtoken.JwtException;
import top.starwindv.WindForum.forum.Server.Forum;


import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.Base64;


public class JWT {
    private static final Key JWT_KEY = Jwts.SIG.HS256.key().build();
    private static final long EXPIRY_TIME = 2 * 60 * 60 * 1000;

    public static String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
            .subject(userId.toString())
            .issuer("forum-backend")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRY_TIME))
            .id(UUID.randomUUID().toString())
            .claim("username", username)
            .claim("role", role)
            .signWith(JWT_KEY)
            .compact();
    }

    public static Claims validateAndParseToken(String token) {
        try {
            return Jwts.parser()
                .verifyWith((SecretKey) JWT_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            Forum.Logger().trace(e);
        }
        return null;
    }

    public static Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public static String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public static String getRole(Claims claims) {
        return claims.get("role", String.class);
    }

    public static void test_main(String[] args) {
        String token = generateToken(1001L, "test_user", "normal");
        System.out.println("生成的JWT令牌：" + token);
        System.out.println("生成的JWTKEY ：" + Base64.getEncoder().encodeToString(JWT_KEY.getEncoded()));
        try {
            Claims claims = validateAndParseToken(token);
            System.out.println("解析结果：");
            if (claims != null) {
                System.out.println("用户ID：" + getUserId(claims));
            }
            if (claims != null) {
                System.out.println("用户名：" + getUsername(claims));
            }
            if (claims != null) {
                System.out.println("角色：" + getRole(claims));
            }
            if (claims != null) {
                System.out.println("过期时间：" + claims.getExpiration());
            }
        } catch (Exception e) {
            System.out.println("验证失败：" + e.getMessage());
        }
    }
}
