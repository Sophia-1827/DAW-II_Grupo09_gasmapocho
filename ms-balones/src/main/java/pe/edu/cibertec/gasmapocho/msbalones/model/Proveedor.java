package pe.edu.cibertec.gasmapocho.msbalones.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "proveedor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 15)
    private String rut;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false, length = 60)
    private String region;

    // Un proveedor con compras registradas no se borra, se desactiva.
    @Column(nullable = false)
    private Boolean activo = true;
}
