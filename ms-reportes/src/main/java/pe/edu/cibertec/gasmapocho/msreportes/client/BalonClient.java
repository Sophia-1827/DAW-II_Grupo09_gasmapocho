package pe.edu.cibertec.gasmapocho.msreportes.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import pe.edu.cibertec.gasmapocho.msreportes.dto.BalonDTO;

@FeignClient(name = "ms-balones", configuration = FeignAuthConfig.class)
public interface BalonClient {

    @GetMapping("/api/balones")
    List<BalonDTO> listar();
}
