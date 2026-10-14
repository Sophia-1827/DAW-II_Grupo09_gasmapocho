package pe.edu.cibertec.gasmapocho.msbalones.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.cibertec.gasmapocho.msbalones.dto.ProveedorDTO;
import pe.edu.cibertec.gasmapocho.msbalones.dto.ProveedorRequestDTO;
import pe.edu.cibertec.gasmapocho.msbalones.service.ProveedorService;

// ALMACEN lee y puede dar de alta un proveedor nuevo al registrar una compra; solo ADMIN edita y activa/desactiva.
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public List<ProveedorDTO> listar() {
        return proveedorService.listar();
    }

    @GetMapping("/activos")
    public List<ProveedorDTO> listarActivos() {
        return proveedorService.listarActivos();
    }

    @GetMapping("/{id}")
    public ProveedorDTO obtener(@PathVariable Long id) {
        return proveedorService.obtener(id);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'ALMACEN')")
    @PostMapping
    public ProveedorDTO crear(@Valid @RequestBody ProveedorRequestDTO request) {
        return proveedorService.crear(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ProveedorDTO actualizar(@PathVariable Long id, @Valid @RequestBody ProveedorRequestDTO request) {
        return proveedorService.actualizar(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/estado")
    public ProveedorDTO cambiarEstado(@PathVariable Long id) {
        return proveedorService.cambiarEstado(id);
    }
}
