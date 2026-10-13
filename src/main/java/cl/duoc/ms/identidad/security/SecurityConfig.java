package cl.duoc.ms.identidad.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration 
public class SecurityConfig {


/**
 * Registra el codificador BCrypt como un componente de Spring.
 *
 * Se utilizará para generar hashes de contraseñas
 * y verificar las credenciales durante el inicio de sesión.
 *
 * @return Codificador de contraseñas basado en BCrypt.
 */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
