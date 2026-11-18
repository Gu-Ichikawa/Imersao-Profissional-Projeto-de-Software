package br.com.zentry.estoque.controller;

import br.com.zentry.estoque.dto.CategoriaInsumoResponse;
import br.com.zentry.estoque.service.CategoriaInsumoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias-insumo")
@RequiredArgsConstructor
public class CategoriaInsumoController {

    private final CategoriaInsumoService categoriaInsumoService;

    @GetMapping
    public List<CategoriaInsumoResponse> listar() {
        return categoriaInsumoService.listar();
    }
}
