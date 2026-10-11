package pe.edu.cibertec.gasmapocho.msusuarios.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msusuarios.dto.LoginRequestDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.LoginResponseDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.model.Rol;
import pe.edu.cibertec.gasmapocho.msusuarios.model.Usuario;
import pe.edu.cibertec.gasmapocho.msusuarios.repository.UsuarioRepository;
import pe.edu.cibertec.gasmapocho.msusuarios.security.JwtService;
import pe.edu.cibertec.gasmapocho.msusuarios.security.UserDetailsServiceImpl;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UsuarioRepository usuarioRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authManager, UsuarioRepository usuarioRepository,
            UserDetailsServiceImpl userDetailsService, JwtService jwtService) {
        this.authManager = authManager;
        this.usuarioRepository = usuarioRepository;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));

        Rol rol = usuario.getRol();
        List<GrantedAuthority> authorities = userDetailsService.getAuthorities(rol);
        String token = jwtService.generarToken(usuario.getEmail(), authorities);
        return new LoginResponseDTO(token, usuario.getNombre(), rol);
    }
}
