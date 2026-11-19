package br.com.zentry.cardapio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.zentry.cardapio.dto.PratoRequest;
import br.com.zentry.cardapio.dto.PratoResponse;
import br.com.zentry.cardapio.repository.CategoriaCardapioRepository;
import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

// Usa o banco local (com os dados do V3). @Transactional desfaz tudo no fim de cada teste.
@SpringBootTest
@Transactional
class PratoServiceTest {

    @Autowired
    private PratoService pratoService;

    @Autowired
    private CategoriaCardapioRepository categoriaCardapioRepository;

    @Autowired
    private EntityManager entityManager;

    private PratoRequest request(String nome, String preco, Long categoriaId) {
        return new PratoRequest(nome, "  Descrição de teste  ", new BigDecimal(preco), categoriaId);
    }

    private Long categoria(String nome) {
        return categoriaCardapioRepository.findAll().stream()
                .filter(c -> c.getNome().equals(nome))
                .findFirst().orElseThrow().getId();
    }

    @Test
    void cadastraPratoAtivoComCategoria() {
        PratoResponse criado = pratoService.cadastrar(request("  X-Salada teste  ", "27.50", categoria("Lanches")));

        assertThat(criado.id()).isNotNull();
        assertThat(criado.nome()).isEqualTo("X-Salada teste");
        assertThat(criado.descricao()).isEqualTo("Descrição de teste");
        assertThat(criado.precoVenda()).isEqualByComparingTo("27.50");
        assertThat(criado.ativo()).isTrue();
        assertThat(criado.categoria().nome()).isEqualTo("Lanches");
    }

    @Test
    void recusaCategoriaInexistente() {
        assertThatThrownBy(() -> pratoService.cadastrar(request("X-Salada teste", "27.50", 999_999L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Categoria");
    }

    @Test
    void recusaCategoriaInativa() {
        Long sobremesas = categoria("Sobremesas");
        entityManager.createNativeQuery("UPDATE categoria_cardapio SET ativa = b'0' WHERE id = :id")
                .setParameter("id", sobremesas).executeUpdate();
        entityManager.clear();

        assertThatThrownBy(() -> pratoService.cadastrar(request("Pudim teste", "15.00", sobremesas)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("inativa");
    }

    @Test
    void editaPratoMantendoOStatus() {
        PratoResponse criado = pratoService.cadastrar(request("X-Salada teste", "27.50", categoria("Lanches")));
        pratoService.inativar(criado.id());

        PratoResponse editado = pratoService.editar(criado.id(), request("X-Bacon teste", "31.00", categoria("Lanches")));

        assertThat(editado.nome()).isEqualTo("X-Bacon teste");
        assertThat(editado.precoVenda()).isEqualByComparingTo("31.00");
        assertThat(editado.ativo()).isFalse();
    }

    @Test
    void inativarEAtivarNaoApagamOPrato() {
        PratoResponse criado = pratoService.cadastrar(request("X-Salada teste", "27.50", categoria("Lanches")));

        pratoService.inativar(criado.id());
        assertThat(pratoService.buscar(criado.id()).ativo()).isFalse();

        pratoService.ativar(criado.id());
        assertThat(pratoService.buscar(criado.id()).ativo()).isTrue();
    }

    @Test
    void listaFiltrandoPorNomeECategoria() {
        var porNome = pratoService.listar("hambúrguer", null, null, PageRequest.of(0, 20));
        assertThat(porNome.content()).extracting(PratoResponse::nome).containsExactly("Hambúrguer artesanal");

        var porCategoria = pratoService.listar(null, categoria("Porções"), null, PageRequest.of(0, 20));
        assertThat(porCategoria.content()).extracting(PratoResponse::nome)
                .containsExactlyInAnyOrder("Batata frita", "Porção de calabresa");
    }

    @Test
    void buscarPratoInexistenteDa404() {
        assertThatThrownBy(() -> pratoService.buscar(999_999L)).isInstanceOf(NaoEncontradoException.class);
    }
}
