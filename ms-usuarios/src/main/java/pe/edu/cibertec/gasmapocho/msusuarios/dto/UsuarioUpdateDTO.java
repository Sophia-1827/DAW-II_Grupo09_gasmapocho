package pe.edu.cibertec.gasmapocho.msusuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import pe.edu.cibertec.gasmapocho.msusuarios.model.Rol;

public record UsuarioUpdateDTO(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotNull Rol rol) {
}
