package br.com.zentry.estoque.controller;

import br.com.zentry.compartilhado.dto.PaginaResponse;
import br.com.zentry.estoque.dto.InsumoRequest;
import br.com.zentry.estoque.dto.InsumoResponse;
import br.com.zentry.estoque.service.InsumoService;
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
@RequestMapping("/api/insumos")
@RequiredArgsConstructor
public class InsumoController {

    private final InsumoService insumoService;

    @GetMapping
    public PaginaResponse<InsumoResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return insumoService.listar(nome, ativo, pageable);
    }

    @GetMapping("/{id}")
    public InsumoResponse buscar(@PathVariable Long id) {
        return insumoService.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InsumoResponse cadastrar(@Valid @RequestBody InsumoRequest request) {
        return insumoService.cadastrar(request);
    }

    @PutMapping("/{id}")
    public InsumoResponse editar(@PathVariable Long id, @Valid @RequestBody InsumoRequest request) {
        return insumoService.editar(id, request);
    }

    @PatchMapping("/{id}/inativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inativar(@PathVariable Long id) {
        insumoService.inativar(id);
    }

    @PatchMapping("/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Long id) {
        insumoService.ativar(id);
    }
}
