
package cl.duoc.ms.identidad.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centraliza el manejo de excepciones de la API.
 *
 * Permite entregar respuestas HTTP consistentes sin
 * exponer información técnica o sensible al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validación de los DTO.
     *
     * @param ex Excepción generada por Bean Validation.
     * @return Respuesta HTTP 400 con el detalle de los campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new java.util.LinkedHashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.putIfAbsent(
                        error.getField(),
                        error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Valor inválido"
                )
        );

        return ResponseEntity.badRequest().body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", "La solicitud contiene datos inválidos",
                "errors", errores
        ));
    }



    /**
     * Maneja los intentos de registro con un correo ya existente.
     *
     * @param ex Excepción de correo duplicado.
     * @return Respuesta HTTP 409 Conflict.
     */
    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> manejarCorreoDuplicado(
            CorreoDuplicadoException ex) {

        return crearRespuesta(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }




    /**
     * Maneja los intentos de autenticación con credenciales incorrectas.
     *
     * @param ex Excepción producida durante la autenticación.
     * @return Respuesta HTTP 401 Unauthorized.
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, Object>> manejarCredencialesInvalidas(
            CredencialesInvalidasException ex) {

        return crearRespuesta(
                HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas"
        );
    }



    /**
     * Maneja errores de argumentos utilizados por los servicios.
     *
     * @param ex Excepción de argumento inválido.
     * @return Respuesta HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(
            IllegalArgumentException ex) {

        return crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "La solicitud no pudo procesarse"
        );
    }

    /**
     * Maneja errores inesperados sin revelar información interna.
     *
     * @param ex Excepción no controlada.
     * @return Respuesta HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorInesperado(
            Exception ex) {

        return crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error interno del servidor"
        );
    }

    /**
     * Construye una respuesta de error con formato uniforme.
     *
     * @param estado Código HTTP de la respuesta.
     * @param mensaje Mensaje seguro para el cliente.
     * @return Respuesta HTTP estructurada.
     */
    private ResponseEntity<Map<String, Object>> crearRespuesta(
            HttpStatus estado,
            String mensaje) {

        return ResponseEntity.status(estado).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", estado.value(),
                "message", mensaje
        ));
    }
}
