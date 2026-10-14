package pe.edu.cibertec.gasmapocho.msbalones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msbalones.dto.ProveedorDTO;
import pe.edu.cibertec.gasmapocho.msbalones.dto.ProveedorRequestDTO;
import pe.edu.cibertec.gasmapocho.msbalones.model.Proveedor;
import pe.edu.cibertec.gasmapocho.msbalones.repository.ProveedorRepository;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    @Test
    void listarActivosDevuelveSoloLosActivos() {
        Proveedor p1 = new Proveedor(1L, "Gas Andes", "76.512.340-1", "+56 2 2765 4410", "Maipu", "RM", true);
        when(proveedorRepository.findByActivoTrue()).thenReturn(List.of(p1));

        List<ProveedorDTO> resultado = proveedorService.listarActivos();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).nombre()).isEqualTo("Gas Andes");
    }

    @Test
    void crearInsertaProveedorActivoConRutNormalizado() {
        ProveedorRequestDTO request = new ProveedorRequestDTO("Nuevo SpA", " 12.345.678-k ", "+56 9 1111 2222",
                "Calle 1", "Maule");
        when(proveedorRepository.existsByRut("12.345.678-K")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> {
            Proveedor p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProveedorDTO resultado = proveedorService.crear(request);

        assertThat(resultado.id()).isEqualTo(10L);
        assertThat(resultado.rut()).isEqualTo("12.345.678-K");
        assertThat(resultado.activo()).isTrue();
    }

    @Test
    void crearRechazaRutDuplicado() {
        ProveedorRequestDTO request = new ProveedorRequestDTO("Otro", "76.512.340-1", "123", "Calle", "RM");
        when(proveedorRepository.existsByRut("76.512.340-1")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> proveedorService.crear(request));
    }

    @Test
    void actualizarRechazaRutDeOtroProveedor() {
        Proveedor existente = new Proveedor(3L, "Sur Gas", "96.784.120-3", "123", "Coronel", "Biobio", true);
        when(proveedorRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.existsByRutAndIdNot("76.512.340-1", 3L)).thenReturn(true);

        ProveedorRequestDTO request = new ProveedorRequestDTO("Sur Gas", "76.512.340-1", "123", "Coronel", "Biobio");

        assertThrows(ResponseStatusException.class, () -> proveedorService.actualizar(3L, request));
    }

    @Test
    void actualizarModificaLosDatosDelProveedor() {
        Proveedor existente = new Proveedor(2L, "Pacifico", "77.208.915-6", "111", "Valparaiso", "Valparaiso", true);
        when(proveedorRepository.findById(2L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.existsByRutAndIdNot("77.208.915-6", 2L)).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        ProveedorDTO resultado = proveedorService.actualizar(2L,
                new ProveedorRequestDTO("Pacifico Ltda.", "77.208.915-6", "222", "Viña del Mar", "Valparaiso"));

        assertThat(resultado.nombre()).isEqualTo("Pacifico Ltda.");
        assertThat(resultado.telefono()).isEqualTo("222");
    }

    @Test
    void cambiarEstadoAlternaActivoSinBorrar() {
        Proveedor existente = new Proveedor(4L, "Pacifico", "77.208.915-6", "111", "Valparaiso", "Valparaiso", true);
        when(proveedorRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.save(any(Proveedor.class))).thenAnswer(inv -> inv.getArgument(0));

        ProveedorDTO resultado = proveedorService.cambiarEstado(4L);

        assertThat(resultado.activo()).isFalse();
        verify(proveedorRepository).save(existente);
    }

    @Test
    void cambiarEstadoLanza404SiNoExiste() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> proveedorService.cambiarEstado(99L));
    }
}
