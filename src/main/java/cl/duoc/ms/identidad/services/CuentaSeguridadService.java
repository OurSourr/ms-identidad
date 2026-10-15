
package cl.duoc.ms.identidad.services;

import cl.duoc.ms.identidad.entity.Cuenta;
import cl.duoc.ms.identidad.repository.CuentaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaSeguridadService {

    // Repositorio para consultar las cuentas registradas.
    @Autowired
    private CuentaRepository cuentaRepository;

    /**
     * Obtiene una cuenta activa y su rol vigente desde MySQL.
     *
     * Este método permite comprobar los permisos actuales
     * de un usuario que presenta un JWT previamente emitido.
     *
     * @param idCuenta Identificador de la cuenta.
     * @return Cuenta activa con su rol disponible.
     * @throws IllegalStateException si la cuenta no existe
     *         o se encuentra desactivada.
     */
    @Transactional(readOnly = true)
    public Cuenta obtenerCuentaActiva(Integer idCuenta) {

        // Busca la cuenta utilizando su identificador.
        Cuenta cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "La cuenta no existe"
                        )
                );

        // Impide utilizar cuentas desactivadas.
        if (!Boolean.TRUE.equals(cuenta.getActivo())) {
            throw new IllegalStateException(
                    "La cuenta está desactivada"
            );
        }

        // Inicializa el rol mientras la transacción está activa.
        cuenta.getRol().getNombre();

        return cuenta;
    }
}
