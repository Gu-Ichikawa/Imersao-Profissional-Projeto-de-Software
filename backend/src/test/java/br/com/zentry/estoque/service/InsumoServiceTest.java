package br.com.zentry.estoque.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import br.com.zentry.estoque.dto.InsumoRequest;
import br.com.zentry.estoque.dto.InsumoResponse;
import br.com.zentry.estoque.model.NivelEstoque;
import br.com.zentry.estoque.model.TipoMovimentacaoEstoque;
import br.com.zentry.estoque.model.UnidadeMedida;
import br.com.zentry.estoque.repository.CategoriaInsumoRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class InsumoServiceTest {

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private CategoriaInsumoRepository categoriaInsumoRepository;

    @Autowired
    private MovimentacaoEstoqueService movimentacaoEstoqueService;

    private InsumoRequest request(String nome, String sku, Long categoriaId, String quantidadeInicial) {
        return new InsumoRequest(nome, sku, UnidadeMedida.KG, true, new BigDecimal("2"),
                new BigDecimal("39.90"), true, categoriaId,
                quantidadeInicial == null ? null : new BigDecimal(quantidadeInicial));
    }

    private Long primeiraCategoria() {
        return categoriaInsumoRepository.findAll().getFirst().getId();
    }

    @Test
    void cadastraComQuantidadeInicialECalculaONivel() {
        InsumoResponse criado = insumoService.cadastrar(request("Bacon teste", "TESTE-001", primeiraCategoria(), "2.5"));

        assertThat(criado.id()).isNotNull();
        assertThat(criado.ativo()).isTrue();
        assertThat(criado.quantidadeAtual()).isEqualByComparingTo("2.5");
        assertThat(criado.nivelEstoque()).isEqualTo(NivelEstoque.ATENCAO);
        assertThat(criado.categoria().id()).isEqualTo(primeiraCategoria());
    }

    @Test
    void cadastroComQuantidadeInicialRegistraUmaEntrada() {
        InsumoResponse criado = insumoService.cadastrar(request("Bacon teste", null, primeiraCategoria(), "2.5"));

        var historico = movimentacaoEstoqueService.listarPorInsumo(criado.id(), PageRequest.of(0, 20));

        assertThat(historico.content()).singleElement().satisfies(movimentacao -> {
            assertThat(movimentacao.tipo()).isEqualTo(TipoMovimentacaoEstoque.ENTRADA);
            assertThat(movimentacao.quantidade()).isEqualByComparingTo("2.5");
        });
    }

    @Test
    void recusaSkuRepetido() {
        assertThatThrownBy(() -> insumoService.cadastrar(request("Outro pão", "INS-001", primeiraCategoria(), null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("SKU");
    }

    @Test
    void recusaCategoriaInexistente() {
        assertThatThrownBy(() -> insumoService.cadastrar(request("Bacon teste", null, 999_999L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Categoria");
    }

    @Test
    void editarNaoMexeNoSaldo() {
        InsumoResponse criado = insumoService.cadastrar(request("Bacon teste", null, primeiraCategoria(), "10"));

        InsumoResponse editado = insumoService.editar(criado.id(),
                request("Bacon defumado teste", null, primeiraCategoria(), "999"));

        assertThat(editado.nome()).isEqualTo("Bacon defumado teste");
        assertThat(editado.quantidadeAtual()).isEqualByComparingTo("10");
    }

    @Test
    void inativarNaoApagaOInsumo() {
        InsumoResponse criado = insumoService.cadastrar(request("Bacon teste", null, primeiraCategoria(), null));

        insumoService.inativar(criado.id());

        assertThat(insumoService.buscar(criado.id()).ativo()).isFalse();
    }

    @Test
    void listaFiltrandoPorNome() {
        var pagina = insumoService.listar("brioche", null, PageRequest.of(0, 20));

        assertThat(pagina.content()).extracting(InsumoResponse::nome).containsExactly("Pão brioche");
    }

    @Test
    void buscarInsumoInexistenteDa404() {
        assertThatThrownBy(() -> insumoService.buscar(999_999L)).isInstanceOf(NaoEncontradoException.class);
    }
}
