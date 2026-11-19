package br.com.zentry.estoque.model;

import br.com.zentry.usuario.model.Usuario;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// lote_id e comanda_id existem na tabela, mas ficam vazios nesta entrega (não são mapeados)
@Entity
@Table(name = "movimentacao_estoque")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private TipoMovimentacaoEstoque tipo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private Usuario responsavel;

    // Só existe quando o tipo é AJUSTE
    @OneToOne(mappedBy = "movimentacao", fetch = FetchType.LAZY)
    private AjusteEstoque ajuste;

    public MovimentacaoEstoque(TipoMovimentacaoEstoque tipo, BigDecimal quantidade, Insumo insumo, Usuario responsavel) {
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.dataHora = LocalDateTime.now();
        this.insumo = insumo;
        this.responsavel = responsavel;
    }

    void vincularAjuste(AjusteEstoque ajuste) {
        this.ajuste = ajuste;
    }
}
