package pe.edu.cibertec.gasmapocho.msmovimientos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import pe.edu.cibertec.gasmapocho.msmovimientos.client.BalonClienteResiliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.client.ProveedorClienteResiliente;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.BalonDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.CompraRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.MovimientoRequestDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.dto.ProveedorDTO;
import pe.edu.cibertec.gasmapocho.msmovimientos.messaging.MovimientoPublicador;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.Movimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.model.TipoMovimiento;
import pe.edu.cibertec.gasmapocho.msmovimientos.repository.MovimientoRepository;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private BalonClienteResiliente balonClienteResiliente;

    @Mock
    private ProveedorClienteResiliente proveedorClienteResiliente;

    @Mock
    private MovimientoPublicador publicador;

    @InjectMocks
    private MovimientoService movimientoService;

    @Test
    void listarDevuelveTodosLosMovimientosOrdenados() {
        Movimiento m1 = new Movimiento(1L, "MOV-00001", TipoMovimiento.ENTRADA, "BAL-5", "Balon 5 kg", 10, 14500.0,
                "Juan", null, LocalDateTime.now(), null, null);
        when(movimientoRepository.findAllByOrderByFechaDesc()).thenReturn(List.of(m1));

        List<MovimientoDTO> resultado = movimientoService.listar();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).folio()).isEqualTo("MOV-00001");
    }

    @Test
    void registrarInsertaUnaEntradaYPublicaElEvento() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 40, 10);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5")).thenReturn(balon);
        simularGuardado(7L);

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.ENTRADA, "BAL-5", 10, "Juan", null);
        MovimientoDTO resultado = movimientoService.registrar(request);

        assertThat(resultado.folio()).isEqualTo("MOV-00007");
        assertThat(resultado.balonNombre()).isEqualTo("Balon 5 kg");
        assertThat(resultado.precioUnitario()).isEqualTo(14500.0);
        verify(publicador).publicar(any());
    }

    @Test
    void registrarRechazaSalidaSinStockSuficiente() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 3, 10);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5")).thenReturn(balon);

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 5, "Juan", null);

        assertThrows(ResponseStatusException.class, () -> movimientoService.registrar(request));
    }

    @Test
    void registrarAceptaSalidaConStockSuficienteYPublicaElEvento() {
        BalonDTO balon = new BalonDTO(1L, "BAL-5", "Balon 5 kg", 14500.0, 10, 2);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-5")).thenReturn(balon);
        simularGuardado(3L);

        MovimientoRequestDTO request = new MovimientoRequestDTO(TipoMovimiento.SALIDA, "BAL-5", 5, "Ana", "Venta");
        MovimientoDTO resultado = movimientoService.registrar(request);

        assertThat(resultado.tipo()).isEqualTo(TipoMovimiento.SALIDA);
        verify(publicador).publicar(any());
    }

    @Test
    void registrarCompraCreaUnaEntradaConElProveedorYPublicaElEvento() {
        ProveedorDTO proveedor = new ProveedorDTO(2L, "Pacifico Ltda.", "77.208.915-6", "123", "Valparaiso",
                "Valparaiso", true);
        BalonDTO balon = new BalonDTO(1L, "BAL-11", "Balon 11 kg", 21000.0, 30, 8);
        when(proveedorClienteResiliente.obtener(2L)).thenReturn(proveedor);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-11")).thenReturn(balon);
        simularGuardado(9L);

        CompraRequestDTO request = new CompraRequestDTO(2L, "BAL-11", 20, 18000.0, "Ana", "Pedido semanal");
        MovimientoDTO resultado = movimientoService.registrarCompra(request);

        assertThat(resultado.tipo()).isEqualTo(TipoMovimiento.ENTRADA);
        assertThat(resultado.proveedorId()).isEqualTo(2L);
        assertThat(resultado.proveedorNombre()).isEqualTo("Pacifico Ltda.");
        assertThat(resultado.precioUnitario()).isEqualTo(18000.0);
        verify(publicador).publicar(any());
    }

    @Test
    void registrarCompraUsaElPrecioDelCatalogoSiNoSeIndicaCosto() {
        ProveedorDTO proveedor = new ProveedorDTO(2L, "Pacifico Ltda.", "77.208.915-6", "123", "Valparaiso",
                "Valparaiso", true);
        BalonDTO balon = new BalonDTO(1L, "BAL-11", "Balon 11 kg", 21000.0, 30, 8);
        when(proveedorClienteResiliente.obtener(2L)).thenReturn(proveedor);
        when(balonClienteResiliente.obtenerPorCodigo("BAL-11")).thenReturn(balon);
        simularGuardado(10L);

        MovimientoDTO resultado = movimientoService
                .registrarCompra(new CompraRequestDTO(2L, "BAL-11", 5, null, "Ana", null));

        assertThat(resultado.precioUnitario()).isEqualTo(21000.0);
    }

    @Test
    void registrarCompraRechazaUnProveedorDesactivado() {
        ProveedorDTO inactivo = new ProveedorDTO(3L, "Sur Gas", "96.784.120-3", "123", "Coronel", "Biobio", false);
        when(proveedorClienteResiliente.obtener(3L)).thenReturn(inactivo);

        CompraRequestDTO request = new CompraRequestDTO(3L, "BAL-5", 5, null, "Ana", null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> movimientoService.registrarCompra(request));
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        verify(publicador, never()).publicar(any());
    }

    @Test
    void listarComprasFiltraPorProveedorCuandoSeIndica() {
        Movimiento compra = new Movimiento(4L, "MOV-00004", TipoMovimiento.ENTRADA, "BAL-5", "Balon 5 kg", 10,
                14000.0, "Ana", null, LocalDateTime.now(), 2L, "Pacifico Ltda.");
        when(movimientoRepository.findByProveedorIdOrderByFechaDesc(2L)).thenReturn(List.of(compra));

        List<MovimientoDTO> resultado = movimientoService.listarCompras(2L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).proveedorNombre()).isEqualTo("Pacifico Ltda.");
    }

    @Test
    void listarComprasSinFiltroDevuelveTodasLasComprasAProveedores() {
        when(movimientoRepository.findByProveedorIdIsNotNullOrderByFechaDesc()).thenReturn(List.of());

        assertThat(movimientoService.listarCompras(null)).isEmpty();
        verify(movimientoRepository).findByProveedorIdIsNotNullOrderByFechaDesc();
    }

    private void simularGuardado(Long id) {
        when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            if (m.getId() == null) {
                m.setId(id);
            }
            return m;
        });
    }
}
