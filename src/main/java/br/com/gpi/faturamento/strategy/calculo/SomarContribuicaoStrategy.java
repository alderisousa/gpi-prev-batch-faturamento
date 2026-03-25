package br.com.gpi.faturamento.strategy.calculo;

import br.com.gpi.faturamento.domain.ArrecadacaoCarga;
import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.repository.ArrecadacaoCargaRepository;
import br.com.gpi.faturamento.strategy.calculo.vo.FaixaTempoPlanoVO;
import br.com.gpi.faturamento.strategy.calculo.vo.FiltroParametroSomarContribuicaoVO;
import br.com.gpi.faturamento.strategy.calculo.vo.ParametroSomarContribuicaoVO;
import br.com.gpi.faturamento.utils.DateUtils;
import br.com.gpi.faturamento.utils.NumberUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("SOMA_CONTRIBUICAO")
@RequiredArgsConstructor
public class SomarContribuicaoStrategy implements RotinaCalculoStrategy {

    private final ArrecadacaoCargaRepository arrecadacaoCargaRepository;

    private final Map<ChaveArrecadacaoCarga, List<ArrecadacaoCarga>> cargasPorCompetencia = new ConcurrentHashMap<>();

    @Override
    public BigDecimal executar(List<ArrecadacaoMovimento> movimentos, DadosParticipante dados) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            ParametroSomarContribuicaoVO parametro = mapper.readValue(dados.getJsonParametroCalculo(), ParametroSomarContribuicaoVO.class);

            List<Long> idsContribuicao = getIdsContribuicao(dados, movimentos, parametro);
            List<Long> idsTipoContribuicao = parametro.getIdsTipoContribuicao();

            if (parametro.isSomaValorRecebido()) {
                List<ArrecadacaoCarga> cargas = listarCargasEmCache(dados);
                dados.setSomaValorRecebido(valorParaSoma(dados, idsContribuicao, cargas));
            }

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
            BigDecimal contribuicao = NumberUtils.multiplicarValor(somaContribuicao, getPercentual(dados, parametro), parametro.getQtDecimais());
            BigDecimal contribuicaoFinal = aplicarFaixaContribuicao(contribuicao, parametro, dados);
            return aplicarRegraValorMaximo(contribuicaoFinal, parametro.getVlMaximoContribuicao());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private List<ArrecadacaoCarga> listarCargasEmCache(DadosParticipante dados) {
        if (dados.getIdPessoaJuridica() == null || dados.getDataCompetencia() == null) {
            return Collections.emptyList();
        }
        LocalDate comp = dados.getDataCompetencia();
        String anoReferencia = String.valueOf(comp.getYear());
        String mesReferencia = String.format("%02d", comp.getMonthValue());
        ChaveArrecadacaoCarga chave = new ChaveArrecadacaoCarga(dados.getIdPessoaJuridica(), anoReferencia, mesReferencia);
        return cargasPorCompetencia.computeIfAbsent(chave,
                k -> arrecadacaoCargaRepository.listar(k.idPessoaJuridica(), k.mesReferencia(), k.anoReferencia()));
    }

    private BigDecimal valorParaSoma(DadosParticipante dados, List<Long> idsContribuicao, List<ArrecadacaoCarga> cargas) {
        return cargas.stream()
                .filter(c -> c.getIdPessoaPlano().equals(dados.getIdPessoaPlano())
                        && idsContribuicao.contains(c.getIdContribuicao()))
                .map(ArrecadacaoCarga::getValorRecebido)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal aplicarRegraValorMaximo(BigDecimal contribuicaoFinal, BigDecimal vlMaximoContribuicao) {
        if (vlMaximoContribuicao == null || vlMaximoContribuicao.compareTo(contribuicaoFinal) > 0) {
            return contribuicaoFinal;
        }
        return vlMaximoContribuicao;
    }

    private BigDecimal aplicarFaixaContribuicao(BigDecimal contribuicao, ParametroSomarContribuicaoVO parametro, DadosParticipante dados) {
        if (parametro.getFaixasContribuicoes() != null && !parametro.getFaixasContribuicoes().isEmpty()) {
            BigDecimal contribuicaoTotal = BigDecimal.ZERO;
            BigDecimal auxiliar = BigDecimal.ZERO;
            for (var faixa : parametro.getFaixasContribuicoes()) {
                if (auxiliar.compareTo(contribuicao) < 0) {
                    contribuicaoTotal = contribuicaoTotal.add(
                            NumberUtils.multiplicarValor(NumberUtils.min(faixa.getVlFaixaMax(), contribuicao).subtract(auxiliar),
                                    NumberUtils.percentualParaTaxa(faixa.getPercentual()), parametro.getQtDecimais()));
                    auxiliar = faixa.getVlFaixaMax();
                }
            }
            return contribuicaoTotal;
        } else if (parametro.getFaixasTempoPlano() != null && !parametro.getFaixasTempoPlano().isEmpty()) {
            BigDecimal percentual = getPercentualPorFaixa(dados, parametro.getFaixasTempoPlano());
            return NumberUtils.multiplicarValor(contribuicao, percentual);
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

    private BigDecimal getPercentualPorFaixa(DadosParticipante dados, List<FaixaTempoPlanoVO> faixas) {
        long tempoPlano = DateUtils.calculaAno(dados.getDataOpcao(), dados.getDataCompetencia());
        for (var faixa: faixas) {
            if (tempoPlano >= faixa.getTempoPlanoMinimo() && tempoPlano < faixa.getTempoPlanoMaximo()) {
                return NumberUtils.percentualParaTaxa(faixa.getPercentual());
            }
        }
        return NumberUtils.percentualParaTaxa(BigDecimal.ONE);
    }

    private record ChaveArrecadacaoCarga(Long idPessoaJuridica, String anoReferencia, String mesReferencia) {
    }

}
