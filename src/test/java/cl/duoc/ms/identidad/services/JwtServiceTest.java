
package cl.duoc.ms.identidad.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Pruebas unitarias de la generación de tokens JWT.
 *
 * Utiliza Mockito para simular el componente que firma
 * los tokens, sin necesitar MySQL ni iniciar Spring Boot.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private JwtService jwtService;

    /**
     * Comprueba que el JWT contiene los datos correctos,
     * utiliza HS256 y tiene una duración de 60 minutos.
     */
    @Test
    void generarTokenConDatosYExpiracionCorrectos() {

        // Simula la configuración de application.properties.
        ReflectionTestUtils.setField(
                jwtService, "expirationMinutes", 60L
        );

        // Simula el resultado del componente que firma el token.
        Jwt jwtSimulado = mock(Jwt.class);

        when(jwtSimulado.getTokenValue())
                .thenReturn("tokenFirmadoSimulado");

        when(jwtEncoder.encode(any(JwtEncoderParameters.class)))
                .thenReturn(jwtSimulado);

        // Ejecuta el método que estamos probando.
        String resultado = jwtService.generarToken(
                3,
                "cliente@oursour.cl",
                "ROLE_CLIENTE"
        );

        // Comprueba que devuelve el token generado.
        assertEquals("tokenFirmadoSimulado", resultado);

        // Captura los parámetros enviados al componente de firma.
        var captor = org.mockito.ArgumentCaptor.forClass(
                JwtEncoderParameters.class
        );

        verify(jwtEncoder).encode(captor.capture());

        var parametros = captor.getValue();
        var claims = parametros.getClaims();

        // Verifica los datos de identidad incluidos en el JWT.
        // Se consulta "iss" directamente porque no es una URL.
        assertEquals(
                "ms-identidad",
                claims.<String>getClaim("iss")
        );

        assertEquals(
                "cliente@oursour.cl",
                claims.getSubject()
        );

        assertEquals(
                Integer.valueOf(3),
                claims.<Integer>getClaim("idCuenta")
        );

        assertEquals(
                "ROLE_CLIENTE",
                claims.<String>getClaim("rol")
        );

        // Verifica que se utiliza el algoritmo HS256.
        assertEquals(
                "HS256",
                parametros.getJwsHeader().getAlgorithm().getName()
        );

        // Verifica que el token expira después de 60 minutos.
        Instant emision = claims.getIssuedAt();
        Instant expiracion = claims.getExpiresAt();

        assertNotNull(emision);
        assertNotNull(expiracion);

        assertEquals(
                Duration.ofMinutes(60),
                Duration.between(emision, expiracion)
        );
    }
}
