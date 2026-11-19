package br.com.zentry.cardapio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.zentry.cardapio.dto.VendaResponse;
import br.com.zentry.cardapio.repository.PratoRepository;
import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import br.com.zentry.estoque.model.Insumo;
import br.com.zentry.estoque.repository.InsumoRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// Usa o banco local com os dados do V3: o Hambúrguer artesanal tem ficha
// (1 pão, 150 g de carne, 2 cheddar, 20 g de alface, 30 ml de molho); a Batata frita não tem.
// @Transactional desfaz tudo no fim de cada teste.
@SpringBootTest
@Transactional
class VendaServiceTest {

    @Autowired
    private VendaService vendaService;

    @Autowired
    private PratoService pratoService;

    @Autowired
    private PratoRepository pratoRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private EntityManager entityManager;

    private Long prato(String nome) {
        return pratoRepository.findAll().stream()
                .filter(p -> p.getNome().equals(nome))
                .findFirst().orElseThrow().getId();
    }

    private Insumo insumo(String nome) {
        return insumoRepository.findAll().stream()
                .filter(i -> i.getNome().equals(nome))
                .findFirst().orElseThrow();
    }

    private long baixasPorVenda() {
        entityManager.flush();
        Number total = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM movimentacao_estoque WHERE tipo = 'BAIXA_POR_VENDA'")
                .getSingleResult();
        return total.longValue();
    }

    @Test
    void vendaDescontaCadaInsumoDaFichaVezesAQuantidade() {
        long baixasAntes = baixasPorVenda();

        VendaResponse resposta = vendaService.registrarVenda(prato("Hambúrguer artesanal"), 2);

        assertThat(resposta.quantidade()).isEqualTo(2);
        assertThat(resposta.baixas()).hasSize(5);
        assertThat(insumo("Pão brioche").getQuantidadeAtual()).isEqualByComparingTo("8");
        assertThat(insumo("Carne bovina").getQuantidadeAtual()).isEqualByComparingTo("1700");
        assertThat(insumo("Queijo cheddar").getQuantidadeAtual()).isEqualByComparingTo("36");
        assertThat(insumo("Alface americana").getQuantidadeAtual()).isEqualByComparingTo("460");
        assertThat(insumo("Molho da casa").getQuantidadeAtual()).isEqualByComparingTo("940");
        assertThat(baixasPorVenda()).isEqualTo(baixasAntes + 5);
    }

    @Test
    void faltaDeSaldoRecusaAVendaInteiraSemBaixarNada() {
        long baixasAntes = baixasPorVenda();

        // Há 10 pães: 11 hambúrgueres não cabem
        assertThatThrownBy(() -> vendaService.registrarVenda(prato("Hambúrguer artesanal"), 11))
                .isInstanceOf(RegraNegocioException.class)
                .hasFieldOrPropertyWithValue("codigo", "ESTOQUE_INSUFICIENTE")
                .hasMessageContaining("Pão brioche");

        assertThat(insumo("Pão brioche").getQuantidadeAtual()).isEqualByComparingTo("10");
        assertThat(insumo("Carne bovina").getQuantidadeAtual()).isEqualByComparingTo("2000");
        assertThat(baixasPorVenda()).isEqualTo(baixasAntes);
    }

    @Test
    void insumoSemControleDeEstoqueEPulado() {
        Insumo molho = insumo("Molho da casa");
        molho.atualizar(molho.getNome(), molho.getSku(), molho.getUnidadeMedida(), molho.getPerecivel(),
                molho.getEstoqueMinimo(), molho.getCustoUnitario(), false, molho.getCategoria());

        VendaResponse resposta = vendaService.registrarVenda(prato("Hambúrguer artesanal"), 1);

        assertThat(resposta.baixas()).hasSize(4)
                .noneMatch(baixa -> baixa.insumo().equals("Molho da casa"));
        assertThat(insumo("Molho da casa").getQuantidadeAtual()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(insumo("Pão brioche").getQuantidadeAtual()).isEqualByComparingTo("9");
    }

    @Test
    void pratoInativoNaoPodeSerVendido() {
        Long hamburguer = prato("Hambúrguer artesanal");
        pratoService.inativar(hamburguer);

        assertThatThrownBy(() -> vendaService.registrarVenda(hamburguer, 1))
                .isInstanceOf(RegraNegocioException.class)
                .hasFieldOrPropertyWithValue("codigo", "PRATO_INATIVO");
    }

    @Test
    void pratoSemFichaTecnicaNaoPodeSerVendido() {
        assertThatThrownBy(() -> vendaService.registrarVenda(prato("Batata frita"), 1))
                .isInstanceOf(RegraNegocioException.class)
                .hasFieldOrPropertyWithValue("codigo", "PRATO_SEM_FICHA");
    }

    @Test
    void pratoInexistenteDa404() {
        assertThatThrownBy(() -> vendaService.registrarVenda(999999L, 1))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
