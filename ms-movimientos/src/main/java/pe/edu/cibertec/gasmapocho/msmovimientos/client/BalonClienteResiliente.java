package pe.edu.cibertec.gasmapocho.msmovimientos.client;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.BalonDTO;

// El 404 (balon inexistente) no debe abrir el circuito: se ignora en application.properties
// (resilience4j.circuitbreaker.instances.balonClient.ignore-exceptions). Solo cuentan fallos
// reales de infraestructura (timeout, conexion rechazada, 5xx).
@Service
public class BalonClienteResiliente {

    private final BalonClient balonClient;

    public BalonClienteResiliente(BalonClient balonClient) {
        this.balonClient = balonClient;
    }

    @CircuitBreaker(name = "balonClient", fallbackMethod = "fallbackObtenerPorCodigo")
    public BalonDTO obtenerPorCodigo(String codigo) {
        return balonClient.obtenerPorCodigo(codigo);
    }

    private BalonDTO fallbackObtenerPorCodigo(String codigo, Throwable t) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "ms-balones no esta disponible en este momento, intenta de nuevo en unos segundos");
    }
}
