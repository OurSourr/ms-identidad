
package cl.duoc.ms.identidad.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 *
 * Los errores inesperados se registran en la consola
 * para facilitar su diagnóstico.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Registrador centralizado de errores del servidor.
     */
    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de validación de los DTO.
     *
     * @param ex Excepción generada por Bean Validation.
     * @return Respuesta HTTP 400 con el detalle de los campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new LinkedHashMap<>();

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
     * Maneja errores inesperados y registra su causa técnica.
     *
     * La excepción completa se registra en el servidor,
     * pero no se expone al cliente.
     *
     * @param ex Excepción no controlada.
     * @return Respuesta HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorInesperado(
            Exception ex) {

        // Registra la excepción y su traza para facilitar el diagnóstico.
        log.error("Error inesperado al procesar la solicitud", ex);

        // Devuelve una respuesta segura sin detalles internos.
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
