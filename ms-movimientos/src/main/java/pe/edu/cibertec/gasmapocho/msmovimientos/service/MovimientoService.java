package pe.edu.cibertec.gasmapocho.msmovimientos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msmovimientos.client.BalonClient;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoEvento;
import pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoPublicador;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Movimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.MovimientoRepository;

import feign.FeignException;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final BalonClient balonClient;
    private final MovimientoPublicador publicador;

    public MovimientoService(MovimientoRepository movimientoRepository, BalonClient balonClient,
            MovimientoPublicador publicador) {
        this.movimientoRepository = movimientoRepository;
        this.balonClient = balonClient;
        this.publicador = publicador;
    }

    public List<MovimientoDTO> listar() {
        return movimientoRepository.findAllByOrderByFechaDesc().stream().map(this::toDTO).toList();
    }

    public MovimientoDTO registrar(MovimientoRequestDTO request) {
        BalonDTO balon = buscarBalonOLanzar(request.balonCodigo());

        if (request.tipo() == TipoMovimiento.SALIDA && balon.stock() < request.cantidad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente: hay " + balon.stock() + " unidades de " + balon.codigo());
        }

        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(request.tipo());
        movimiento.setBalonCodigo(balon.codigo());
        movimiento.setBalonNombre(balon.nombre());
        movimiento.setCantidad(request.cantidad());
        movimiento.setPrecioUnitario(balon.precio());
        movimiento.setResponsable(request.responsable().trim());
        movimiento.setObservacion(request.observacion() != null ? request.observacion().trim() : null);
        movimiento.setFecha(LocalDateTime.now());
        movimiento.setFolio("TEMP");
        movimiento = movimientoRepository.save(movimiento);

        movimiento.setFolio("MOV-" + String.format("%05d", movimiento.getId()));
        movimiento = movimientoRepository.save(movimiento);

        publicador.publicar(new MovimientoEvento(balon.codigo(), request.tipo().name(), request.cantidad()));

        return toDTO(movimiento);
    }

    private BalonDTO buscarBalonOLanzar(String codigo) {
        try {
            return balonClient.obtenerPorCodigo(codigo);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Balon no encontrado: " + codigo);
        } catch (FeignException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "ms-balones no responde");
        }
    }

    private MovimientoDTO toDTO(Movimiento m) {
        return new MovimientoDTO(m.getId(), m.getFolio(), m.getTipo(), m.getBalonCodigo(), m.getBalonNombre(),
                m.getCantidad(), m.getPrecioUnitario(), m.getResponsable(), m.getObservacion(), m.getFecha());
    }
}
