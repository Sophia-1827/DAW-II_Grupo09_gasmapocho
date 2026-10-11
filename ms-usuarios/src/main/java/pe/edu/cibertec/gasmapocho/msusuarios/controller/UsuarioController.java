package pe.edu.cibertec.gasmapocho.msusuarios.controller;

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

import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioCreateDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioUpdateDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.service.UsuarioService;

// Solo ADMIN: ALMACEN no tiene acceso al modulo de usuarios (ver cuadro de permisos del informe).
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioDTO> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public UsuarioDTO obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    public UsuarioDTO crear(@Valid @RequestBody UsuarioCreateDTO request) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{id}")
    public UsuarioDTO actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateDTO request) {
        return usuarioService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}
