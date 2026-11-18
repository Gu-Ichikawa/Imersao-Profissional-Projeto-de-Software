package br.com.zentry.estoque.service;

import br.com.zentry.estoque.dto.CategoriaInsumoResponse;
import br.com.zentry.estoque.repository.CategoriaInsumoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaInsumoService {

    private final CategoriaInsumoRepository categoriaInsumoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaInsumoResponse> listar() {
        return categoriaInsumoRepository.findAll(Sort.by("nome")).stream()
                .map(CategoriaInsumoResponse::de)
                .toList();
    }
}
