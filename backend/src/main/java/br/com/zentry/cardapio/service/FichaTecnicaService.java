package br.com.zentry.cardapio.service;

import br.com.zentry.cardapio.dto.FichaTecnicaResponse;
import br.com.zentry.cardapio.dto.ItemFichaRequest;
import br.com.zentry.cardapio.model.FichaTecnica;
import br.com.zentry.cardapio.model.ItemFichaTecnica;
import br.com.zentry.cardapio.repository.FichaTecnicaRepository;
import br.com.zentry.estoque.service.InsumoService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FichaTecnicaService {

    private final FichaTecnicaRepository fichaTecnicaRepository;
    private final PratoService pratoService;
    private final InsumoService insumoService;

    /** Busca a ficha do prato e cria uma vazia se ainda não existir. */
    @Transactional
    public FichaTecnicaResponse buscarOuCriar(Long pratoId) {
        return FichaTecnicaResponse.de(obterOuCriar(pratoId));
    }

    @Transactional
    public FichaTecnicaResponse adicionarItem(Long pratoId, ItemFichaRequest request) {
        FichaTecnica ficha = obterOuCriar(pratoId);
        ficha.adicionarItem(insumoService.buscarPorId(request.insumoId()), request.quantidade());
        // O id do item novo só nasce no flush; sem isso a resposta voltaria com id nulo
        fichaTecnicaRepository.flush();
        return FichaTecnicaResponse.de(ficha);
    }

    @Transactional
    public FichaTecnicaResponse alterarQuantidade(Long pratoId, Long itemId, BigDecimal quantidade) {
        FichaTecnica ficha = obterOuCriar(pratoId);
        ficha.alterarQuantidade(itemId, quantidade);
        return FichaTecnicaResponse.de(ficha);
    }

    @Transactional
    public FichaTecnicaResponse removerItem(Long pratoId, Long itemId) {
        FichaTecnica ficha = obterOuCriar(pratoId);
        ficha.removerItem(itemId);
        return FichaTecnicaResponse.de(ficha);
    }

    /**
     * Contrato interno: usado pela venda (SCRUM-105). Devolve os itens (insumo + quantidade)
     * da ficha do prato; lista vazia se o prato não tem ficha ou a ficha está sem itens
     * (a venda responde 422 PRATO_SEM_FICHA). Chamar dentro de uma transação, pois o insumo é LAZY.
     */
    @Transactional(readOnly = true)
    public List<ItemFichaTecnica> listarItensDoPrato(Long pratoId) {
        return fichaTecnicaRepository.findByPratoId(pratoId)
                .map(FichaTecnica::getItens)
                .orElse(List.of());
    }

    private FichaTecnica obterOuCriar(Long pratoId) {
        return fichaTecnicaRepository.findByPratoId(pratoId)
                .orElseGet(() -> fichaTecnicaRepository.save(new FichaTecnica(pratoService.buscarPorId(pratoId))));
    }
}
