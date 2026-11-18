package br.com.zentry.estoque.repository;

import br.com.zentry.estoque.model.CategoriaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaInsumoRepository extends JpaRepository<CategoriaInsumo, Long> {
}
