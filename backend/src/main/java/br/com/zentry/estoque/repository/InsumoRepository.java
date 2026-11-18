package br.com.zentry.estoque.repository;

import br.com.zentry.estoque.model.Insumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            select i from Insumo i
            where (:nome is null or lower(i.nome) like lower(concat('%', :nome, '%')))
              and (:ativo is null or i.ativo = :ativo)
            """)
    Page<Insumo> buscar(@Param("nome") String nome, @Param("ativo") Boolean ativo, Pageable pageable);

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);
}
