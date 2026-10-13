
package cl.duoc.ms.identidad.exception;

/**
 * Excepción de negocio que indica que un correo electrónico
 * ya se encuentra registrado en el sistema.
 */
public class CorreoDuplicadoException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo.
     *
     * @param mensaje Descripción del conflicto.
     */
    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
