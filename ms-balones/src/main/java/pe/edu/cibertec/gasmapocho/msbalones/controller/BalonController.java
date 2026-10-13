package pe.edu.cibertec.gasmapocho.msbalones.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.cibertec.gasmapocho.msbalones.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msbalones.dto.BalonRequestDTO;
import pe.edu.cibertec.gasmapocho.msbalones.service.BalonService;

// ADMIN tiene CRUD completo; ALMACEN solo lectura (ver cuadro de permisos del informe).
@RestController
@RequestMapping("/api/balones")
public class BalonController {

    private final BalonService balonService;

    public BalonController(BalonService balonService) {
        this.balonService = balonService;
    }

    @GetMapping
    public List<BalonDTO> listar() {
        return balonService.listar();
    }

    @GetMapping("/{id}")
    public BalonDTO obtener(@PathVariable Long id) {
        return balonService.obtener(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public BalonDTO crear(@Valid @RequestBody BalonRequestDTO request) {
        return balonService.crear(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public BalonDTO actualizar(@PathVariable Long id, @Valid @RequestBody BalonRequestDTO request) {
        return balonService.actualizar(id, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        balonService.eliminar(id);
    }
}
