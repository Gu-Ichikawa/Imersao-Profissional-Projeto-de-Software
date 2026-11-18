package br.com.zentry.cardapio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Entidade mínima criada no SCRUM-101 porque o Prato depende dela.
// O CRUD completo da categoria (dto, service, controller) é do SCRUM-95.
@Entity
@Table(name = "categoria_cardapio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoriaCardapio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String nome;

    @Column(nullable = false)
    private Boolean ativa;
}
