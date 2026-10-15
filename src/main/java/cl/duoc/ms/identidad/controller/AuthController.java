
package cl.duoc.ms.identidad.controller;

import cl.duoc.ms.identidad.dto.CuentaResponse;
import cl.duoc.ms.identidad.dto.RegistroRequest;
import cl.duoc.ms.identidad.entity.Cuenta;
import cl.duoc.ms.identidad.services.AuthService;

import jakarta.validation.Valid;


import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import cl.duoc.ms.identidad.dto.LoginRequest;
import cl.duoc.ms.identidad.dto.AuthResponse;
import cl.duoc.ms.identidad.services.JwtService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;


    @Autowired
    private JwtService jwtService;
    /**
     * Recibe los datos de registro, valida la solicitud
     * y delega la creación de la cuenta al servicio.
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

        // Prepara una respuesta sin información sensible.
        CuentaResponse respuesta = new CuentaResponse(
                cuenta.getIdCuenta(),
                cuenta.getEmail(),
                cuenta.getRol().getNombre(),
                cuenta.getActivo()
        );

        // Devuelve HTTP 201: recurso creado.
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

        // Comprueba las credenciales y obtiene la cuenta autenticada.
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

        // Construye la respuesta que recibirá el cliente.
        AuthResponse respuesta = new AuthResponse(token, "Bearer");

        return ResponseEntity.ok(respuesta);
        }

        /**
         * Obtiene la identidad del usuario autenticado mediante JWT.
         *
         * Spring Security valida el token antes de permitir
         * la ejecución de este método.
         *
         * @param autenticacion Información del JWT validado.
         * @return Identidad pública del usuario autenticado.
         */
        @GetMapping("/me")
        public ResponseEntity<Map<String, Object>> obtenerMiPerfil(
                JwtAuthenticationToken autenticacion) {

        // Recupera los datos del JWT previamente validado.
        String correo = autenticacion.getToken().getSubject();
        Integer idCuenta = ((Number) autenticacion.getToken()
                .getClaim("idCuenta")).intValue();
        String rol = autenticacion.getToken().getClaimAsString("rol");

        // Devuelve únicamente información no sensible.
        return ResponseEntity.ok(Map.of(
                "idCuenta", idCuenta,
                "email", correo,
                "rol", rol
        ));
        }
}
