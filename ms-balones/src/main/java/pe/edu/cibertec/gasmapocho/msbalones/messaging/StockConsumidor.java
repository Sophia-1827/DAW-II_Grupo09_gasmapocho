package pe.edu.cibertec.gasmapocho.msbalones.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import pe.edu.cibertec.gasmapocho.msbalones.service.BalonService;

@Component
public class StockConsumidor {

    private final BalonService balonService;

    public StockConsumidor(BalonService balonService) {
        this.balonService = balonService;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_STOCK)
    public void onMovimientoRegistrado(MovimientoEvento evento) {
        int delta = "ENTRADA".equals(evento.tipo()) ? evento.cantidad() : -evento.cantidad();
        balonService.ajustarStock(evento.balonCodigo(), delta);
    }
}
