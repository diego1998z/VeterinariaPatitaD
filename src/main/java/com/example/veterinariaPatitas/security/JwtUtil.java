package com.example.veterinariaPatitas.security;

import io.jsonwebtoken.*;//importa la libreria jwt
import io.jsonwebtoken.security.Keys;//utilidad de claves seguras
import org.springframework.stereotype.Component;

import java.security.Key;//clave secreta de token
import java.util.Date;//creacion y expiracion

@Component
public class JwtUtil {

    // firma y verifica el token
    private final Key key = Keys.hmacShaKeyFor("CambiarPorUnaClaveMuyLargaYSeguraQueTengaAlMenos256bits!".getBytes());
    //tiempo de expiracion del token
    private final long expirationMs = 1000 * 60 * 60 * 4; // 4 horas

    public String generateToken(String username, String role) {//genera el token con la info del usuario y marca la hora
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()//inicializa el token con los datos del usuario y en cuando expira
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {//valida y genera excepciones si no es valido
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {//valida el usuario dentro del token
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {//el rol dentr del token
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Object r = claims.get("role");
        return r != null ? r.toString() : "";
    }
}

