package pe.edu.cibertec.gasmapocho.msreportes.dto;

import java.time.LocalDateTime;

public record MovimientoDTO(
        Long id,
        String folio,
        String tipo,
        String balonCodigo,
        String balonNombre,
        Integer cantidad,
        Double precioUnitario,
        String responsable,
        String observacion,
        LocalDateTime fecha,
        String estado) {
}
