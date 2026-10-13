
package cl.duoc.ms.identidad.dto;

/**
 * DTO que representa la información pública
 * de una cuenta registrada en Our Sour.
 *
 * No incluye la contraseña ni su hash por seguridad.
 *
 * @param idCuenta Identificador de la cuenta.
 * @param email Correo electrónico registrado.
 * @param rol Nombre del rol asignado.
 * @param activo Estado actual de la cuenta.
 */
public record CuentaResponse(
        Integer idCuenta,
        String email,
        String rol,
        Boolean activo
) {
}
