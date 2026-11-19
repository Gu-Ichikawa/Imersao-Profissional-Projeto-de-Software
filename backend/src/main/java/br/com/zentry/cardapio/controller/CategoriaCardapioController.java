package br.com.zentry.cardapio.controller;

import br.com.zentry.cardapio.dto.CategoriaCardapioRequest;
import br.com.zentry.cardapio.dto.CategoriaCardapioResponse;
import br.com.zentry.cardapio.service.CategoriaCardapioService;
import br.com.zentry.compartilhado.dto.PaginaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias-cardapio")
@RequiredArgsConstructor
public class CategoriaCardapioController {

    private final CategoriaCardapioService categoriaCardapioService;

    @GetMapping
    public PaginaResponse<CategoriaCardapioResponse> listar(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return categoriaCardapioService.listar(pageable);
    }

    @GetMapping("/{id}")
    public CategoriaCardapioResponse buscar(@PathVariable Long id) {
        return categoriaCardapioService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaCardapioResponse cadastrar(@Valid @RequestBody CategoriaCardapioRequest request) {
        return categoriaCardapioService.cadastrar(request);
    }

    @PutMapping("/{id}")
    public CategoriaCardapioResponse editar(@PathVariable Long id, @Valid @RequestBody CategoriaCardapioRequest request) {
        return categoriaCardapioService.editar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        categoriaCardapioService.inativar(id);
    }

    @PatchMapping("/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Long id) {
        categoriaCardapioService.ativar(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        categoriaCardapioService.excluir(id);
    }
}
