package br.com.zentry.estoque.model;

import br.com.zentry.usuario.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// Sem login no protótipo: o ajuste nasce EFETIVADO e sem aprovador
@Entity
@Table(name = "ajuste_estoque")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AjusteEstoque {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movimentacao_estoque_id")
    private MovimentacaoEstoque movimentacao;

    @Column(name = "saldo_sistema", nullable = false, precision = 12, scale = 3)
    private BigDecimal saldoSistema;

    @Column(name = "quantidade_contada", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeContada;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal diferenca;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private MotivoAjuste motivo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 25)
    private StatusAjuste status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprovador_id")
    private Usuario aprovador;

    public AjusteEstoque(MovimentacaoEstoque movimentacao, BigDecimal saldoSistema, BigDecimal quantidadeContada,
                         MotivoAjuste motivo) {
        this.movimentacao = movimentacao;
        this.saldoSistema = saldoSistema;
        this.quantidadeContada = quantidadeContada;
        this.diferenca = quantidadeContada.subtract(saldoSistema);
        this.motivo = motivo;
        this.status = StatusAjuste.EFETIVADO;
        movimentacao.vincularAjuste(this);
    }
}
