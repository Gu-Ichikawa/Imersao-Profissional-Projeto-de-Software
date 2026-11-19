package br.com.zentry.cardapio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.zentry.cardapio.dto.CategoriaCardapioRequest;
import br.com.zentry.cardapio.dto.CategoriaCardapioResponse;
import br.com.zentry.cardapio.repository.CategoriaCardapioRepository;
import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

// Usa o banco local (com os dados do V3). @Transactional desfaz tudo no fim de cada teste.
@SpringBootTest
@Transactional
class CategoriaCardapioServiceTest {

    @Autowired
    private CategoriaCardapioService categoriaCardapioService;

    @Autowired
    private CategoriaCardapioRepository categoriaCardapioRepository;

    private Long categoria(String nome) {
        return categoriaCardapioRepository.findAll().stream()
                .filter(c -> c.getNome().equals(nome))
                .findFirst().orElseThrow().getId();
    }

    @Test
    void cadastraCategoriaAtiva() {
        CategoriaCardapioResponse criada = categoriaCardapioService.cadastrar(new CategoriaCardapioRequest("  Bebidas teste  "));

        assertThat(criada.id()).isNotNull();
        assertThat(criada.nome()).isEqualTo("Bebidas teste");
        assertThat(criada.ativa()).isTrue();
    }

    @Test
    void recusaNomeRepetido() {
        assertThatThrownBy(() -> categoriaCardapioService.cadastrar(new CategoriaCardapioRequest("Lanches")))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("CATEGORIA_JA_CADASTRADA");
    }

    @Test
    void editaONome() {
        Long id = categoriaCardapioService.cadastrar(new CategoriaCardapioRequest("Bebidas teste")).id();

        CategoriaCardapioResponse editada = categoriaCardapioService.editar(id, new CategoriaCardapioRequest("Drinks teste"));

        assertThat(editada.nome()).isEqualTo("Drinks teste");
    }

    @Test
    void recusaEditarParaNomeDeOutraCategoria() {
        Long id = categoriaCardapioService.cadastrar(new CategoriaCardapioRequest("Bebidas teste")).id();

        assertThatThrownBy(() -> categoriaCardapioService.editar(id, new CategoriaCardapioRequest("Pizzas")))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("CATEGORIA_JA_CADASTRADA");
    }

    @Test
    void inativaEReativa() {
        Long id = categoria("Sobremesas");

        categoriaCardapioService.inativar(id);
        assertThat(categoriaCardapioService.buscar(id).ativa()).isFalse();

        categoriaCardapioService.ativar(id);
        assertThat(categoriaCardapioService.buscar(id).ativa()).isTrue();
    }

    @Test
    void excluiCategoriaSemPratos() {
        Long id = categoriaCardapioService.cadastrar(new CategoriaCardapioRequest("Bebidas teste")).id();

        categoriaCardapioService.excluir(id);

        assertThatThrownBy(() -> categoriaCardapioService.buscar(id)).isInstanceOf(NaoEncontradoException.class);
    }

    @Test
    void bloqueiaExclusaoDeCategoriaComPratos() {
        Long id = categoria("Lanches");

        assertThatThrownBy(() -> categoriaCardapioService.excluir(id))
                .isInstanceOf(RegraNegocioException.class)
                .extracting("codigo").isEqualTo("CATEGORIA_COM_PRATOS");
        assertThat(categoriaCardapioService.buscar(id).nome()).isEqualTo("Lanches");
    }

    @Test
    void listaPaginadoPorNome() {
        var pagina = categoriaCardapioService.listar(PageRequest.of(0, 20, Sort.by("nome")));

        assertThat(pagina.content()).extracting(CategoriaCardapioResponse::nome)
                .containsExactly("Lanches", "Pizzas", "Porções", "Saladas", "Sobremesas");
    }
}
