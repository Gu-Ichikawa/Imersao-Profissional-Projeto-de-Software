package br.com.zentry.estoque.service;

import br.com.zentry.compartilhado.dto.PaginaResponse;
import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import br.com.zentry.estoque.dto.AjusteEstoqueRequest;
import br.com.zentry.estoque.dto.MovimentacaoEstoqueRequest;
import br.com.zentry.estoque.dto.MovimentacaoEstoqueResponse;
import br.com.zentry.estoque.model.AjusteEstoque;
import br.com.zentry.estoque.model.Insumo;
import br.com.zentry.estoque.model.MovimentacaoEstoque;
import br.com.zentry.estoque.model.TipoMovimentacaoEstoque;
import br.com.zentry.estoque.repository.AjusteEstoqueRepository;
import br.com.zentry.estoque.repository.InsumoRepository;
import br.com.zentry.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.zentry.usuario.service.UsuarioService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MovimentacaoEstoqueService {

    // Sem login no protótipo: toda movimentação fica no nome do usuário de demonstração
    private static final Long USUARIO_DEMO_ID = 1L;

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final AjusteEstoqueRepository ajusteEstoqueRepository;
    // Pelo repository, não pelo InsumoService: o InsumoService chama este service (dependência circular)
    private final InsumoRepository insumoRepository;
    private final UsuarioService usuarioService;

    @Transactional(readOnly = true)
    public PaginaResponse<MovimentacaoEstoqueResponse> listarPorInsumo(Long insumoId, Pageable pageable) {
        if (!insumoRepository.existsById(insumoId)) {
            throw new NaoEncontradoException("Insumo", insumoId);
        }
        return PaginaResponse.de(movimentacaoEstoqueRepository.findByInsumoId(insumoId, pageable)
                .map(MovimentacaoEstoqueResponse::de));
    }

    @Transactional
    public MovimentacaoEstoqueResponse registrar(MovimentacaoEstoqueRequest request) {
        Insumo insumo = buscarInsumo(request.insumoId());
        MovimentacaoEstoque movimentacao = switch (request.tipo()) {
            case ENTRADA -> registrarEntrada(insumo, request.quantidade());
            case RETIRADA -> {
                insumo.diminuirSaldo(request.quantidade());
                yield salvar(TipoMovimentacaoEstoque.RETIRADA, request.quantidade(), insumo);
            }
            default -> throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "TIPO_INVALIDO",
                    "Use ENTRADA ou RETIRADA. O ajuste tem rota própria e a baixa por venda é feita pela venda.");
        };
        return MovimentacaoEstoqueResponse.de(movimentacao);
    }

    @Transactional
    public MovimentacaoEstoqueResponse ajustar(AjusteEstoqueRequest request) {
        Insumo insumo = buscarInsumo(request.insumoId());
        BigDecimal saldoSistema = insumo.getQuantidadeAtual();
        BigDecimal diferenca = request.quantidadeContada().subtract(saldoSistema);
        if (diferenca.signum() == 0) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "QUANTIDADE_INVALIDA",
                    "A quantidade contada é igual ao saldo do sistema. Não há o que ajustar.");
        }
        if (diferenca.signum() > 0) {
            insumo.aumentarSaldo(diferenca);
        } else {
            insumo.diminuirSaldo(diferenca.negate());
        }
        MovimentacaoEstoque movimentacao = salvar(TipoMovimentacaoEstoque.AJUSTE, diferenca.abs(), insumo);
        ajusteEstoqueRepository.save(
                new AjusteEstoque(movimentacao, saldoSistema, request.quantidadeContada(), request.motivo()));
        return MovimentacaoEstoqueResponse.de(movimentacao);
    }

    // Usado pelo cadastro de insumo, para registrar a quantidade inicial
    @Transactional
    public MovimentacaoEstoque registrarEntrada(Insumo insumo, BigDecimal quantidade) {
        insumo.aumentarSaldo(quantidade);
        return salvar(TipoMovimentacaoEstoque.ENTRADA, quantidade, insumo);
    }

    /**
     * Contrato interno: usado pela venda (SCRUM-105, UC-CAR-006).
     * Lança 422 ESTOQUE_INSUFICIENTE se faltar saldo.
     */
    @Transactional
    public MovimentacaoEstoque registrarBaixa(Insumo insumo, BigDecimal quantidade) {
        insumo.diminuirSaldo(quantidade);
        return salvar(TipoMovimentacaoEstoque.BAIXA_POR_VENDA, quantidade, insumo);
    }

    private MovimentacaoEstoque salvar(TipoMovimentacaoEstoque tipo, BigDecimal quantidade, Insumo insumo) {
        return movimentacaoEstoqueRepository.save(
                new MovimentacaoEstoque(tipo, quantidade, insumo, usuarioService.buscarPorId(USUARIO_DEMO_ID)));
    }

    private Insumo buscarInsumo(Long insumoId) {
        return insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "INSUMO_NAO_ENCONTRADO", "Insumo com id " + insumoId + " não encontrado."));
    }
}
