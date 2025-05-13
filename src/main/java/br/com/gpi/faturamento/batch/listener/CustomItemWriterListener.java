package br.com.gpi.faturamento.batch.listener;

import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.service.PlanoProcessamentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomItemWriterListener implements ItemWriteListener<PlanoProcessamento> {

    @Autowired
    private PlanoProcessamentoService service;

    @Override
    public void onWriteError(Exception exception, Chunk<? extends PlanoProcessamento> item) {
        log.error(exception.getMessage(), exception.getCause());
        for (var planoProcessamento : item.getItems()) {
            service.atualizarParaPendente(planoProcessamento);
        }
    }
}
