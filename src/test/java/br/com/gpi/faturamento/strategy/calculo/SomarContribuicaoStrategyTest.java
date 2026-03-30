package br.com.gpi.faturamento.strategy.calculo;

import br.com.gpi.faturamento.domain.ArrecadacaoMovimento;
import br.com.gpi.faturamento.domain.DadosParticipante;
import br.com.gpi.faturamento.repository.ArrecadacaoCargaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SomarContribuicaoStrategyTest {

    @Mock
    private ArrecadacaoCargaRepository arrecadacaoCargaRepository;

    @InjectMocks
    private SomarContribuicaoStrategy somarContribuicaoStrategy;

    @Test
    void listarChamadoUmaVezQuandoExecutarDuasVezesComMesmaCompetenciaEPessoaJuridica() {
        when(arrecadacaoCargaRepository.listar(eq(10L), eq("03"), eq("2024")))
                .thenReturn(Collections.emptyList());

        DadosParticipante dados = DadosParticipante.builder()
                .idPessoaJuridica(10L)
                .idPessoaPlano(1L)
                .dataCompetencia(LocalDate.of(2024, 3, 31))
                .jsonParametroCalculo("{\"idsContribuicao\":[100],\"qtDecimais\":2,\"aplicaPercentual\":false}")
                .pctContribuicao(BigDecimal.TEN)
                .build();

        ArrecadacaoMovimento mov = ArrecadacaoMovimento.builder()
                .idContribuicao(100L)
                .idPessoaPlano(1L)
                .valorCalculado(new BigDecimal("50.00"))
                .build();

        List<ArrecadacaoMovimento> movimentos = List.of(mov);

        BigDecimal r1 = somarContribuicaoStrategy.executar(movimentos, dados);
        BigDecimal r2 = somarContribuicaoStrategy.executar(movimentos, dados);

        assertThat(r1).isEqualByComparingTo(r2);
        verify(arrecadacaoCargaRepository, times(1)).listar(10L, "03", "2024");
    }
}
