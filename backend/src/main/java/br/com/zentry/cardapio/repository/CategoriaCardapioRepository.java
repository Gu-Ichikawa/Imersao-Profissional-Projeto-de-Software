package br.com.zentry.cardapio.repository;

import br.com.zentry.cardapio.model.CategoriaCardapio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaCardapioRepository extends JpaRepository<CategoriaCardapio, Long> {

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
