package pe.edu.cibertec.gasmapocho.msbalones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record BalonRequestDTO(
        @NotBlank @Size(max = 20) String codigo,
        @NotBlank @Size(max = 100) String nombre,
        @NotNull @PositiveOrZero Double precio,
        @NotNull @PositiveOrZero Integer stock,
        @NotNull @PositiveOrZero Integer stockMinimo) {
}
