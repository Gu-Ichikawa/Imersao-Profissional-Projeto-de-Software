package br.com.zentry.cardapio.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.zentry.cardapio.dto.ItemFichaRequest;
import br.com.zentry.cardapio.model.CategoriaCardapio;
import br.com.zentry.cardapio.model.FichaTecnica;
import br.com.zentry.cardapio.model.Prato;
import br.com.zentry.cardapio.repository.FichaTecnicaRepository;
import br.com.zentry.estoque.model.Insumo;
import br.com.zentry.estoque.service.InsumoService;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FichaTecnicaServiceTest {

    @Mock
    private FichaTecnicaRepository fichaTecnicaRepository;
    @Mock
    private PratoService pratoService;
    @Mock
    private InsumoService insumoService;
    @InjectMocks
    private FichaTecnicaService service;

    private Prato prato() {
        CategoriaCardapio categoria = org.mockito.Mockito.mock(CategoriaCardapio.class);
        when(categoria.getNome()).thenReturn("Lanches");
        Prato prato = org.mockito.Mockito.mock(Prato.class);
        when(prato.getId()).thenReturn(1L);
        when(prato.getNome()).thenReturn("Hambúrguer artesanal");
        when(prato.getPrecoVenda()).thenReturn(new BigDecimal("32.00"));
        when(prato.getCategoria()).thenReturn(categoria);
        return prato;
    }

    @Test
    void criaFichaVaziaQuandoOPratoAindaNaoTem() {
        Prato prato = prato();
        when(fichaTecnicaRepository.findByPratoId(1L)).thenReturn(Optional.empty());
        when(pratoService.buscarPorId(1L)).thenReturn(prato);
        when(fichaTecnicaRepository.save(any(FichaTecnica.class))).thenAnswer(i -> i.getArgument(0));

        var resposta = service.buscarOuCriar(1L);

        assertThat(resposta.pratoNome()).isEqualTo("Hambúrguer artesanal");
        assertThat(resposta.itens()).isEmpty();
        assertThat(resposta.custoTotal()).isEqualByComparingTo("0");
    }

    @Test
    void naoCriaOutraFichaQuandoJaExiste() {
        FichaTecnica ficha = new FichaTecnica(prato());
        when(fichaTecnicaRepository.findByPratoId(1L)).thenReturn(Optional.of(ficha));

        service.buscarOuCriar(1L);

        verify(fichaTecnicaRepository, never()).save(any());
    }

    @Test
    void adicionaItemEDevolveOCustoAtualizado() {
        Insumo pao = org.mockito.Mockito.mock(Insumo.class);
        when(pao.getId()).thenReturn(7L);
        when(pao.getNome()).thenReturn("Pão brioche");
        when(pao.getCustoUnitario()).thenReturn(new BigDecimal("1.2000"));
        FichaTecnica ficha = new FichaTecnica(prato());
        when(fichaTecnicaRepository.findByPratoId(1L)).thenReturn(Optional.of(ficha));
        when(insumoService.buscarPorId(7L)).thenReturn(pao);

        var resposta = service.adicionarItem(1L, new ItemFichaRequest(7L, new BigDecimal("2")));

        assertThat(resposta.itens()).hasSize(1);
        assertThat(resposta.custoTotal()).isEqualByComparingTo("2.40");
        verify(fichaTecnicaRepository).flush();
    }

    @Test
    void listarItensDoPratoVemVazioQuandoOPratoNaoTemFicha() {
        when(fichaTecnicaRepository.findByPratoId(9L)).thenReturn(Optional.empty());

        assertThat(service.listarItensDoPrato(9L)).isEmpty();
        verify(fichaTecnicaRepository, never()).save(any());
    }
}
