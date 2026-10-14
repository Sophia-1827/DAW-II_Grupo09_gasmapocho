package pe.edu.cibertec.gasmapocho.msbalones;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import pe.edu.cibertec.gasmapocho.msbalones.model.Balon;
import pe.edu.cibertec.gasmapocho.msbalones.model.Proveedor;
import pe.edu.cibertec.gasmapocho.msbalones.repository.BalonRepository;
import pe.edu.cibertec.gasmapocho.msbalones.repository.ProveedorRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BalonRepository balonRepository;
    private final ProveedorRepository proveedorRepository;

    public DataSeeder(BalonRepository balonRepository, ProveedorRepository proveedorRepository) {
        this.balonRepository = balonRepository;
        this.proveedorRepository = proveedorRepository;
    }

    @Override
    public void run(String... args) {
        sembrarBalones();
        sembrarProveedores();
    }

    private void sembrarBalones() {
        if (balonRepository.count() > 0) {
            return;
        }
        balonRepository.save(new Balon(null, "BAL-5", "Balon de gas 5 kg", 14500.0, 40, 10));
        balonRepository.save(new Balon(null, "BAL-11", "Balon de gas 11 kg", 21000.0, 30, 8));
        balonRepository.save(new Balon(null, "BAL-15", "Balon de gas 15 kg", 28900.0, 20, 5));
        balonRepository.save(new Balon(null, "BAL-45", "Balon de gas 45 kg", 29900.0, 10, 3));
    }

    // Tres proveedores predeterminados: el formulario de compra los ofrece para autocompletar.
    private void sembrarProveedores() {
        if (proveedorRepository.count() > 0) {
            return;
        }
        proveedorRepository.save(new Proveedor(null, "Gas Andes Distribuciones SpA", "76.512.340-1",
                "+56 2 2765 4410", "Av. Pajaritos 3450, Maipu", "Region Metropolitana", true));
        proveedorRepository.save(new Proveedor(null, "Combustibles del Pacifico Ltda.", "77.208.915-6",
                "+56 32 258 7730", "Calle Errazuriz 1280, Valparaiso", "Valparaiso", true));
        proveedorRepository.save(new Proveedor(null, "Envasadora Sur Gas S.A.", "96.784.120-3",
                "+56 41 291 6605", "Ruta 160 Km 5, Coronel", "Biobio", true));
    }
}
