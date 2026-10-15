
package cl.duoc.ms.identidad.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pruebas de seguridad de los endpoints HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Un usuario sin JWT no puede consultar su perfil.
     */
    @Test
    void accederAlPerfilSinTokenDebeRetornar401() throws Exception {

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Un usuario sin JWT no puede acceder
     * a los recursos administrativos.
     */
    @Test
    void accederComoAdministradorSinTokenDebeRetornar401()
            throws Exception {

        mockMvc.perform(get("/api/auth/admin-test"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Un cliente autenticado no puede acceder
     * a los recursos exclusivos del administrador.
     */
    @Test
    void clienteNoPuedeAccederComoAdministrador()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/admin-test")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .subject("cliente@oursour.cl")
                                        .claim("idCuenta", 3)
                                        .claim("rol", "ROLE_CLIENTE")
                                )
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_CLIENTE")
                                )
                        )
                )
                .andExpect(status().isForbidden());
    }

    /**
     * Un administrador autenticado puede acceder
     * a los recursos administrativos.
     */
    @Test
    void administradorPuedeAccederAlRecursoAdministrativo()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/admin-test")
                        .with(SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt
                                        .subject("admin@oursour.cl")
                                        .claim("idCuenta", 4)
                                        .claim("rol", "ROLE_ADMIN")
                                )
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADMIN")
                                )
                        )
                )
                .andExpect(status().isOk());
    }
}
