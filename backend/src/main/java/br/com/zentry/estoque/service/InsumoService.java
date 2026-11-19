package br.com.zentry.estoque.service;

import br.com.zentry.compartilhado.dto.PaginaResponse;
import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import br.com.zentry.estoque.dto.InsumoRequest;
import br.com.zentry.estoque.dto.InsumoResponse;
import br.com.zentry.estoque.model.CategoriaInsumo;
import br.com.zentry.estoque.model.Insumo;
import br.com.zentry.estoque.repository.CategoriaInsumoRepository;
import br.com.zentry.estoque.repository.InsumoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InsumoService {

    private final InsumoRepository insumoRepository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<InsumoResponse> listar(String nome, Boolean ativo, Pageable pageable) {
        String filtroNome = (nome == null || nome.isBlank()) ? null : nome.trim();
        return PaginaResponse.de(insumoRepository.buscar(filtroNome, ativo, pageable).map(InsumoResponse::de));
    }

    @Transactional(readOnly = true)
    public InsumoResponse buscar(Long id) {
        return InsumoResponse.de(buscarPorId(id));
    }

    @Transactional
    public InsumoResponse cadastrar(InsumoRequest request) {
        String sku = normalizarSku(request.sku());
        if (sku != null && insumoRepository.existsBySku(sku)) {
            throw skuJaCadastrado(sku);
        }
        Insumo insumo = new Insumo(
                request.nome().trim(),
                sku,
                request.unidadeMedida(),
                request.perecivel(),
                request.estoqueMinimo(),
                request.custoUnitario(),
                request.controlaEstoque(),
                buscarCategoria(request.categoriaInsumoId()),
                request.quantidadeInicial());
        // TODO SCRUM-99: registrar uma ENTRADA com a quantidade inicial
        return InsumoResponse.de(insumoRepository.save(insumo));
    }

    @Transactional
    public InsumoResponse editar(Long id, InsumoRequest request) {
        Insumo insumo = buscarPorId(id);
        String sku = normalizarSku(request.sku());
        if (sku != null && insumoRepository.existsBySkuAndIdNot(sku, id)) {
            throw skuJaCadastrado(sku);
        }
        insumo.atualizar(
                request.nome().trim(),
                sku,
                request.unidadeMedida(),
                request.perecivel(),
                request.estoqueMinimo(),
                request.custoUnitario(),
                request.controlaEstoque(),
                buscarCategoria(request.categoriaInsumoId()));
        return InsumoResponse.de(insumo);
    }

    @Transactional
    public void inativar(Long id) {
        buscarPorId(id).inativar();
    }

    @Transactional
    public void ativar(Long id) {
        buscarPorId(id).ativar();
    }

    // Usado pela ficha técnica e pela venda
    @Transactional(readOnly = true)
    public Insumo buscarPorId(Long id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Insumo", id));
    }

    private CategoriaInsumo buscarCategoria(Long categoriaId) {
        return categoriaInsumoRepository.findById(categoriaId)
                .orElseThrow(() -> new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "CATEGORIA_NAO_ENCONTRADA", "Categoria de insumo com id " + categoriaId + " não encontrada."));
    }

    private String normalizarSku(String sku) {
        return (sku == null || sku.isBlank()) ? null : sku.trim();
    }

    private RegraNegocioException skuJaCadastrado(String sku) {
        return new RegraNegocioException(HttpStatus.CONFLICT, "SKU_JA_CADASTRADO",
                "Já existe um insumo com o SKU " + sku + ".");
    }
}
