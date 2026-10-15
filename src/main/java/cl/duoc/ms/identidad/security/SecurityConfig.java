
package cl.duoc.ms.identidad.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Configura las reglas de seguridad HTTP del microservicio.
     *
     * Permite el registro y el inicio de sesión públicos.
     * Restringe la ruta administrativa al rol ROLE_ADMIN.
     * Las demás solicitudes requieren un JWT válido.
     *
     * @param http Configuración de seguridad proporcionada por Spring.
     * @return Cadena de filtros de seguridad.
     * @throws Exception Si ocurre un error de configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // API REST sin sesiones HTTP.
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Desactiva CSRF para esta API sin sesiones.
            .csrf(csrf -> csrf.disable())

            // Desactiva el formulario HTML y HTTP Basic.
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // Define las reglas de acceso en orden de prioridad.
            .authorizeHttpRequests(auth -> auth

                // Registro e inicio de sesión públicos.
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/auth/registro",
                    "/api/auth/login"
                ).permitAll()

                // Solo los administradores pueden acceder a esta ruta.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/auth/admin-test"
                ).hasRole("ADMIN")

                // Todas las demás rutas requieren autenticación.
                .anyRequest().authenticated()
            )

            // Valida los JWT y convierte el rol en una autoridad.
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Convierte el atributo personalizado "rol" del JWT
     * en una autoridad reconocida por Spring Security.
     *
     * @return Conversor de JWT a autenticación de Spring Security.
     */
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken>
            jwtAuthenticationConverter() {

        return jwt -> {
            String rol = jwt.getClaimAsString("rol");

            List<SimpleGrantedAuthority> autoridades =
                    rol == null || rol.isBlank()
                    ? List.of()
                    : List.of(new SimpleGrantedAuthority(rol));

            return new JwtAuthenticationToken(
                    jwt,
                    autoridades,
                    jwt.getSubject()
            );
        };
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
