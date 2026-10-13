
package cl.duoc.ms.identidad.controller;

import cl.duoc.ms.identidad.dto.CuentaResponse;
import cl.duoc.ms.identidad.dto.RegistroRequest;
import cl.duoc.ms.identidad.entity.Cuenta;
import cl.duoc.ms.identidad.services.AuthService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

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
}
