package pe.edu.cibertec.gasmapocho.msbalones.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msbalones.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msbalones.dto.BalonRequestDTO;
import pe.edu.cibertec.gasmapocho.msbalones.model.Balon;
import pe.edu.cibertec.gasmapocho.msbalones.repository.BalonRepository;

@Service
public class BalonService {

    private final BalonRepository balonRepository;

    public BalonService(BalonRepository balonRepository) {
        this.balonRepository = balonRepository;
    }

    public List<BalonDTO> listar() {
        return balonRepository.findAll().stream().map(this::toDTO).toList();
    }

    public BalonDTO obtener(Long id) {
        return toDTO(buscarOLanzar(id));
    }

    public BalonDTO crear(BalonRequestDTO request) {
        if (balonRepository.existsByCodigo(request.codigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un balon con ese codigo");
        }
        Balon balon = new Balon();
        aplicar(balon, request);
        return toDTO(balonRepository.save(balon));
    }

    public BalonDTO actualizar(Long id, BalonRequestDTO request) {
        Balon balon = buscarOLanzar(id);
        aplicar(balon, request);
        return toDTO(balonRepository.save(balon));
    }

    public void eliminar(Long id) {
        balonRepository.delete(buscarOLanzar(id));
    }

    private void aplicar(Balon balon, BalonRequestDTO request) {
        balon.setCodigo(request.codigo().trim().toUpperCase());
        balon.setNombre(request.nombre().trim());
        balon.setPrecio(request.precio());
        balon.setStock(request.stock());
        balon.setStockMinimo(request.stockMinimo());
    }

    private Balon buscarOLanzar(Long id) {
        return balonRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Balon no encontrado"));
    }

    private BalonDTO toDTO(Balon balon) {
        return new BalonDTO(balon.getId(), balon.getCodigo(), balon.getNombre(), balon.getPrecio(),
                balon.getStock(), balon.getStockMinimo());
    }
}
