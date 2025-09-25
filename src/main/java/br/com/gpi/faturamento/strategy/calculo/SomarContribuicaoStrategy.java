package br.com.gpi.faturamento.strategy.calculo;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.strategy.calculo.vo.FiltroParametroSomarContribuicaoVO;
import br.com.gpi.faturamento.strategy.calculo.vo.ParametroSomarContribuicaoVO;
import br.com.gpi.faturamento.utils.NumberUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component("SOMA_CONTRIBUICAO")
public class SomarContribuicaoStrategy implements RotinaCalculoStrategy {

    @Override
    public BigDecimal executar(List<ArrecadacaoMovimento> movimentos, DadosParticipante dados) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            ParametroSomarContribuicaoVO parametro = mapper.readValue(dados.getJsonParametroCalculo(), ParametroSomarContribuicaoVO.class);

            List<Long> idsContribuicao = getIdsContribuicao(dados, movimentos, parametro);
            List<Long> idsTipoContribuicao = parametro.getIdsTipoContribuicao();

            List<ArrecadacaoMovimento> movimentosAux = movimentos.stream()
                    .filter(m ->
                            (
                                (idsContribuicao != null && idsContribuicao.contains(m.getIdContribuicao())) ||
                                (idsTipoContribuicao != null && idsTipoContribuicao.contains(m.getIdTipoContribuicao()))
                            )
                            && dados.getIdPessoaPlano().equals(m.getIdPessoaPlano()))
                    .toList();

            BigDecimal somaContribuicao = movimentosAux.stream()
                    .map(ArrecadacaoMovimento::getValorCalculado)
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);
            BigDecimal contribuicao = NumberUtils.multiplicarValor(somaContribuicao, getPercentual(dados, parametro));
            return aplicarFaixaContribuicao(contribuicao, parametro);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private BigDecimal aplicarFaixaContribuicao(BigDecimal contribuicao, ParametroSomarContribuicaoVO parametro) {
        if (parametro.getFaixasContribuicoes() != null && !parametro.getFaixasContribuicoes().isEmpty()) {
            BigDecimal contribuicaoTotal = BigDecimal.ZERO;
            BigDecimal auxiliar = BigDecimal.ZERO;
            for (var faixa : parametro.getFaixasContribuicoes()) {
                if (auxiliar.compareTo(contribuicao) < 0) {
                    contribuicaoTotal = contribuicaoTotal.add(
                            NumberUtils.multiplicarValor(NumberUtils.min(faixa.getVlFaixaMax(), contribuicao).subtract(auxiliar),
                                    NumberUtils.percentualParaTaxa(faixa.getPercentual())));
                    auxiliar = faixa.getVlFaixaMax();
                }
            }
            return contribuicaoTotal;
        }
        return contribuicao;
    }

    private List<Long> getIdsContribuicao(DadosParticipante dados, List<ArrecadacaoMovimento> movimentos, ParametroSomarContribuicaoVO parametro) {
        if (parametro.getFiltros() == null || parametro.getFiltros().isEmpty()) {
            return parametro.getIdsContribuicao();
        }

        List<Long> ids = new ArrayList<>();
        for (FiltroParametroSomarContribuicaoVO filtro : parametro.getFiltros()) {
            if (filtro.getFuncao() == null) {
                ids.addAll(filtro.getIdsContribuicao());
            } else {
                List<ArrecadacaoMovimento> movimentosAux = movimentos.stream()
                        .filter(m -> filtro.getIdsContribuicao().contains(m.getIdContribuicao())
                                && dados.getIdPessoaPlano().equals(m.getIdPessoaPlano()))
                        .toList();

                switch (filtro.getFuncao()) {
                    case "MIN":
                        ids.add(getIdMinContribuicao(movimentosAux));
                        break;
                    case "MAX":
                        ids.add(getIdMaxContribuicao(movimentosAux));
                        break;
                }
            }
        }
        return ids;
    }

    private Long getIdMinContribuicao(List<ArrecadacaoMovimento> movimentos) {

        ArrecadacaoMovimento aux = null;
        for (ArrecadacaoMovimento mov : movimentos) {
            if (aux == null) {
                aux = mov;
            } else {
                if (aux.getValorCalculado().compareTo(mov.getValorCalculado()) > 0) {
                    aux = mov;
                }
            }
        }

        return aux == null ? null : aux.getIdContribuicao();
    }

    private Long getIdMaxContribuicao(List<ArrecadacaoMovimento> movimentos) {

        ArrecadacaoMovimento aux = null;
        for (ArrecadacaoMovimento mov : movimentos) {
            if (aux == null) {
                aux = mov;
            } else {
                if (aux.getValorCalculado().compareTo(mov.getValorCalculado()) < 0) {
                    aux = mov;
                }
            }
        }

        return aux == null ? null : aux.getIdContribuicao();
    }

    private BigDecimal getPercentual(DadosParticipante dados, ParametroSomarContribuicaoVO parametro) {
        if (parametro.isAplicaPercentual()) {
            return NumberUtils.percentualParaTaxa(dados.getPctContribuicao());
        }
        return BigDecimal.ONE;
    }

}
