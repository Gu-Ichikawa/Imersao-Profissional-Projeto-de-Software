package br.com.zentry.cardapio.repository;

import br.com.zentry.cardapio.model.Prato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PratoRepository extends JpaRepository<Prato, Long> {

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            select p from Prato p
            where (:nome is null or lower(p.nome) like lower(concat('%', :nome, '%')))
              and (:categoriaId is null or p.categoria.id = :categoriaId)
              and (:ativo is null or p.ativo = :ativo)
            """)
    Page<Prato> buscar(@Param("nome") String nome, @Param("categoriaId") Long categoriaId,
                       @Param("ativo") Boolean ativo, Pageable pageable);
}
