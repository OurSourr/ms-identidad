
package cl.duoc.ms.identidad.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Configura las reglas de seguridad HTTP del microservicio.
     *
     * Permite el registro público de clientes y exige autenticación
     * para las demás solicitudes.
     *
     * @param http Configuración de seguridad proporcionada por Spring.
     * @return Cadena de filtros de seguridad.
     * @throws Exception Si ocurre un error de configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // API REST: no utilizaremos sesiones HTTP.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Desactiva CSRF para nuestra API sin sesiones.
            .csrf(csrf -> csrf.disable())

            // Desactiva el formulario HTML generado por Spring.
            .formLogin(form -> form.disable())

            // Desactiva la autenticación HTTP Basic.
            .httpBasic(basic -> basic.disable())

            // Define qué solicitudes están permitidas.
            .authorizeHttpRequests(auth -> auth

                // Cualquier visitante puede registrarse.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/registro"
                ).permitAll()

                // Las demás rutas requieren autenticación.
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * Registra BCrypt como codificador de contraseñas.
     *
     * @return Codificador de contraseñas basado en BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
