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

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void listarDevuelveTodosLosUsuarios() {
        Usuario u1 = new Usuario(1L, "Admin", "admin@gasmapocho.cl", "hash1", Rol.ADMIN, true);
        Usuario u2 = new Usuario(2L, "Almacen", "almacen@gasmapocho.cl", "hash2", Rol.ALMACEN, true);
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
        Usuario existente = new Usuario(5L, "Viejo Nombre", "viejo@gasmapocho.cl", "hash", Rol.ALMACEN, true);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUpdateDTO request = new UsuarioUpdateDTO("Nombre Actualizado", "nuevo-email@gasmapocho.cl", Rol.ADMIN, null);
        UsuarioDTO resultado = usuarioService.actualizar(5L, request);

        assertThat(resultado.nombre()).isEqualTo("Nombre Actualizado");
        assertThat(resultado.email()).isEqualTo("nuevo-email@gasmapocho.cl");
        assertThat(resultado.rol()).isEqualTo(Rol.ADMIN);
    }

    @Test
    void actualizarCambiaElPasswordSoloSiVieneInformado() {
        Usuario existente = new Usuario(6L, "Alguien", "alguien@gasmapocho.cl", "hash-viejo", Rol.ALMACEN, true);
        when(usuarioRepository.findById(6L)).thenReturn(Optional.of(existente));
        when(passwordEncoder.encode("NuevaClave123")).thenReturn("hash-nuevo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUpdateDTO request = new UsuarioUpdateDTO("Alguien", "alguien@gasmapocho.cl", Rol.ALMACEN, "NuevaClave123");
        usuarioService.actualizar(6L, request);

        assertThat(existente.getPasswordHash()).isEqualTo("hash-nuevo");
    }

    @Test
    void eliminarBorraElUsuarioExistente() {
        Usuario existente = new Usuario(7L, "Alguien", "alguien@gasmapocho.cl", "hash", Rol.ALMACEN, true);
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(existente));

        usuarioService.eliminar(7L);

        verify(usuarioRepository).delete(existente);
    }

    @Test
    void cambiarEstadoDesactivaUnUsuarioActivo() {
        Usuario existente = new Usuario(8L, "Alguien", "alguien@gasmapocho.cl", "hash", Rol.ALMACEN, true);
        when(usuarioRepository.findById(8L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioDTO resultado = usuarioService.cambiarEstado(8L, "admin@gasmapocho.cl");

        assertThat(resultado.activo()).isFalse();
    }

    @Test
    void cambiarEstadoReactivaUnUsuarioDesactivado() {
        Usuario existente = new Usuario(8L, "Alguien", "alguien@gasmapocho.cl", "hash", Rol.ALMACEN, false);
        when(usuarioRepository.findById(8L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(usuarioService.cambiarEstado(8L, "admin@gasmapocho.cl").activo()).isTrue();
    }

    @Test
    void cambiarEstadoNoPermiteDesactivarLaPropiaCuenta() {
        Usuario admin = new Usuario(1L, "Admin", "admin@gasmapocho.cl", "hash", Rol.ADMIN, true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        ResponseStatusException ex = org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> usuarioService.cambiarEstado(1L, "ADMIN@gasmapocho.cl"));

        assertThat(ex.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.CONFLICT);
        assertThat(admin.getActivo()).isTrue();
    }

    @Test
    void cambiarEstadoLanza404SiElUsuarioNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> usuarioService.cambiarEstado(99L, "admin@gasmapocho.cl"));
    }

    @Test
    void eliminarLanza404SiElUsuarioNoExiste() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> usuarioService.eliminar(99L));
    }
}
