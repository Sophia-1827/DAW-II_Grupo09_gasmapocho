package pe.edu.cibertec.gasmapocho.msbalones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.cibertec.gasmapocho.msbalones.model.Proveedor;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    boolean existsByRut(String rut);

    boolean existsByRutAndIdNot(String rut, Long id);

    List<Proveedor> findByActivoTrue();
}
