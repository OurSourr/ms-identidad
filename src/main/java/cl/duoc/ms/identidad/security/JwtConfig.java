
package cl.duoc.ms.identidad.security;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

/**
 * Configura los componentes criptográficos utilizados
 * para generar y validar tokens JWT.
 */
@Configuration
public class JwtConfig {

    /**
     * Obtiene la clave secreta desde la configuración externa.
     *
     * @param secret Clave codificada en Base64.
     * @return Clave criptográfica para HS256.
     */
    @Bean
    public SecretKey jwtSecretKey(
            @Value("${app.jwt.secret}") String secret) {

        byte[] keyBytes = Base64.getDecoder().decode(secret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "La clave JWT debe tener al menos 256 bits"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /**
     * Configura el componente encargado de firmar JWT.
     *
     * @param secretKey Clave secreta compartida.
     * @return Codificador JWT.
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey)
        );
    }

    /**
     * Configura la validación criptográfica de los JWT.
     *
     * @param secretKey Clave utilizada para verificar las firmas.
     * @return Decodificador y validador JWT.
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {
        return NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}
