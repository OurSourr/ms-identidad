
package cl.duoc.ms.identidad.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import cl.duoc.ms.identidad.entity.Cuenta;
import cl.duoc.ms.identidad.services.CuentaSeguridadService;

/**
 * Configuración central de seguridad del microservicio de identidad.
 *
 * Gestiona la autenticación mediante JWT y la autorización
 * según los roles vigentes de las cuentas en MySQL.
 */
@Configuration
public class SecurityConfig {

    /**
     * Servicio encargado de consultar el estado y
     * los permisos actuales de las cuentas.
     */
    @Autowired
    private CuentaSeguridadService cuentaSeguridadService;

    /**
     * Configura las reglas de seguridad HTTP del microservicio.
     *
     * Permite el registro y el inicio de sesión públicos.
     * Restringe la ruta administrativa al rol ROLE_ADMIN.
     * Las demás solicitudes requieren autenticación.
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

                // Acceso exclusivo para administradores.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/auth/admin-test"
                ).hasRole("ADMIN")

                // Las demás rutas requieren autenticación.
                .anyRequest().authenticated()
            )

            // Valida el JWT y consulta los permisos actuales.
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Convierte un JWT validado en una autenticación de Spring Security.
     *
     * Consulta MySQL para verificar que la cuenta siga activa
     * y recuperar su rol vigente, evitando utilizar permisos
     * desactualizados almacenados en el JWT.
     *
     * @return Conversor de JWT a autenticación.
     */
    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken>
            jwtAuthenticationConverter() {

        return jwt -> {

            // Obtiene el identificador de cuenta incluido en el JWT.
            Number idClaim;

            try {
                idClaim = jwt.getClaim("idCuenta");
            } catch (ClassCastException ex) {
                throw new InvalidBearerTokenException(
                        "El identificador de cuenta del token no es válido"
                );
            }

            // Rechaza tokens sin un identificador válido.
            if (idClaim == null
                    || idClaim.longValue() <= 0
                    || idClaim.longValue() > Integer.MAX_VALUE
                    || idClaim.doubleValue() != idClaim.longValue()) {

                throw new InvalidBearerTokenException(
                        "El token no contiene un identificador de cuenta válido"
                );
            }

            // Busca la cuenta y verifica que siga activa.
            Cuenta cuenta;

            try {
                cuenta = cuentaSeguridadService.obtenerCuentaActiva(
                        idClaim.intValue()
                );
            } catch (IllegalStateException ex) {
                throw new InvalidBearerTokenException(
                        "La cuenta no existe o se encuentra desactivada"
                );
            }

            // Obtiene el rol vigente desde MySQL.
            String rolActual = cuenta.getRol().getNombre();

            // Comprueba que el rol del JWT coincida con el rol vigente en MySQL.
            // Si cambió, el usuario deberá iniciar sesión nuevamente.
            String rolToken = jwt.getClaimAsString("rol");

            if (rolToken == null || !rolToken.equals(rolActual)) {
                throw new InvalidBearerTokenException(
                        "Los permisos de la cuenta cambiaron. Inicie sesión nuevamente"
                );
            }

            // Construye las autoridades reconocidas por Spring Security.
            List<SimpleGrantedAuthority> autoridades = List.of(
                    new SimpleGrantedAuthority(rolActual)
            );

            // Devuelve la autenticación con los permisos actuales.
            return new JwtAuthenticationToken(
                    jwt,
                    autoridades,
                    cuenta.getEmail()
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
