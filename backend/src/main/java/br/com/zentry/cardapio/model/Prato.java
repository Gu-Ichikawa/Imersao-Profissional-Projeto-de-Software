package br.com.zentry.cardapio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// taxa_cmv e imagem_url existem na tabela, mas ficam fora desta entrega (não são mapeadas)
@Entity
@Table(name = "prato")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Prato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "preco_venda", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoVenda;

    @Column(nullable = false)
    private Boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_cardapio_id", nullable = false)
    private CategoriaCardapio categoria;

    public Prato(String nome, String descricao, BigDecimal precoVenda, CategoriaCardapio categoria) {
        atualizar(nome, descricao, precoVenda, categoria);
        this.ativo = true;
    }

    public void atualizar(String nome, String descricao, BigDecimal precoVenda, CategoriaCardapio categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.precoVenda = precoVenda;
        this.categoria = categoria;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }
}
