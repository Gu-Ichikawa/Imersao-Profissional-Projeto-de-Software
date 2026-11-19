package br.com.zentry.cardapio.controller;

import br.com.zentry.cardapio.dto.VendaRequest;
import br.com.zentry.cardapio.dto.VendaResponse;
import br.com.zentry.cardapio.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Venda simulada (sem comanda): é a rota que o modal "Registrar venda" (SCRUM-106) chama
@RestController
@RequestMapping("/api/pratos/{pratoId}/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VendaResponse registrar(@PathVariable Long pratoId, @Valid @RequestBody VendaRequest request) {
        return vendaService.registrarVenda(pratoId, request.quantidade());
    }
}
