
package cl.duoc.ms.identidad.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import cl.duoc.ms.identidad.entity.Cuenta;
import cl.duoc.ms.identidad.entity.Rol;
import cl.duoc.ms.identidad.services.CuentaSeguridadService;

/**
 * Pruebas unitarias del convertidor de JWT.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationConverterTest {

    @Mock
    private CuentaSeguridadService cuentaSeguridadService;

    @InjectMocks
    private SecurityConfig securityConfig;

    /**
     * Rechaza un JWT cuando el rol almacenado
     * ya no coincide con el rol vigente.
     */
    @Test
    void rechazarTokenCuandoElRolHaCambiado() {

        // Rol actual de la cuenta.
        Rol rol = new Rol();
        rol.setNombre("ROLE_CLIENTE");

        Cuenta cuenta = new Cuenta(
                "usuario@oursour.cl",
                "hashSimulado",
                rol
        );

        // Simula la consulta del usuario vigente.
        when(cuentaSeguridadService.obtenerCuentaActiva(3))
                .thenReturn(cuenta);

        // JWT emitido anteriormente con rol administrador.
        Instant ahora = Instant.now();

        Jwt token = Jwt.withTokenValue("tokenSimulado")
                .header("alg", "HS256")
                .subject("usuario@oursour.cl")
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(3600))
                .claim("idCuenta", 3)
                .claim("rol", "ROLE_ADMIN")
                .build();

        // El convertidor debe rechazar el rol desactualizado.
        assertThrows(
                InvalidBearerTokenException.class,
                () -> securityConfig
                        .jwtAuthenticationConverter()
                        .convert(token)
        );
    }


    /**
     * Rechaza el JWT cuando la cuenta
     * se encuentra desactivada.
     */
    @Test
    void rechazarTokenCuandoLaCuentaEstaDesactivada() {

        // Simula que el servicio detecta una cuenta desactivada.
        when(cuentaSeguridadService.obtenerCuentaActiva(3))
                .thenThrow(
                        new IllegalStateException("La cuenta está desactivada")
                );

        Instant ahora = Instant.now();

        Jwt token = Jwt.withTokenValue("tokenSimulado")
                .header("alg", "HS256")
                .subject("usuario@oursour.cl")
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(3600))
                .claim("idCuenta", 3)
                .claim("rol", "ROLE_CLIENTE")
                .build();

        assertThrows(
                InvalidBearerTokenException.class,
                () -> securityConfig
                        .jwtAuthenticationConverter()
                        .convert(token)
        );
    }

}
