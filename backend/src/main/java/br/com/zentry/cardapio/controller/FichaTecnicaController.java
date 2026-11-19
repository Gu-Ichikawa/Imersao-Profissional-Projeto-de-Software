package br.com.zentry.cardapio.controller;

import br.com.zentry.cardapio.dto.FichaTecnicaResponse;
import br.com.zentry.cardapio.dto.ItemFichaRequest;
import br.com.zentry.cardapio.dto.QuantidadeItemRequest;
import br.com.zentry.cardapio.service.FichaTecnicaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pratos/{pratoId}/ficha-tecnica")
@RequiredArgsConstructor
public class FichaTecnicaController {

    private final FichaTecnicaService fichaTecnicaService;

    @GetMapping
    public FichaTecnicaResponse buscar(@PathVariable Long pratoId) {
        return fichaTecnicaService.buscarOuCriar(pratoId);
    }

    @PostMapping("/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public FichaTecnicaResponse adicionarItem(@PathVariable Long pratoId, @Valid @RequestBody ItemFichaRequest request) {
        return fichaTecnicaService.adicionarItem(pratoId, request);
    }

    @PutMapping("/itens/{itemId}")
    public FichaTecnicaResponse alterarQuantidade(@PathVariable Long pratoId, @PathVariable Long itemId,
                                                  @Valid @RequestBody QuantidadeItemRequest request) {
        return fichaTecnicaService.alterarQuantidade(pratoId, itemId, request.quantidade());
    }

    // Devolve a ficha atualizada (200) em vez de 204: a tela precisa do novo custo total
    @DeleteMapping("/itens/{itemId}")
    public FichaTecnicaResponse removerItem(@PathVariable Long pratoId, @PathVariable Long itemId) {
        return fichaTecnicaService.removerItem(pratoId, itemId);
    }
}
