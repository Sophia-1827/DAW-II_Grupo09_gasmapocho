package pe.edu.cibertec.gasmapocho.msreportes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pe.edu.cibertec.gasmapocho.msreportes.client.BalonClient;
import pe.edu.cibertec.gasmapocho.msreportes.client.MovimientoClient;
import pe.edu.cibertec.gasmapocho.msreportes.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msreportes.dto.ConsolidadoBalonDTO;
import pe.edu.cibertec.gasmapocho.msreportes.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msreportes.dto.StockActualItemDTO;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private BalonClient balonClient;

    @Mock
    private MovimientoClient movimientoClient;

    @InjectMocks
    private ReporteService reporteService;

    @Test
    void stockActualMarcaBajoMinimoCorrectamente() {
        when(balonClient.listar()).thenReturn(List.of(
                new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 8, 10),
                new BalonDTO(2L, "BAL-11", "Balon 11 kg", 21000.0, 30, 8)));

        List<StockActualItemDTO> resultado = reporteService.stockActual();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).bajoMinimo()).isTrue();
        assertThat(resultado.get(1).bajoMinimo()).isFalse();
    }

    @Test
    void movimientosDiaFiltraSoloLosDeHoy() {
        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime ayer = hoy.minusDays(1);
        when(movimientoClient.listar()).thenReturn(List.of(
                new MovimientoDTO(1L, "MOV-00001", "ENTRADA", "BAL-5", "Balon 5 kg", 10, 14500.0, "Juan", null, hoy, "REGISTRADO"),
                new MovimientoDTO(2L, "MOV-00002", "SALIDA", "BAL-11", "Balon 11 kg", 3, 21000.0, "Ana", null, ayer, "REGISTRADO")));

        List<MovimientoDTO> resultado = reporteService.movimientosDia();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).folio()).isEqualTo("MOV-00001");
    }

    @Test
    void losMovimientosAnuladosNoCuentanEnLosReportes() {
        LocalDateTime hoy = LocalDateTime.now();
        when(balonClient.listar()).thenReturn(List.of(new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 40, 10)));
        when(movimientoClient.listar()).thenReturn(List.of(
                new MovimientoDTO(1L, "MOV-00001", "ENTRADA", "BAL-5", "Balon 5 kg", 20, 14500.0, "Juan", null, hoy, "CONFIRMADO"),
                new MovimientoDTO(2L, "MOV-00002", "SALIDA", "BAL-5", "Balon 5 kg", 5, 14500.0, "Ana", null, hoy, "ANULADO")));

        assertThat(reporteService.movimientosDia()).extracting(MovimientoDTO::folio).containsExactly("MOV-00001");
        ConsolidadoBalonDTO c = reporteService.consolidadoMensual().get(0);
        assertThat(c.totalSalidas()).isZero();
        assertThat(c.montoVentas()).isZero();
    }

    @Test
    void consolidadoMensualSumaEntradasYSalidasPorBalon() {
        LocalDateTime hoy = LocalDateTime.now();
        when(balonClient.listar()).thenReturn(List.of(new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 40, 10)));
        when(movimientoClient.listar()).thenReturn(List.of(
                new MovimientoDTO(1L, "MOV-00001", "ENTRADA", "BAL-5", "Balon 5 kg", 20, 14500.0, "Juan", null, hoy, "REGISTRADO"),
                new MovimientoDTO(2L, "MOV-00002", "SALIDA", "BAL-5", "Balon 5 kg", 5, 14500.0, "Ana", null, hoy, "REGISTRADO")));

        List<ConsolidadoBalonDTO> resultado = reporteService.consolidadoMensual();

        assertThat(resultado).hasSize(1);
        ConsolidadoBalonDTO c = resultado.get(0);
        assertThat(c.totalEntradas()).isEqualTo(20);
        assertThat(c.totalSalidas()).isEqualTo(5);
        assertThat(c.unidadesNetas()).isEqualTo(15);
        assertThat(c.montoVentas()).isEqualTo(5 * 14500.0);
    }
}
