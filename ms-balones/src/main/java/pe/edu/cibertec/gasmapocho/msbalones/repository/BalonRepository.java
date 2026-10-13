package pe.edu.cibertec.gasmapocho.msbalones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.cibertec.gasmapocho.msbalones.model.Balon;

public interface BalonRepository extends JpaRepository<Balon, Long> {

    boolean existsByCodigo(String codigo);

    Optional<Balon> findByCodigo(String codigo);
}
