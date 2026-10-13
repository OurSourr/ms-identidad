
package cl.duoc.ms.identidad.exception;

/**
 * Excepción de autenticación que indica que las credenciales
 * proporcionadas no permiten identificar al usuario.
 *
 * No distingue entre correo inexistente y contraseña incorrecta,
 * para evitar revelar información sobre las cuentas registradas.
 */
public class CredencialesInvalidasException extends RuntimeException {

    /**
     * Construye una excepción de credenciales inválidas.
     *
     * @param mensaje Mensaje seguro para informar el error.
     */
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
