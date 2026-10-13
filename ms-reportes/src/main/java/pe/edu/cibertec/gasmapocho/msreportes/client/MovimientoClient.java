package pe.edu.cibertec.gasmapocho.msreportes.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import pe.edu.cibertec.gasmapocho.msreportes.dto.MovimientoDTO;

@FeignClient(name = "ms-movimientos", configuration = FeignAuthConfig.class)
public interface MovimientoClient {

    @GetMapping("/api/movimientos")
    List<MovimientoDTO> listar();
}
