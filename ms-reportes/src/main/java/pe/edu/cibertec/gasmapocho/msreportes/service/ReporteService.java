package pe.edu.cibertec.gasmapocho.msreportes.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import pe.edu.cibertec.gasmapocho.msreportes.client.BalonClient;
import pe.edu.cibertec.gasmapocho.msreportes.client.MovimientoClient;
import pe.edu.cibertec.gasmapocho.msreportes.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msreportes.dto.ConsolidadoBalonDTO;
import pe.edu.cibertec.gasmapocho.msreportes.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msreportes.dto.StockActualItemDTO;

@Service
public class ReporteService {

    // Un movimiento anulado ya no afecta el stock, asi que no cuenta en los reportes.
    private static final String ANULADO = "ANULADO";

    private final BalonClient balonClient;
    private final MovimientoClient movimientoClient;

    public ReporteService(BalonClient balonClient, MovimientoClient movimientoClient) {
        this.balonClient = balonClient;
        this.movimientoClient = movimientoClient;
    }

    public List<StockActualItemDTO> stockActual() {
        return balonClient.listar().stream()
                .map(b -> new StockActualItemDTO(b.codigo(), b.nombre(), b.stock(), b.stockMinimo(),
                        b.stock() <= b.stockMinimo()))
                .toList();
    }

    public List<MovimientoDTO> movimientosDia() {
        LocalDate hoy = LocalDate.now();
        return movimientoClient.listar().stream()
                .filter(m -> !ANULADO.equals(m.estado()))
                .filter(m -> m.fecha().toLocalDate().isEqual(hoy))
                .sorted(Comparator.comparing(MovimientoDTO::fecha).reversed())
                .toList();
    }

    public List<ConsolidadoBalonDTO> consolidadoMensual() {
        LocalDate ahora = LocalDate.now();
        List<BalonDTO> balones = balonClient.listar();
        List<MovimientoDTO> movimientosDelMes = movimientoClient.listar().stream()
                .filter(m -> !ANULADO.equals(m.estado()))
                .filter(m -> m.fecha().getYear() == ahora.getYear() && m.fecha().getMonthValue() == ahora.getMonthValue())
                .toList();

        Map<String, ConsolidadoAcumulador> acumulado = new LinkedHashMap<>();
        for (BalonDTO balon : balones) {
            acumulado.put(balon.codigo(), new ConsolidadoAcumulador(balon.nombre()));
        }
        for (MovimientoDTO m : movimientosDelMes) {
            ConsolidadoAcumulador acc = acumulado.computeIfAbsent(m.balonCodigo(),
                    k -> new ConsolidadoAcumulador(m.balonNombre()));
            if ("ENTRADA".equals(m.tipo())) {
                acc.entradas += m.cantidad();
            } else {
                acc.salidas += m.cantidad();
                acc.montoVentas += m.cantidad() * m.precioUnitario();
            }
        }

        return acumulado.entrySet().stream()
                .map(e -> new ConsolidadoBalonDTO(e.getKey(), e.getValue().nombre, e.getValue().entradas,
                        e.getValue().salidas, e.getValue().entradas - e.getValue().salidas, e.getValue().montoVentas))
                .toList();
    }

    private static class ConsolidadoAcumulador {
        final String nombre;
        int entradas = 0;
        int salidas = 0;
        double montoVentas = 0;

        ConsolidadoAcumulador(String nombre) {
            this.nombre = nombre;
        }
    }
}
