package pe.edu.cibertec.gasmapocho.msmovimientos.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msmovimientos.client.BalonClienteResiliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.client.ProveedorClienteResiliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.CompraRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ProveedorDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoEvento;
import pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoPublicador;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Cliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Movimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.ClienteRepository;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.MovimientoRepository;

import feign.FeignException;

@Service
public class MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final ClienteRepository clienteRepository;
    private final BalonClienteResiliente balonClienteResiliente;
    private final ProveedorClienteResiliente proveedorClienteResiliente;
    private final MovimientoPublicador publicador;

    public MovimientoService(MovimientoRepository movimientoRepository, ClienteRepository clienteRepository,
            BalonClienteResiliente balonClienteResiliente, ProveedorClienteResiliente proveedorClienteResiliente,
            MovimientoPublicador publicador) {
        this.movimientoRepository = movimientoRepository;
        this.clienteRepository = clienteRepository;
        this.balonClienteResiliente = balonClienteResiliente;
        this.proveedorClienteResiliente = proveedorClienteResiliente;
        this.publicador = publicador;
    }

    public List<MovimientoDTO> listar() {
        return movimientoRepository.findAllByOrderByFechaDesc().stream().map(this::toDTO).toList();
    }

    public List<MovimientoDTO> listarCompras(Long proveedorId) {
        List<Movimiento> compras = proveedorId != null
                ? movimientoRepository.findByProveedorIdOrderByFechaDesc(proveedorId)
                : movimientoRepository.findByProveedorIdIsNotNullOrderByFechaDesc();
        return compras.stream().map(this::toDTO).toList();
    }

    public MovimientoDTO registrar(MovimientoRequestDTO request) {
        Cliente cliente = buscarClienteParaVenta(request);
        BalonDTO balon = buscarBalonOLanzar(request.balonCodigo());

        if (request.tipo() == TipoMovimiento.SALIDA && balon.stock() < request.cantidad()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente: hay " + balon.stock() + " unidades de " + balon.codigo());
        }

        Movimiento movimiento = nuevoMovimiento(balon, request.tipo(), request.cantidad(), balon.precio(),
                request.responsable(), request.observacion());
        if (cliente != null) {
            movimiento.setClienteId(cliente.getId());
            movimiento.setClienteNombre(cliente.getNombre());
            movimiento.setClienteDireccion(cliente.getDireccion());
        }
        return guardarYPublicar(movimiento);
    }

    // El cliente es opcional y solo aplica a ventas (SALIDA); debe existir y estar activo.
    private Cliente buscarClienteParaVenta(MovimientoRequestDTO request) {
        if (request.clienteId() == null) {
            return null;
        }
        if (request.tipo() != TipoMovimiento.SALIDA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El cliente solo se asocia a ventas (SALIDA)");
        }
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Cliente no encontrado: " + request.clienteId()));
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El cliente " + cliente.getNombre() + " esta desactivado");
        }
        return cliente;
    }

    // Compra a proveedor: una ENTRADA asociada a un proveedor activo. El costo es opcional;
    // si no se indica se usa el precio del catalogo.
    public MovimientoDTO registrarCompra(CompraRequestDTO request) {
        ProveedorDTO proveedor = buscarProveedorOLanzar(request.proveedorId());
        if (!Boolean.TRUE.equals(proveedor.activo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El proveedor " + proveedor.nombre() + " esta desactivado");
        }
        BalonDTO balon = buscarBalonOLanzar(request.balonCodigo());

        double precio = request.precioUnitario() != null ? request.precioUnitario() : balon.precio();
        Movimiento movimiento = nuevoMovimiento(balon, TipoMovimiento.ENTRADA, request.cantidad(), precio,
                request.responsable(), request.observacion());
        movimiento.setProveedorId(proveedor.id());
        movimiento.setProveedorNombre(proveedor.nombre());
        return guardarYPublicar(movimiento);
    }

    private Movimiento nuevoMovimiento(BalonDTO balon, TipoMovimiento tipo, Integer cantidad, Double precio,
            String responsable, String observacion) {
        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(tipo);
        movimiento.setBalonCodigo(balon.codigo());
        movimiento.setBalonNombre(balon.nombre());
        movimiento.setCantidad(cantidad);
        movimiento.setPrecioUnitario(precio);
        movimiento.setResponsable(responsable.trim());
        movimiento.setObservacion(observacion != null ? observacion.trim() : null);
        movimiento.setFecha(LocalDateTime.now());
        return movimiento;
    }

    private MovimientoDTO guardarYPublicar(Movimiento movimiento) {
        movimiento.setFolio("TEMP");
        movimiento = movimientoRepository.save(movimiento);

        movimiento.setFolio("MOV-" + String.format("%05d", movimiento.getId()));
        movimiento = movimientoRepository.save(movimiento);

        publicador.publicar(new MovimientoEvento(movimiento.getBalonCodigo(), movimiento.getTipo().name(),
                movimiento.getCantidad()));

        return toDTO(movimiento);
    }

    private BalonDTO buscarBalonOLanzar(String codigo) {
        try {
            return balonClienteResiliente.obtenerPorCodigo(codigo);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Balon no encontrado: " + codigo);
        }
    }

    private ProveedorDTO buscarProveedorOLanzar(Long id) {
        try {
            return proveedorClienteResiliente.obtener(id);
        } catch (FeignException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado: " + id);
        }
    }

    private MovimientoDTO toDTO(Movimiento m) {
        return new MovimientoDTO(m.getId(), m.getFolio(), m.getTipo(), m.getBalonCodigo(), m.getBalonNombre(),
                m.getCantidad(), m.getPrecioUnitario(), m.getResponsable(), m.getObservacion(), m.getFecha(),
                m.getProveedorId(), m.getProveedorNombre(), m.getClienteId(), m.getClienteNombre(),
                m.getClienteDireccion());
    }
}
