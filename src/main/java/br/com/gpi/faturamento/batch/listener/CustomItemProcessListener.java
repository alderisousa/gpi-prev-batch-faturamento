package br.com.gpi.faturamento.batch.listener;

import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.service.ArrecadacaoService;
import br.com.gpi.faturamento.service.PlanoProcessamentoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomItemProcessListener implements ItemProcessListener<PlanoProcessamento, PlanoProcessamento> {

    @Autowired
    private PlanoProcessamentoService planoProcessamentoService;

    @Autowired
    private ArrecadacaoService arrecadacaoService;

    @Override
    public void beforeProcess(PlanoProcessamento item) {
        arrecadacaoService.limparProcessamento(item);
        planoProcessamentoService.atualizarParaEmProcessamento(item);
    }

    @Override
    public void onProcessError(PlanoProcessamento item, Exception e) {
        planoProcessamentoService.atualizarParaPendente(item);
    }
}
