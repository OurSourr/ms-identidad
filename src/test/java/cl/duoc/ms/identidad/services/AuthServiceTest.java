package cl.duoc.ms.identidad.services;

import cl.duoc.ms.identidad.entity.Cuenta;
import cl.duoc.ms.identidad.exception.CredencialesInvalidasException;
import cl.duoc.ms.identidad.repository.CuentaRepository;
import cl.duoc.ms.identidad.repository.RolRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del servicio de autenticación.
 *
 * Utiliza Mockito para simular las dependencias,
 * sin conectarse a una base de datos real.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    /**
     * Comprueba que un usuario con credenciales válidas
     * pueda autenticarse correctamente.
     */
    @Test
    void autenticarConCredencialesCorrectas() {

        Cuenta cuenta = mock(Cuenta.class);

        when(cuentaRepository.findByEmail("cliente@oursour.cl"))
                .thenReturn(Optional.of(cuenta));

        when(cuenta.getPasswordHash()).thenReturn("hashSimulado");
        when(passwordEncoder.matches("Password123!", "hashSimulado"))
                .thenReturn(true);
        when(cuenta.getActivo()).thenReturn(true);

        Cuenta resultado = authService.autenticar(
                "CLIENTE@OURSOUR.CL",
                "Password123!"
        );

        assertSame(cuenta, resultado);
        verify(cuentaRepository).findByEmail("cliente@oursour.cl");
    }

    /**
     * Comprueba que una contraseña incorrecta
     * provoque el rechazo de la autenticación.
     */
    @Test
    void rechazarPasswordIncorrecta() {

        Cuenta cuenta = mock(Cuenta.class);

        when(cuentaRepository.findByEmail("cliente@oursour.cl"))
                .thenReturn(Optional.of(cuenta));

        when(cuenta.getPasswordHash()).thenReturn("hashSimulado");
        when(passwordEncoder.matches("Incorrecta", "hashSimulado"))
                .thenReturn(false);

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.autenticar(
                        "cliente@oursour.cl",
                        "Incorrecta"
                )
        );
    }

    /**
     * Comprueba que no se permita autenticar
     * un correo inexistente.
     */
    @Test
    void rechazarCorreoInexistente() {

        when(cuentaRepository.findByEmail("inexistente@oursour.cl"))
                .thenReturn(Optional.empty());

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.autenticar(
                        "inexistente@oursour.cl",
                        "Password123!"
                )
        );
    }

    /**
     * Comprueba que una cuenta desactivada
     * no pueda iniciar sesión.
     */
    @Test
    void rechazarCuentaDesactivada() {

        Cuenta cuenta = mock(Cuenta.class);

        when(cuentaRepository.findByEmail("cliente@oursour.cl"))
                .thenReturn(Optional.of(cuenta));

        when(cuenta.getPasswordHash()).thenReturn("hashSimulado");
        when(passwordEncoder.matches("Password123!", "hashSimulado"))
                .thenReturn(true);
        when(cuenta.getActivo()).thenReturn(false);

        assertThrows(
                IllegalStateException.class,
                () -> authService.autenticar(
                        "cliente@oursour.cl",
                        "Password123!"
                )
        );
    }
}