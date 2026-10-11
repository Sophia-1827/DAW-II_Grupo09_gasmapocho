package pe.edu.cibertec.gasmapocho.msusuarios.controller;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.cibertec.gasmapocho.msusuarios.dto.LoginRequestDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.LoginResponseDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return usuarioService.login(request);
    }
}
