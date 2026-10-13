package pe.edu.cibertec.gasmapocho.msmovimientos.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.service.MovimientoService;

// ADMIN y ALMACEN pueden ver y registrar movimientos (ver cuadro de permisos del informe).
@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {

    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public List<MovimientoDTO> listar() {
        return movimientoService.listar();
    }

    @PostMapping
    public MovimientoDTO registrar(@Valid @RequestBody MovimientoRequestDTO request) {
        return movimientoService.registrar(request);
    }
}
