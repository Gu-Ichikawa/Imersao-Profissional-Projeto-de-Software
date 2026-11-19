package br.com.zentry.cardapio.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import br.com.zentry.estoque.model.Insumo;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class FichaTecnicaTest {

    private Insumo insumo(long id, String nome, String custoUnitario) {
        Insumo insumo = mock(Insumo.class);
        when(insumo.getId()).thenReturn(id);
        when(insumo.getNome()).thenReturn(nome);
        when(insumo.getCustoUnitario()).thenReturn(new BigDecimal(custoUnitario));
        return insumo;
    }

    private FichaTecnica fichaDoHamburguer() {
        FichaTecnica ficha = new FichaTecnica(mock(Prato.class));
        ficha.adicionarItem(insumo(1, "Pão brioche", "1.2000"), new BigDecimal("1"));
        ficha.adicionarItem(insumo(2, "Carne bovina", "0.0380"), new BigDecimal("150"));
        ficha.adicionarItem(insumo(3, "Queijo cheddar", "0.4500"), new BigDecimal("2"));
        ficha.adicionarItem(insumo(4, "Alface americana", "0.0150"), new BigDecimal("20"));
        ficha.adicionarItem(insumo(5, "Molho da casa", "0.0100"), new BigDecimal("30"));
        return ficha;
    }

    @Test
    void fichaNovaComecaVaziaComCustoZero() {
        FichaTecnica ficha = new FichaTecnica(mock(Prato.class));

        assertThat(ficha.getItens()).isEmpty();
        assertThat(ficha.getCustoTotal()).isEqualByComparingTo("0");
        assertThat(ficha.getAtualizadaEm()).isNotNull();
    }

    @Test
    void custoDoHamburguerBateComADemo() {
        FichaTecnica ficha = fichaDoHamburguer();

        assertThat(ficha.getCustoTotal()).isEqualByComparingTo("8.40");
        assertThat(ficha.getCustoTotal().scale()).isEqualTo(2);
    }

    @Test
    void custoTotalArredondaPara2Casas() {
        FichaTecnica ficha = new FichaTecnica(mock(Prato.class));
        // 1,5 x 0,0333 = 0,04995 -> 0,05
        ficha.adicionarItem(insumo(1, "Tempero", "0.0333"), new BigDecimal("1.5"));

        assertThat(ficha.getCustoTotal()).isEqualByComparingTo("0.05");
    }

    @Test
    void mesmoInsumoDuasVezesDa409() {
        FichaTecnica ficha = new FichaTecnica(mock(Prato.class));
        Insumo pao = insumo(1, "Pão brioche", "1.2000");
        ficha.adicionarItem(pao, BigDecimal.ONE);

        assertThatThrownBy(() -> ficha.adicionarItem(pao, BigDecimal.TEN))
                .isInstanceOfSatisfying(RegraNegocioException.class, e -> {
                    assertThat(e.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(e.getCodigo()).isEqualTo("INSUMO_JA_NA_FICHA");
                });
        assertThat(ficha.getItens()).hasSize(1);
    }

    @Test
    void quantidadeZeroOuNegativaEhRecusada() {
        FichaTecnica ficha = new FichaTecnica(mock(Prato.class));
        Insumo pao = insumo(1, "Pão brioche", "1.2000");

        assertThatThrownBy(() -> ficha.adicionarItem(pao, BigDecimal.ZERO))
                .isInstanceOfSatisfying(RegraNegocioException.class,
                        e -> assertThat(e.getCodigo()).isEqualTo("QUANTIDADE_INVALIDA"));
        assertThatThrownBy(() -> ficha.adicionarItem(pao, new BigDecimal("-1")))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(ficha.getItens()).isEmpty();
    }

    @Test
    void alterarQuantidadeRecalculaOCusto() {
        FichaTecnica ficha = fichaDoHamburguer();
        // O id real vem do banco; aqui o item ainda não foi salvo, então fixamos o id por reflexão
        ItemFichaTecnica carne = ficha.getItens().get(1);
        org.springframework.test.util.ReflectionTestUtils.setField(carne, "id", 20L);

        ficha.alterarQuantidade(20L, new BigDecimal("200"));

        // carne passa de 5,70 para 7,60: 8,40 + 1,90 = 10,30
        assertThat(ficha.getCustoTotal()).isEqualByComparingTo("10.30");
    }

    @Test
    void removerItemRecalculaOCusto() {
        FichaTecnica ficha = fichaDoHamburguer();
        ItemFichaTecnica queijo = ficha.getItens().get(2);
        org.springframework.test.util.ReflectionTestUtils.setField(queijo, "id", 30L);

        ficha.removerItem(30L);

        // tira 0,90 do queijo: 8,40 - 0,90 = 7,50
        assertThat(ficha.getItens()).hasSize(4);
        assertThat(ficha.getCustoTotal()).isEqualByComparingTo("7.50");
    }

    @Test
    void itemInexistenteDa404() {
        FichaTecnica ficha = fichaDoHamburguer();

        assertThatThrownBy(() -> ficha.alterarQuantidade(999L, BigDecimal.ONE))
                .isInstanceOf(NaoEncontradoException.class);
        assertThatThrownBy(() -> ficha.removerItem(999L))
                .isInstanceOf(NaoEncontradoException.class);
    }
}
