package pe.edu.cibertec.gasmapocho.msmovimientos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msmovimientos.client.BalonClient;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoPublicador;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Movimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.MovimientoRepository;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private BalonClient balonClient;

    @Mock
    private MovimientoPublicador publicador;

    @InjectMocks
    private MovimientoService movimientoService;

    @Test
    void listarDevuelveTodosLosMovimientosOrdenados() {
        Movimiento m1 = new Movimiento(1L, "MOV-00001", TipoMovimiento.ENTRADA, "BAL-5", "Balon 5 kg", 10, 14500.0,
                "Juan", null, LocalDateTime.now());
        when(movimientoRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(m1));

        List<MovimientoDTO> resultado = movimientoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).folio()).isEqualTo("MOV-00001");
    }

    @Test
    void registrarInsertaUnaEntradaYPublicaElEvento() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 40, 10);
        when(balonClient.obtenerPorCodigo("BAL-5")).thenReturn(balon);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(7L);
            }
            return m;
        });

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.ENTRADA, "BAL-5", 10, "Juan", null);
        MovimientoDTO resultado = movimientoService.registrar(request);

        assertThat(resultado.folio()).isEqualTo("MOV-00007");
        assertThat(resultado.balonNombre()).isEqualTo("Balon 5 kg");
        assertThat(resultado.precioUnitario()).isEqualTo(14500.0);
        verify(publicador).publicar(any());
    }

    @Test
    void registrarRechazaSalidaSinStockSuficiente() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 3, 10);
        when(balonClient.obtenerPorCodigo("BAL-5")).thenReturn(balon);

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 5, "Juan", null);

        org.junit.jupiter.api.Assertions.assertThrows(ResponseStatusException.class,
                () -> movimientoService.registrar(request));
    }

    @Test
    void registrarAceptaSalidaConStockSuficienteYPublicaElEvento() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 10, 2);
        when(balonClient.obtenerPorCodigo("BAL-5")).thenReturn(balon);
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(3L);
            }
            return m;
        });

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 5, "Ana", "Venta");
        MovimientoDTO resultado = movimientoService.registrar(request);

        assertThat(resultado.tipo()).isEqualTo(TipoMovimiento.SALIDA);
        verify(publicador).publicar(any());
    }
}
