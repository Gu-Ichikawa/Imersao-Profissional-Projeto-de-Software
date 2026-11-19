package br.com.zentry.cardapio.model;

import br.com.zentry.estoque.model.Insumo;
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

@Entity
@Table(name = "item_ficha_tecnica")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemFichaTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Na mesma unidade do insumo (sem conversão)
    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ficha_tecnica_id", nullable = false)
    private FichaTecnica fichaTecnica;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;

    // Só a FichaTecnica cria e altera itens, para o custo total nunca ficar desatualizado
    ItemFichaTecnica(FichaTecnica fichaTecnica, Insumo insumo, BigDecimal quantidade) {
        this.fichaTecnica = fichaTecnica;
        this.insumo = insumo;
        this.quantidade = quantidade;
    }

    void alterarQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    /** Custo deste item: quantidade x custo unitário do insumo. */
    public BigDecimal custo() {
        return quantidade.multiply(insumo.getCustoUnitario());
    }
}
