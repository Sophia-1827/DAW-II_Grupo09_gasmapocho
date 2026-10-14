package pe.edu.cibertec.gasmapocho.msusuarios;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import pe.edu.cibertec.gasmapocho.msusuarios.model.Rol;
import pe.edu.cibertec.gasmapocho.msusuarios.model.Usuario;
import pe.edu.cibertec.gasmapocho.msusuarios.repository.UsuarioRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.save(new Usuario(null, "Administrador Gas Mapocho", "admin@gasmapocho.cl",
                passwordEncoder.encode("Admin123"), Rol.ADMIN, true));
        usuarioRepository.save(new Usuario(null, "Personal de Almacen", "almacen@gasmapocho.cl",
                passwordEncoder.encode("Almacen123"), Rol.ALMACEN, true));
    }
}
