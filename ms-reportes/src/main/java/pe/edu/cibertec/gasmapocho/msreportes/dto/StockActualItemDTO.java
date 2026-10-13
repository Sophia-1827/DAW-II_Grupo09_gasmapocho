package pe.edu.cibertec.gasmapocho.msreportes.dto;

public record StockActualItemDTO(String codigo, String nombre, Integer stock, Integer stockMinimo, Boolean bajoMinimo) {
}
