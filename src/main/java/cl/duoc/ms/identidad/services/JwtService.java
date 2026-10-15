
package cl.duoc.ms.identidad.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Servicio responsable de generar tokens JWT
 * para los usuarios autenticados.
 */
@Service
public class JwtService {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    /**
     * Genera un token JWT firmado para un usuario autenticado.
     *
     * @param idCuenta identificador del usuario.
     * @param correo correo electrónico del usuario.
     * @param rol rol asignado al usuario.
     * @return token JWT firmado.
     */
    public String generarToken(Integer idCuenta, String correo, String rol) {

        Instant ahora = Instant.now();

        // Información que contendrá el token JWT.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ms-identidad")
                .issuedAt(ahora)
                .expiresAt(ahora.plus(expirationMinutes, ChronoUnit.MINUTES))
                .subject(correo)
                .claim("idCuenta", idCuenta)
                .claim("rol", rol)
                .build();

        // Especifica HS256 como algoritmo de firma del JWT.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .build();

        // Firma el token utilizando la clave configurada en JwtConfig.
        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }
}
