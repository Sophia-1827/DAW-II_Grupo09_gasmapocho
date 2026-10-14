package pe.edu.cibertec.gasmapocho.msbalones.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msbalones.dto.ProveedorDTO;
import pe.edu.cibertec.gasmapocho.msbalones.dto.ProveedorRequestDTO;
import pe.edu.cibertec.gasmapocho.msbalones.model.Proveedor;
import pe.edu.cibertec.gasmapocho.msbalones.repository.ProveedorRepository;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public List<ProveedorDTO> listar() {
        return proveedorRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<ProveedorDTO> listarActivos() {
        return proveedorRepository.findByActivoTrue().stream().map(this::toDTO).toList();
    }

    public ProveedorDTO obtener(Long id) {
        return toDTO(buscarOLanzar(id));
    }

    public ProveedorDTO crear(ProveedorRequestDTO request) {
        if (proveedorRepository.existsByRut(normalizarRut(request.rut()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un proveedor con ese RUT");
        }
        Proveedor proveedor = new Proveedor();
        aplicar(proveedor, request);
        return toDTO(proveedorRepository.save(proveedor));
    }

    public ProveedorDTO actualizar(Long id, ProveedorRequestDTO request) {
        Proveedor proveedor = buscarOLanzar(id);
        if (proveedorRepository.existsByRutAndIdNot(normalizarRut(request.rut()), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe otro proveedor con ese RUT");
        }
        aplicar(proveedor, request);
        return toDTO(proveedorRepository.save(proveedor));
    }

    // Alterna activo/inactivo en vez de borrar, para no perder el historial de compras.
    public ProveedorDTO cambiarEstado(Long id) {
        Proveedor proveedor = buscarOLanzar(id);
        proveedor.setActivo(!proveedor.getActivo());
        return toDTO(proveedorRepository.save(proveedor));
    }

    private void aplicar(Proveedor proveedor, ProveedorRequestDTO request) {
        proveedor.setNombre(request.nombre().trim());
        proveedor.setRut(normalizarRut(request.rut()));
        proveedor.setTelefono(request.telefono().trim());
        proveedor.setDireccion(request.direccion().trim());
        proveedor.setRegion(request.region().trim());
    }

    private String normalizarRut(String rut) {
        return rut.trim().toUpperCase();
    }

    private Proveedor buscarOLanzar(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
    }

    private ProveedorDTO toDTO(Proveedor proveedor) {
        return new ProveedorDTO(proveedor.getId(), proveedor.getNombre(), proveedor.getRut(),
                proveedor.getTelefono(), proveedor.getDireccion(), proveedor.getRegion(), proveedor.getActivo());
    }
}
