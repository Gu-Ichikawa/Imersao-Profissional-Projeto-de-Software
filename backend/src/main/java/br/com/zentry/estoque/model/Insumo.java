package br.com.zentry.estoque.model;

import br.com.zentry.compartilhado.erro.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "insumo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Insumo {

    private static final BigDecimal FATOR_ATENCAO = new BigDecimal("1.5");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 50, unique = true)
    private String sku;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "unidade_medida", nullable = false, length = 10)
    private UnidadeMedida unidadeMedida;

    @Column(nullable = false)
    private Boolean perecivel;

    @Column(name = "estoque_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal estoqueMinimo;

    @Column(name = "custo_unitario", nullable = false, precision = 12, scale = 4)
    private BigDecimal custoUnitario;

    @Column(name = "controla_estoque", nullable = false)
    private Boolean controlaEstoque;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(name = "quantidade_atual", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeAtual;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_insumo_id", nullable = false)
    private CategoriaInsumo categoria;

    public Insumo(String nome, String sku, UnidadeMedida unidadeMedida, Boolean perecivel,
                  BigDecimal estoqueMinimo, BigDecimal custoUnitario, Boolean controlaEstoque,
                  CategoriaInsumo categoria, BigDecimal quantidadeInicial) {
        atualizar(nome, sku, unidadeMedida, perecivel, estoqueMinimo, custoUnitario, controlaEstoque, categoria);
        this.ativo = true;
        this.quantidadeAtual = quantidadeInicial == null ? BigDecimal.ZERO : quantidadeInicial;
    }

    public void atualizar(String nome, String sku, UnidadeMedida unidadeMedida, Boolean perecivel,
                          BigDecimal estoqueMinimo, BigDecimal custoUnitario, Boolean controlaEstoque,
                          CategoriaInsumo categoria) {
        this.nome = nome;
        this.sku = sku;
        this.unidadeMedida = unidadeMedida;
        this.perecivel = perecivel;
        this.estoqueMinimo = estoqueMinimo;
        this.custoUnitario = custoUnitario;
        this.controlaEstoque = controlaEstoque;
        this.categoria = categoria;
    }

    public void inativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public NivelEstoque nivelEstoque() {
        if (!Boolean.TRUE.equals(controlaEstoque)) {
            return null;
        }
        if (quantidadeAtual.compareTo(estoqueMinimo) <= 0) {
            return NivelEstoque.REPOR;
        }
        if (quantidadeAtual.compareTo(estoqueMinimo.multiply(FATOR_ATENCAO)) <= 0) {
            return NivelEstoque.ATENCAO;
        }
        return NivelEstoque.SUFICIENTE;
    }

    public void aumentarSaldo(BigDecimal quantidade) {
        validarQuantidade(quantidade);
        this.quantidadeAtual = quantidadeAtual.add(quantidade);
    }

    public void diminuirSaldo(BigDecimal quantidade) {
        validarQuantidade(quantidade);
        if (quantidadeAtual.compareTo(quantidade) < 0) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "ESTOQUE_INSUFICIENTE",
                    "Estoque insuficiente para: " + nome + ".");
        }
        this.quantidadeAtual = quantidadeAtual.subtract(quantidade);
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "QUANTIDADE_INVALIDA",
                    "A quantidade deve ser maior que zero.");
        }
    }
}
