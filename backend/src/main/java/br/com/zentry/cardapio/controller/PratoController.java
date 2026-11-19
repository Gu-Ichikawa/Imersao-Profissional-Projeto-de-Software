package br.com.zentry.cardapio.controller;

import br.com.zentry.cardapio.dto.PratoRequest;
import br.com.zentry.cardapio.dto.PratoResponse;
import br.com.zentry.cardapio.service.PratoService;
import br.com.zentry.compartilhado.dto.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pratos")
@RequiredArgsConstructor
public class PratoController {

    private final PratoService pratoService;

    @GetMapping
    public PaginaResponse<PratoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return pratoService.listar(nome, categoriaId, ativo, pageable);
    }

    @GetMapping("/{id}")
    public PratoResponse buscar(@PathVariable Long id) {
        return pratoService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PratoResponse cadastrar(@Valid @RequestBody PratoRequest request) {
        return pratoService.cadastrar(request);
    }

    @PutMapping("/{id}")
    public PratoResponse editar(@PathVariable Long id, @Valid @RequestBody PratoRequest request) {
        return pratoService.editar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        pratoService.inativar(id);
    }

    @PatchMapping("/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Long id) {
        pratoService.ativar(id);
    }
}
