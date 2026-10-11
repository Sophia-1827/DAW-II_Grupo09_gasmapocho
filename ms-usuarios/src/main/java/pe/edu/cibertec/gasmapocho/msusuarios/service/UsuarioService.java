package pe.edu.cibertec.gasmapocho.msusuarios.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msusuarios.dto.LoginRequestDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.LoginResponseDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioCreateDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioUpdateDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.model.Usuario;
import pe.edu.cibertec.gasmapocho.msusuarios.repository.UsuarioRepository;
import pe.edu.cibertec.gasmapocho.msusuarios.security.JwtService;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public List<UsuarioDTO> listar() {
        return usuarioRepository.findAll().stream().map(this::toDTO).toList();
    }

    public UsuarioDTO obtener(Long id) {
        return toDTO(buscarOLanzar(id));
    }

    public UsuarioDTO crear(UsuarioCreateDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        return toDTO(usuarioRepository.save(usuario));
    }

    public UsuarioDTO actualizar(Long id, UsuarioUpdateDTO request) {
        Usuario usuario = buscarOLanzar(id);
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setRol(request.rol());
        return toDTO(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarOLanzar(id);
        usuarioRepository.delete(usuario);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }
        String token = jwtService.generarToken(usuario.getEmail(), usuario.getRol().name());
        return new LoginResponseDTO(token, usuario.getNombre(), usuario.getRol());
    }

    private Usuario buscarOLanzar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
