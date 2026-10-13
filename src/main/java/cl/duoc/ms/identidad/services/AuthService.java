
package cl.duoc.ms.identidad.services;
import cl.duoc.ms.identidad.exception.CorreoDuplicadoException;
import cl.duoc.ms.identidad.entity.Cuenta;
import cl.duoc.ms.identidad.entity.Rol;
import cl.duoc.ms.identidad.repository.CuentaRepository;
import cl.duoc.ms.identidad.repository.RolRepository;

import java.util.Locale;
import cl.duoc.ms.identidad.exception.CredencialesInvalidasException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    // Repositorio para consultar y guardar cuentas.
    @Autowired
    private CuentaRepository cuentaRepository;

    // Repositorio para consultar los roles del sistema.
    @Autowired
    private RolRepository rolRepository;

    // Componente encargado de proteger las contraseñas con BCrypt.
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Comprueba si un correo electrónico ya está registrado.
     * Normaliza el correo antes de consultar la base de datos.
     *
     * @param email Correo electrónico que se desea verificar.
     * @return true si el correo existe; false en caso contrario.
     */
    public boolean existeCorreo(String email) {

        String correo = email.trim().toLowerCase(Locale.ROOT);

        return cuentaRepository.existsByEmail(correo);
    }

    /**
     * Obtiene el rol predeterminado para los usuarios
     * que se registran en Our Sour.
     *
     * @return El objeto Rol correspondiente a ROLE_CLIENTE.
     * @throws IllegalStateException si el rol no existe en la base de datos.
     */
    private Rol obtenerRolCliente() {

        return rolRepository.findByNombre("ROLE_CLIENTE")
                .orElseThrow(() ->
                    new IllegalStateException("El rol ROLE_CLIENTE no existe")
                );
    }

    /**
     * Registra una nueva cuenta de cliente en Our Sour.
     *
     * Comprueba que el correo no esté registrado,
     * genera el hash BCrypt de la contraseña,
     * asigna ROLE_CLIENTE y guarda la cuenta.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña original del usuario.
     * @return La cuenta registrada.
     * @throws IllegalArgumentException si el correo ya está registrado.
     * @throws IllegalStateException si el rol ROLE_CLIENTE no existe.
     */
    public Cuenta registrar(String email, String password) {

        // Normaliza el correo electrónico.
        String correo = email.trim().toLowerCase(Locale.ROOT);

        // Verifica que el correo no esté registrado.
        if (existeCorreo(correo)) {
            throw new CorreoDuplicadoException(
                "El correo electrónico ya está registrado"
            );
        }

        // Obtiene el rol que corresponde a los nuevos usuarios.
        Rol rolCliente = obtenerRolCliente();

        // Genera el hash BCrypt de la contraseña.
        String passwordHash = passwordEncoder.encode(password);

        // Construye la nueva cuenta.
        Cuenta cuenta = new Cuenta(correo, passwordHash, rolCliente);

        // Guarda la cuenta en MySQL mediante el repositorio.
        return cuentaRepository.save(cuenta);
    }


    /**
     * Autentica a un usuario mediante su correo y contraseña.
     *
     * Comprueba que la cuenta exista, esté activa y que
     * la contraseña coincida con el hash BCrypt almacenado.
     *
     * @param email Correo electrónico del usuario.
     * @param password Contraseña ingresada por el usuario.
     * @return Cuenta autenticada.
     * @throws IllegalArgumentException si las credenciales son incorrectas.
     * @throws IllegalStateException si la cuenta está desactivada.
     */

    public Cuenta autenticar(String email, String password) {

        // Normaliza el correo antes de buscarlo.
        String correo = email.trim().toLowerCase(Locale.ROOT);

        // Busca la cuenta en MySQL.
        Cuenta cuenta = cuentaRepository.findByEmail(correo)
                .orElseThrow(() ->
                        new CredencialesInvalidasException("Credenciales incorrectas")
                );

        // Verifica la contraseña contra el hash BCrypt almacenado.
        if (!passwordEncoder.matches(password, cuenta.getPasswordHash())) {
            throw new CredencialesInvalidasException("Credenciales incorrectas");
        }

        // Impide el acceso a cuentas desactivadas.
        if (!Boolean.TRUE.equals(cuenta.getActivo())) {
            throw new IllegalStateException("La cuenta está desactivada");
        }

        return cuenta;
    }

}
