package pe.edu.cibertec.gasmapocho.msbalones.service;

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
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msbalones.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msbalones.dto.BalonRequestDTO;
import pe.edu.cibertec.gasmapocho.msbalones.model.Balon;
import pe.edu.cibertec.gasmapocho.msbalones.repository.BalonRepository;

@ExtendWith(MockitoExtension.class)
class BalonServiceTest {

    @Mock
    private BalonRepository balonRepository;

    @InjectMocks
    private BalonService balonService;

    @Test
    void listarDevuelveTodosLosBalones() {
        Balon b1 = new Balon(1L, "BAL-5", "Balon 5 kg", 14500.0, 40, 10);
        Balon b2 = new Balon(2L, "BAL-11", "Balon 11 kg", 21000.0, 30, 8);
        when(balonRepository.findAll()).thenReturn(List.of(b1, b2));

        List<BalonDTO> resultado = balonService.listar();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).codigo()).isEqualTo("BAL-5");
    }

    @Test
    void crearInsertaUnNuevoBalon() {
        BalonRequestDTO request = new BalonRequestDTO("bal-20", "Balon 20 kg", 35000.0, 5, 2);
        when(balonRepository.existsByCodigo("bal-20")).thenReturn(false);
        when(balonRepository.save(any(Balon.class))).thenAnswer(inv -> {
            Balon b = inv.getArgument(0);
            b.setId(10L);
            return b;
        });

        BalonDTO resultado = balonService.crear(request);

        assertThat(resultado.id()).isEqualTo(10L);
        assertThat(resultado.codigo()).isEqualTo("BAL-20");
        verify(balonRepository).save(any(Balon.class));
    }

    @Test
    void crearRechazaCodigoDuplicado() {
        BalonRequestDTO request = new BalonRequestDTO("BAL-5", "Balon 5 kg", 14500.0, 40, 10);
        when(balonRepository.existsByCodigo("BAL-5")).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> balonService.crear(request));
    }

    @Test
    void actualizarModificaLosDatosDelBalonExistente() {
        Balon existente = new Balon(5L, "BAL-11", "Balon 11 kg", 21000.0, 30, 8);
        when(balonRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(balonRepository.save(any(Balon.class))).thenAnswer(inv -> inv.getArgument(0));

        BalonRequestDTO request = new BalonRequestDTO("BAL-11", "Balon 11 kg actualizado", 22500.0, 25, 8);
        BalonDTO resultado = balonService.actualizar(5L, request);

        assertThat(resultado.nombre()).isEqualTo("Balon 11 kg actualizado");
        assertThat(resultado.precio()).isEqualTo(22500.0);
        assertThat(resultado.stock()).isEqualTo(25);
    }

    @Test
    void eliminarBorraElBalonExistente() {
        Balon existente = new Balon(7L, "BAL-45", "Balon 45 kg", 29900.0, 10, 3);
        when(balonRepository.findById(7L)).thenReturn(Optional.of(existente));

        balonService.eliminar(7L);

        verify(balonRepository).delete(existente);
    }

    @Test
    void eliminarLanza404SiElBalonNoExiste() {
        when(balonRepository.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> balonService.eliminar(99L));
    }
}
