package pe.edu.cibertec.gasmapocho.msusuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioCreateDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.dto.UsuarioUpdateDTO;
import pe.edu.cibertec.gasmapocho.msusuarios.model.Rol;
import pe.edu.cibertec.gasmapocho.msusuarios.model.Usuario;
import pe.edu.cibertec.gasmapocho.msusuarios.repository.UsuarioRepository;
import pe.edu.cibertec.gasmapocho.msusuarios.security.JwtService;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void listarDevuelveTodosLosUsuarios() {
        Usuario u1 = new Usuario(1L, "Admin", "admin@gasmapocho.cl", "hash1", Rol.ADMIN);
        Usuario u2 = new Usuario(2L, "Almacen", "almacen@gasmapocho.cl", "hash2", Rol.ALMACEN);
        when(usuarioRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UsuarioDTO> resultado = usuarioService.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).email()).isEqualTo("admin@gasmapocho.cl");
    }

    @Test
    void crearInsertaUnNuevoUsuarioConPasswordCifrado() {
        UsuarioCreateDTO request = new UsuarioCreateDTO("Nuevo", "nuevo@gasmapocho.cl", "Clave123", Rol.ALMACEN);
        when(usuarioRepository.existsByEmail("nuevo@gasmapocho.cl")).thenReturn(false);
        when(passwordEncoder.encode("Clave123")).thenReturn("hash-cifrado");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(inv -> {
                    Usuario u = inv.getArgument(0);
                    u.setId(10L);
                    return u;
                });

        UsuarioDTO resultado = usuarioService.crear(request);

        assertThat(resultado.id()).isEqualTo(10L);
        assertThat(resultado.email()).isEqualTo("nuevo@gasmapocho.cl");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void actualizarModificaLosDatosDelUsuarioExistente() {
        Usuario existente = new Usuario(5L, "Viejo Nombre", "viejo@gasmapocho.cl", "hash", Rol.ALMACEN);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUpdateDTO request = new UsuarioUpdateDTO("Nombre Actualizado", "nuevo-email@gasmapocho.cl", Rol.ADMIN);
        UsuarioDTO resultado = usuarioService.actualizar(5L, request);

        assertThat(resultado.nombre()).isEqualTo("Nombre Actualizado");
        assertThat(resultado.email()).isEqualTo("nuevo-email@gasmapocho.cl");
        assertThat(resultado.rol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void eliminarBorraElUsuarioExistente() {
        Usuario existente = new Usuario(7L, "Alguien", "alguien@gasmapocho.cl", "hash", Rol.ALMACEN);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(existente));

        usuarioService.eliminar(7L);

        verify(usuarioRepository).delete(existente);
    }

    @Test
    void eliminarLanza404SiElUsuarioNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> usuarioService.eliminar(99L));
    }
}
