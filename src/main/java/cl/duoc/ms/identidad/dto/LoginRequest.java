
package cl.duoc.ms.identidad.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO que recibe las credenciales de un usuario
 * que desea iniciar sesión en Our Sour.
 *
 * @param email Correo electrónico del usuario.
 * @param password Contraseña ingresada por el usuario.
 */
public record LoginRequest(

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo debe tener un formato válido")
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        String password

) {
}
