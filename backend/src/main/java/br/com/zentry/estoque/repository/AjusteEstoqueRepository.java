package br.com.zentry.estoque.repository;

import br.com.zentry.estoque.model.AjusteEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AjusteEstoqueRepository extends JpaRepository<AjusteEstoque, Long> {
}
