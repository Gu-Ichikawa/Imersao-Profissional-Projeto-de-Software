package br.com.zentry.estoque.controller;

import br.com.zentry.compartilhado.dto.PaginaResponse;
import br.com.zentry.estoque.dto.AjusteEstoqueRequest;
import br.com.zentry.estoque.dto.MovimentacaoEstoqueRequest;
import br.com.zentry.estoque.dto.MovimentacaoEstoqueResponse;
import br.com.zentry.estoque.service.MovimentacaoEstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimentacoes-estoque")
@RequiredArgsConstructor
public class MovimentacaoEstoqueController {

    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    @GetMapping("/insumos/{insumoId}")
    public PaginaResponse<MovimentacaoEstoqueResponse> listarPorInsumo(
            @PathVariable Long insumoId,
            @PageableDefault(size = 20, sort = "dataHora", direction = Sort.Direction.DESC) Pageable pageable) {
        return movimentacaoEstoqueService.listarPorInsumo(insumoId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoEstoqueResponse registrar(@Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return movimentacaoEstoqueService.registrar(request);
    }

    @PostMapping("/ajustes")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimentacaoEstoqueResponse ajustar(@Valid @RequestBody AjusteEstoqueRequest request) {
        return movimentacaoEstoqueService.ajustar(request);
    }
}
