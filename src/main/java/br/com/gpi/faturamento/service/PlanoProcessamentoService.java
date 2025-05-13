package br.com.gpi.faturamento.service;

import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.enums.StatusProcessamento;
import br.com.gpi.faturamento.repository.PlanoProcessamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanoProcessamentoService {

    @Autowired
    private PlanoProcessamentoRepository planoProcessamentoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizarParaEmProcessamento(PlanoProcessamento item) {
        item.setIdStatus(StatusProcessamento.EM_PROCESSAMENTO.getId());
        planoProcessamentoRepository.save(item);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizarParaPendente(PlanoProcessamento item) {
        item.setIdStatus(StatusProcessamento.PENDENTE.getId());
        planoProcessamentoRepository.save(item);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizarParaProcessado(PlanoProcessamento item) {
        item.setIdStatus(StatusProcessamento.PROCESSADO.getId());
        planoProcessamentoRepository.save(item);
    }
}
