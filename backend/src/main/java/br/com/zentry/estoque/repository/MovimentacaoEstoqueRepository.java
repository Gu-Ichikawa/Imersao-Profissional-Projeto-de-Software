package br.com.zentry.estoque.repository;

import br.com.zentry.estoque.model.MovimentacaoEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    @EntityGraph(attributePaths = "ajuste")
    Page<MovimentacaoEstoque> findByInsumoId(Long insumoId, Pageable pageable);
}
