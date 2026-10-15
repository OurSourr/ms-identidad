
package cl.duoc.ms.identidad.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ms.identidad.dto.AuthResponse;
import cl.duoc.ms.identidad.dto.CuentaResponse;
import cl.duoc.ms.identidad.dto.LoginRequest;
import cl.duoc.ms.identidad.dto.RegistroRequest;
import cl.duoc.ms.identidad.entity.Cuenta;
import cl.duoc.ms.identidad.services.AuthService;
import cl.duoc.ms.identidad.services.JwtService;

import jakarta.validation.Valid;

/**
 * Controlador REST responsable del registro, autenticación
 * y consulta de identidad de los usuarios de Our Sour.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    /**
     * Registra una nueva cuenta de cliente.
     *
     * @param request Datos enviados por el cliente.
     * @return Datos públicos de la cuenta y estado HTTP 201.
     */
    @PostMapping("/registro")
    public ResponseEntity<CuentaResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {

        // Ejecuta la lógica de registro.
        Cuenta cuenta = authService.registrar(
                request.email(),
                request.password()
        );

        // Construye una respuesta sin información sensible.
        CuentaResponse respuesta = new CuentaResponse(
                cuenta.getIdCuenta(),
                cuenta.getEmail(),
                cuenta.getRol().getNombre(),
                cuenta.getActivo()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    /**
     * Autentica al usuario y genera un token JWT.
     *
     * @param request Correo y contraseña enviados por el cliente.
     * @return Token JWT y tipo de autenticación HTTP 200.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        // Comprueba las credenciales del usuario.
        Cuenta cuenta = authService.autenticar(
                request.email(),
                request.password()
        );

        // Genera un JWT firmado con los datos de la cuenta.
        String token = jwtService.generarToken(
                cuenta.getIdCuenta(),
                cuenta.getEmail(),
                cuenta.getRol().getNombre()
        );

        // Construye la respuesta de autenticación.
        AuthResponse respuesta = new AuthResponse(
                token,
                "Bearer"
        );

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene la identidad del usuario autenticado mediante JWT.
     *
     * Spring Security valida el token antes de ejecutar este método.
     *
     * @param autenticacion Información del JWT validado.
     * @return Identidad pública del usuario autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> obtenerMiPerfil(
            JwtAuthenticationToken autenticacion) {

        // Recupera los datos del JWT validado.
        String correo = autenticacion.getToken().getSubject();

        Integer idCuenta = ((Number) autenticacion.getToken()
                .getClaim("idCuenta")).intValue();

        String rol = autenticacion.getToken()
                .getClaimAsString("rol");

        // Devuelve únicamente información no sensible.
        return ResponseEntity.ok(Map.of(
                "idCuenta", idCuenta,
                "email", correo,
                "rol", rol
        ));
    }

    /**
     * Endpoint de prueba para verificar la autorización administrativa.
     *
     * Spring Security permite el acceso únicamente a usuarios
     * autenticados con la autoridad ROLE_ADMIN.
     *
     * @return Mensaje de confirmación del acceso administrativo.
     */
    @GetMapping("/admin-test")
    public ResponseEntity<String> probarAccesoAdministrador() {

        // Solo se ejecuta si Spring Security autoriza el acceso.
        return ResponseEntity.ok(
                "Acceso autorizado: ROLE_ADMIN"
        );
    }
}
