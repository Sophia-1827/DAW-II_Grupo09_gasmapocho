package pe.edu.cibertec.gasmapocho.msusuarios.dto;

import pe.edu.cibertec.gasmapocho.msusuarios.model.Rol;

public record UsuarioDTO(Long id, String nombre, String email, Rol rol, Boolean activo) {
}
