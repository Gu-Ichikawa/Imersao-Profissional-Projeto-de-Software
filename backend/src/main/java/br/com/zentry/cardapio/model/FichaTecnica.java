package br.com.zentry.cardapio.model;

import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import br.com.zentry.estoque.model.Insumo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "ficha_tecnica")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FichaTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "custo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal custoTotal;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prato_id", nullable = false, unique = true)
    private Prato prato;

    @OneToMany(mappedBy = "fichaTecnica", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<ItemFichaTecnica> itens = new ArrayList<>();

    public FichaTecnica(Prato prato) {
        this.prato = prato;
        this.custoTotal = BigDecimal.ZERO.setScale(2);
        this.atualizadaEm = LocalDateTime.now();
    }

    public void adicionarItem(Insumo insumo, BigDecimal quantidade) {
        validarQuantidade(quantidade);
        boolean jaNaFicha = itens.stream().anyMatch(i -> i.getInsumo().getId().equals(insumo.getId()));
        if (jaNaFicha) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "INSUMO_JA_NA_FICHA",
                    insumo.getNome() + " já está na ficha técnica deste prato.");
        }
        itens.add(new ItemFichaTecnica(this, insumo, quantidade));
        recalcular();
    }

    public void alterarQuantidade(Long itemId, BigDecimal quantidade) {
        validarQuantidade(quantidade);
        buscarItem(itemId).alterarQuantidade(quantidade);
        recalcular();
    }

    public void removerItem(Long itemId) {
        itens.remove(buscarItem(itemId));
        recalcular();
    }

    /** Soma de (quantidade x custo unitário) de cada insumo, com 2 casas. */
    public BigDecimal calcularCusto() {
        return itens.stream()
                .map(ItemFichaTecnica::custo)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void recalcular() {
        this.custoTotal = calcularCusto();
        this.atualizadaEm = LocalDateTime.now();
    }

    private ItemFichaTecnica buscarItem(Long itemId) {
        return itens.stream()
                .filter(i -> i.getId() != null && i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NaoEncontradoException("Item da ficha técnica", itemId));
    }

    private void validarQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "QUANTIDADE_INVALIDA",
                    "A quantidade deve ser maior que zero.");
        }
    }
}
