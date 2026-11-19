package br.com.zentry.cardapio.service;

import br.com.zentry.cardapio.dto.VendaResponse;
import br.com.zentry.cardapio.model.ItemFichaTecnica;
import br.com.zentry.cardapio.model.Prato;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import br.com.zentry.estoque.model.Insumo;
import br.com.zentry.estoque.service.MovimentacaoEstoqueService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-CAR-006: dar baixa automática de insumos por venda.
 *
 * Depende de dois contratos de outros tickets (ajustar os nomes se forem diferentes):
 * - SCRUM-103: FichaTecnicaService.listarItensDoPrato(pratoId), devolve os itens (insumo + quantidade)
 *   da ficha do prato, ou lista vazia se o prato não tiver ficha
 * - SCRUM-99: MovimentacaoEstoqueService.registrarBaixa(insumo, quantidade), diminui o saldo
 *   e grava a movimentação BAIXA_POR_VENDA com o usuário de demonstração
 *
 * O Cardápio chama os services do Estoque, nunca o repository dele (regra do monólito modular).
 */
@Service
@RequiredArgsConstructor
public class VendaService {

    private final PratoService pratoService;
    private final FichaTecnicaService fichaTecnicaService;
    private final MovimentacaoEstoqueService movimentacaoEstoqueService;

    /**
     * Registra a venda de N unidades de um prato.
     * Tudo numa transação só: se faltar saldo de qualquer insumo, a venda é recusada
     * e nenhum insumo é baixado (RNF-CAR-005). Quando o Caixa existir, ele chama este
     * mesmo método ao fechar a comanda.
     */
    @Transactional
    public VendaResponse registrarVenda(Long pratoId, int quantidade) {
        Prato prato = pratoService.buscarPorId(pratoId);
        if (!Boolean.TRUE.equals(prato.getAtivo())) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "PRATO_INATIVO",
                    "O prato " + prato.getNome() + " está inativo e não pode ser vendido.");
        }

        List<ItemFichaTecnica> itens = fichaTecnicaService.listarItensDoPrato(pratoId);
        if (itens.isEmpty()) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "PRATO_SEM_FICHA",
                    "O prato " + prato.getNome() + " não tem ficha técnica. Monte a ficha antes de vender.");
        }

        BigDecimal unidades = BigDecimal.valueOf(quantidade);

        // Insumo sem controle de estoque é pulado (passo 8 do diagrama, RN-CAR-006)
        List<ItemFichaTecnica> itensControlados = itens.stream()
                .filter(item -> Boolean.TRUE.equals(item.getInsumo().getControlaEstoque()))
                .toList();

        // Confere o saldo de todos antes de baixar qualquer um, para avisar tudo o que falta de uma vez
        List<String> semSaldo = itensControlados.stream()
                .filter(item -> item.getInsumo().getQuantidadeAtual()
                        .compareTo(item.getQuantidade().multiply(unidades)) < 0)
                .map(item -> item.getInsumo().getNome())
                .toList();
        if (!semSaldo.isEmpty()) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "ESTOQUE_INSUFICIENTE",
                    "Estoque insuficiente para: " + String.join(", ", semSaldo) + ". Nenhum insumo foi baixado.");
        }

        List<VendaResponse.BaixaInsumo> baixas = new ArrayList<>();
        for (ItemFichaTecnica item : itensControlados) {
            Insumo insumo = item.getInsumo();
            BigDecimal total = item.getQuantidade().multiply(unidades);
            movimentacaoEstoqueService.registrarBaixa(insumo, total);
            baixas.add(new VendaResponse.BaixaInsumo(
                    insumo.getId(),
                    insumo.getNome(),
                    insumo.getUnidadeMedida().name(),
                    total,
                    insumo.getQuantidadeAtual()));
        }

        return new VendaResponse(prato.getId(), prato.getNome(), quantidade, baixas);
    }
}
