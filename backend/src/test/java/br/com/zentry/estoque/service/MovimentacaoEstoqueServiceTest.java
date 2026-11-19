package br.com.zentry.estoque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.zentry.compartilhado.erro.RegraNegocioException;
import br.com.zentry.estoque.dto.AjusteEstoqueRequest;
import br.com.zentry.estoque.dto.InsumoRequest;
import br.com.zentry.estoque.dto.MovimentacaoEstoqueRequest;
import br.com.zentry.estoque.dto.MovimentacaoEstoqueResponse;
import br.com.zentry.estoque.model.AjusteEstoque;
import br.com.zentry.estoque.model.Insumo;
import br.com.zentry.estoque.model.MotivoAjuste;
import br.com.zentry.estoque.model.StatusAjuste;
import br.com.zentry.estoque.model.TipoMovimentacaoEstoque;
import br.com.zentry.estoque.model.UnidadeMedida;
import br.com.zentry.estoque.repository.AjusteEstoqueRepository;
import br.com.zentry.estoque.repository.CategoriaInsumoRepository;
import jakarta.validation.Validator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MovimentacaoEstoqueServiceTest {

    @Autowired
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @Autowired
    private AjusteEstoqueRepository ajusteEstoqueRepository;

    @Autowired
    private Validator validator;

    private Insumo insumoComSaldo(String saldo) {
        Long categoriaId = categoriaInsumoRepository.findAll().getFirst().getId();
        Long id = insumoService.cadastrar(new InsumoRequest("Pão teste", null, UnidadeMedida.UNIDADE, true,
                new BigDecimal("2"), new BigDecimal("1.20"), true, categoriaId, new BigDecimal(saldo))).id();
        return insumoService.buscarPorId(id);
    }

    private MovimentacaoEstoqueRequest movimentacao(Insumo insumo, TipoMovimentacaoEstoque tipo, String quantidade) {
        return new MovimentacaoEstoqueRequest(insumo.getId(), tipo, new BigDecimal(quantidade));
    }

    private AjusteEstoqueRequest ajuste(Insumo insumo, String contada, MotivoAjuste motivo) {
        return new AjusteEstoqueRequest(insumo.getId(), new BigDecimal(contada), motivo);
    }

    @Test
    void entradaSomaAoSaldoEFicaNoHistorico() {
        Insumo insumo = insumoComSaldo("10");

        movimentacaoEstoqueService.registrar(movimentacao(insumo, TipoMovimentacaoEstoque.ENTRADA, "5"));

        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("15");
        var historico = movimentacaoEstoqueService.listarPorInsumo(insumo.getId(), PageRequest.of(0, 20));
        assertThat(historico.content()).extracting(MovimentacaoEstoqueResponse::quantidade)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactlyInAnyOrder(new BigDecimal("10"), new BigDecimal("5"));
    }

    @Test
    void retiradaSubtraiDoSaldo() {
        Insumo insumo = insumoComSaldo("10");

        movimentacaoEstoqueService.registrar(movimentacao(insumo, TipoMovimentacaoEstoque.RETIRADA, "4"));

        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("6");
    }

    @Test
    void recusaRetiradaMaiorQueOSaldo() {
        Insumo insumo = insumoComSaldo("10");

        assertThatThrownBy(() -> movimentacaoEstoqueService.registrar(
                movimentacao(insumo, TipoMovimentacaoEstoque.RETIRADA, "12")))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("10");
    }

    @Test
    void recusaBaixaPorVendaPelaRotaPublica() {
        Insumo insumo = insumoComSaldo("10");

        assertThatThrownBy(() -> movimentacaoEstoqueService.registrar(
                movimentacao(insumo, TipoMovimentacaoEstoque.BAIXA_POR_VENDA, "1")))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("TIPO_INVALIDO");
    }

    @Test
    void ajusteDePerdaDeixaOSaldoIgualAContagem() {
        Insumo insumo = insumoComSaldo("10");

        MovimentacaoEstoqueResponse resposta = movimentacaoEstoqueService.ajustar(ajuste(insumo, "8", MotivoAjuste.PERDA));

        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("8");
        assertThat(resposta.tipo()).isEqualTo(TipoMovimentacaoEstoque.AJUSTE);
        assertThat(resposta.quantidade()).isEqualByComparingTo("2");
        assertThat(resposta.motivo()).isEqualTo(MotivoAjuste.PERDA);
        AjusteEstoque ajusteGravado = ajusteEstoqueRepository.findById(resposta.id()).orElseThrow();
        assertThat(ajusteGravado.getSaldoSistema()).isEqualByComparingTo("10");
        assertThat(ajusteGravado.getDiferenca()).isEqualByComparingTo("-2");
        assertThat(ajusteGravado.getStatus()).isEqualTo(StatusAjuste.EFETIVADO);
    }

    @Test
    void ajusteComContagemMaiorAumentaOSaldo() {
        Insumo insumo = insumoComSaldo("10");

        movimentacaoEstoqueService.ajustar(ajuste(insumo, "12", MotivoAjuste.ERRO_CONTAGEM));

        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("12");
    }

    @Test
    void recusaAjusteSemDiferenca() {
        Insumo insumo = insumoComSaldo("10");

        assertThatThrownBy(() -> movimentacaoEstoqueService.ajustar(ajuste(insumo, "10", MotivoAjuste.PERDA)))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("QUANTIDADE_INVALIDA");
    }

    @Test
    void recusaAjusteSemMotivo() {
        var erros = validator.validate(new AjusteEstoqueRequest(1L, new BigDecimal("8"), null));

        assertThat(erros).extracting(erro -> erro.getPropertyPath().toString()).containsExactly("motivo");
    }

    @Test
    void registrarBaixaDiminuiOSaldoComTipoBaixaPorVenda() {
        Insumo insumo = insumoComSaldo("10");

        var baixa = movimentacaoEstoqueService.registrarBaixa(insumo, new BigDecimal("3"));

        assertThat(baixa.getTipo()).isEqualTo(TipoMovimentacaoEstoque.BAIXA_POR_VENDA);
        assertThat(insumo.getQuantidadeAtual()).isEqualByComparingTo("7");
    }
}
