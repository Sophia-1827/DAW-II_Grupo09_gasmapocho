package pe.edu.cibertec.gasmapocho.msreportes.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.cibertec.gasmapocho.msreportes.dto.ConsolidadoBalonDTO;
import pe.edu.cibertec.gasmapocho.msreportes.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msreportes.dto.StockActualItemDTO;
import pe.edu.cibertec.gasmapocho.msreportes.service.ReporteService;

// ADMIN y ALMACEN pueden ver los reportes (ver cuadro de permisos del informe).
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/stock-actual")
    public List<StockActualItemDTO> stockActual() {
        return reporteService.stockActual();
    }

    @GetMapping("/movimientos-dia")
    public List<MovimientoDTO> movimientosDia() {
        return reporteService.movimientosDia();
    }

    @GetMapping("/consolidado-mensual")
    public List<ConsolidadoBalonDTO> consolidadoMensual() {
        return reporteService.consolidadoMensual();
    }
}
