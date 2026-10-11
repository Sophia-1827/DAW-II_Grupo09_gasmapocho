package pe.edu.cibertec.gasmapocho.msusuarios.dto;

import pe.edu.cibertec.gasmapocho.msusuarios.model.Rol;

public record LoginResponseDTO(String token, String nombre, Rol rol) {
}
