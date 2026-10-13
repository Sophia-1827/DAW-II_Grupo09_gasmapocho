package pe.edu.cibertec.gasmapocho.msreportes.dto;

public record ConsolidadoBalonDTO(
        String codigo,
        String nombre,
        Integer totalEntradas,
        Integer totalSalidas,
        Integer unidadesNetas,
        Double montoVentas) {
}
