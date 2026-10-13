package pe.edu.cibertec.gasmapocho.msbalones;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import pe.edu.cibertec.gasmapocho.msbalones.model.Balon;
import pe.edu.cibertec.gasmapocho.msbalones.repository.BalonRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BalonRepository balonRepository;

    public DataSeeder(BalonRepository balonRepository) {
        this.balonRepository = balonRepository;
    }

    @Override
    public void run(String... args) {
        if (balonRepository.count() > 0) {
            return;
        }
        balonRepository.save(new Balon(null, "BAL-5", "Balon de gas 5 kg", 14500.0, 40, 10));
        balonRepository.save(new Balon(null, "BAL-11", "Balon de gas 11 kg", 21000.0, 30, 8));
        balonRepository.save(new Balon(null, "BAL-15", "Balon de gas 15 kg", 28900.0, 20, 5));
        balonRepository.save(new Balon(null, "BAL-45", "Balon de gas 45 kg", 29900.0, 10, 3));
    }
}
