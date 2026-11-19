package br.com.zentry.cardapio.service;

import br.com.zentry.cardapio.dto.PratoRequest;
import br.com.zentry.cardapio.dto.PratoResponse;
import br.com.zentry.cardapio.model.CategoriaCardapio;
import br.com.zentry.cardapio.model.Prato;
import br.com.zentry.cardapio.repository.CategoriaCardapioRepository;
import br.com.zentry.cardapio.repository.PratoRepository;
import br.com.zentry.compartilhado.dto.PaginaResponse;
import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.compartilhado.erro.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PratoService {

    private final PratoRepository pratoRepository;
    private final CategoriaCardapioRepository categoriaCardapioRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<PratoResponse> listar(String nome, Long categoriaId, Boolean ativo, Pageable pageable) {
        String filtroNome = (nome == null || nome.isBlank()) ? null : nome.trim();
        return PaginaResponse.de(pratoRepository.buscar(filtroNome, categoriaId, ativo, pageable).map(PratoResponse::de));
    }

    @Transactional(readOnly = true)
    public PratoResponse buscar(Long id) {
        return PratoResponse.de(buscarPorId(id));
    }

    @Transactional
    public PratoResponse cadastrar(PratoRequest request) {
        Prato prato = new Prato(
                request.nome().trim(),
                normalizarDescricao(request.descricao()),
                request.precoVenda(),
                buscarCategoriaAtiva(request.categoriaCardapioId()));
        return PratoResponse.de(pratoRepository.save(prato));
    }

    @Transactional
    public PratoResponse editar(Long id, PratoRequest request) {
        Prato prato = buscarPorId(id);
        prato.atualizar(
                request.nome().trim(),
                normalizarDescricao(request.descricao()),
                request.precoVenda(),
                buscarCategoriaAtiva(request.categoriaCardapioId()));
        return PratoResponse.de(prato);
    }

    @Transactional
    public void inativar(Long id) {
        buscarPorId(id).inativar();
    }

    @Transactional
    public void ativar(Long id) {
        buscarPorId(id).ativar();
    }

    /**
     * Contrato interno: usado pela ficha técnica (SCRUM-103) e pela venda (SCRUM-105).
     * Lança 404 se o prato não existir.
     */
    @Transactional(readOnly = true)
    public Prato buscarPorId(Long id) {
        return pratoRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Prato", id));
    }

    private CategoriaCardapio buscarCategoriaAtiva(Long categoriaId) {
        CategoriaCardapio categoria = categoriaCardapioRepository.findById(categoriaId)
                .orElseThrow(() -> new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "CATEGORIA_NAO_ENCONTRADA", "Categoria do cardápio com id " + categoriaId + " não encontrada."));
        if (!Boolean.TRUE.equals(categoria.getAtiva())) {
            throw new RegraNegocioException(HttpStatus.UNPROCESSABLE_ENTITY, "CATEGORIA_INATIVA",
                    "A categoria " + categoria.getNome() + " está inativa. Escolha uma categoria ativa.");
        }
        return categoria;
    }

    private String normalizarDescricao(String descricao) {
        return (descricao == null || descricao.isBlank()) ? null : descricao.trim();
    }
}
