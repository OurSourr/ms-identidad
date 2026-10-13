
package cl.duoc.ms.identidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO que representa los datos enviados por un usuario
 * cuando solicita registrarse en Our Sour.
 *
 * @param email Correo electrónico del usuario.
 * @param password Contraseña original que será protegida con BCrypt.
 */
public record RegistroRequest(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password

) {
}
