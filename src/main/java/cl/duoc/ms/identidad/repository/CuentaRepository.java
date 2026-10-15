
package cl.duoc.ms.identidad.repository;

import cl.duoc.ms.identidad.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.Optional;

public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {


    @EntityGraph(attributePaths = "rol")
    Optional<Cuenta> findByEmail(String email);

    boolean existsByEmail(String email);
}
