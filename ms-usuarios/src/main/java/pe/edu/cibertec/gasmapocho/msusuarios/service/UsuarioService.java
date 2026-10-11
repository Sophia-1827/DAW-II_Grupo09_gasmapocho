package pe.edu.cibertec.gasmapocho.msusuarios.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioCreateDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioUpdateDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.model.Usuario;
import pe.edu.cibertec.gasmapocho.msusuarios.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        return toDTO(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id) {
        Usuario usuario = buscarOLanzar(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarOLanzar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol());
    }
}
