package br.com.gpi.faturamento.service;

import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.domain.PlanoProcessamentoErro;
import br.com.gpi.faturamento.enums.StatusProcessamento;
import br.com.gpi.faturamento.repository.PlanoProcessamentoErroRepository;
import br.com.gpi.faturamento.repository.PlanoProcessamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class PlanoProcessamentoService {

    @Autowired
    private PlanoProcessamentoRepository planoProcessamentoRepository;
    @Autowired
    private PlanoProcessamentoErroRepository planoProcessamentoErroRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizarParaEmProcessamento(PlanoProcessamento item) {
        item.setIdStatus(StatusProcessamento.EM_PROCESSAMENTO.getId());
        item.setDataCadastro(LocalDateTime.now());
        planoProcessamentoRepository.save(item);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizarParaProcessadoComErro(PlanoProcessamento item, Exception ex) {
        item.setIdStatus(StatusProcessamento.PROCESSADO_ERRO.getId());
        item.setDataProcessamento(LocalDateTime.now());
        planoProcessamentoRepository.save(item);

        planoProcessamentoErroRepository.save(PlanoProcessamentoErro.builder()
                .idPlanoProcessamento(item.getId())
                .tipoExcecao(ex.getClass().getName())
                .descricao(ex.getMessage())
                .stack(Arrays.toString(ex.getStackTrace()))
                .tipoExcecaoCausa(ex.getCause() != null ? ex.getCause().getClass().getName() : null)
                .descricaoCausa(ex.getCause() != null ? ex.getCause().getMessage() : null)
                .stackCausa(ex.getCause() != null ? Arrays.toString(ex.getCause().getStackTrace()) : null)
                .build());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void atualizarParaProcessado(PlanoProcessamento item) {
        item.setIdStatus(StatusProcessamento.PROCESSADO.getId());
        item.setDataProcessamento(LocalDateTime.now());
        planoProcessamentoRepository.save(item);
    }
}
