package br.com.zentry.estoque.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.zentry.compartilhado.erro.RegraNegocioException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InsumoTest {

    private Insumo insumo(String saldo, String minimo, boolean controlaEstoque) {
        return new Insumo("Pão brioche", null, UnidadeMedida.UNIDADE, true,
                new BigDecimal(minimo), new BigDecimal("1.20"), controlaEstoque, null, new BigDecimal(saldo));
    }

    @Test
    void nivelEstoqueSegueARegraDoGrupo() {
        assertThat(insumo("5", "5", true).nivelEstoque()).isEqualTo(NivelEstoque.REPOR);
        assertThat(insumo("7.5", "5", true).nivelEstoque()).isEqualTo(NivelEstoque.ATENCAO);
        assertThat(insumo("8", "5", true).nivelEstoque()).isEqualTo(NivelEstoque.SUFICIENTE);
        assertThat(insumo("8", "5", false).nivelEstoque()).isNull();
    }

    @Test
    void insumoNasceAtivoESemSaldoQuandoNaoInformado() {
        Insumo insumo = new Insumo("Sal", null, UnidadeMedida.G, false,
                BigDecimal.ZERO, BigDecimal.ZERO, false, null, null);
        assertThat(insumo.getAtivo()).isTrue();
        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("0");
    }

    @Test
    void aumentarEDiminuirSaldo() {
        Insumo insumo = insumo("10", "5", true);
        insumo.aumentarSaldo(new BigDecimal("2"));
        insumo.diminuirSaldo(new BigDecimal("4"));
        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("8");
    }

    @Test
    void naoDeixaSaldoFicarNegativo() {
        Insumo insumo = insumo("10", "5", true);
        assertThatThrownBy(() -> insumo.diminuirSaldo(new BigDecimal("12")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("10");
    }

    @Test
    void recusaQuantidadeZeroOuNegativa() {
        Insumo insumo = insumo("10", "5", true);
        assertThatThrownBy(() -> insumo.aumentarSaldo(BigDecimal.ZERO)).isInstanceOf(RegraNegocioException.class);
        assertThatThrownBy(() -> insumo.diminuirSaldo(new BigDecimal("-1"))).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void inativarEAtivar() {
        Insumo insumo = insumo("10", "5", true);
        insumo.inativar();
        assertThat(insumo.getAtivo()).isFalse();
        insumo.ativar();
        assertThat(insumo.getAtivo()).isTrue();
    }
}
