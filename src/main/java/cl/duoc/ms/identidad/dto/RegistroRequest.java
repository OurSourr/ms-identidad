
package cl.duoc.ms.identidad.dto;

/**
 * DTO que representa los datos enviados por un usuario
 * cuando solicita registrarse en Our Sour.
 *
 * @param email Correo electrónico del usuario.
 * @param password Contraseña original que será protegida con BCrypt.
 */
public record RegistroRequest(
        String email,
        String password
) {
}
