package br.com.zentry.cardapio.repository;

import br.com.zentry.cardapio.model.FichaTecnica;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FichaTecnicaRepository extends JpaRepository<FichaTecnica, Long> {

    @EntityGraph(attributePaths = {"itens", "itens.insumo"})
    Optional<FichaTecnica> findByPratoId(Long pratoId);
}
