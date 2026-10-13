
package cl.duoc.ms.identidad.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuenta")
@Getter
@NoArgsConstructor
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Integer idCuenta;

    @Column(name = "email", nullable = false, unique = true, length = 160)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Cuenta(String email, String passwordHash, Rol rol) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = true;
    }

    //ejecuta el metodo antes de insertar una cuenta mediante JPA
    @PrePersist
    public void antesDeGuardar() {
        LocalDateTime ahora = LocalDateTime.now();
        this.createdAt = ahora;
        this.updatedAt = ahora;
    }

    //ejecuta el metodo antes de actualizarla mediante JPA
    @PreUpdate
    public void antesDeActualizar() {
        this.updatedAt = LocalDateTime.now();
    }
}
