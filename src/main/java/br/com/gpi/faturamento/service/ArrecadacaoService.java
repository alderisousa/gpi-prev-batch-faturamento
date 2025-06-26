package br.com.gpi.faturamento.service;

import br.com.gpi.faturamento.domain.PlanoProcessamento;
import br.com.gpi.faturamento.repository.ArrecadacaoMovimentoRepository;
import br.com.gpi.faturamento.repository.ArrecadacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArrecadacaoService {

    @Autowired
    private ArrecadacaoRepository arrecadacaoRepository;

    @Autowired
    private ArrecadacaoMovimentoRepository arrecadacaoMovimentoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void limparProcessamento(PlanoProcessamento item) {
//        arrecadacaoMovimentoRepository.deleteMovimentoPorCompetencia(
//                item.getIdPessoaJuridica(),
//                item.getMesReferencia(),
//                item.getAnoReferencia());
//        arrecadacaoRepository.deleteArrecadacoesPorCompetencia(
//                item.getIdPessoaJuridica(),
//                item.getMesReferencia(),
//                item.getAnoReferencia());
    }
}
