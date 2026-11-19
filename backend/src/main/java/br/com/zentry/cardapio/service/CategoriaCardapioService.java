package br.com.zentry.cardapio.service;

import br.com.zentry.cardapio.dto.CategoriaCardapioRequest;
import br.com.zentry.cardapio.dto.CategoriaCardapioResponse;
import br.com.zentry.cardapio.model.CategoriaCardapio;
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
public class CategoriaCardapioService {

    private final CategoriaCardapioRepository categoriaCardapioRepository;
    private final PratoRepository pratoRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<CategoriaCardapioResponse> listar(Pageable pageable) {
        return PaginaResponse.de(categoriaCardapioRepository.findAll(pageable).map(CategoriaCardapioResponse::de));
    }

    @Transactional(readOnly = true)
    public CategoriaCardapioResponse buscar(Long id) {
        return CategoriaCardapioResponse.de(buscarPorId(id));
    }

    @Transactional
    public CategoriaCardapioResponse cadastrar(CategoriaCardapioRequest request) {
        String nome = request.nome().trim();
        if (categoriaCardapioRepository.existsByNome(nome)) {
            throw nomeJaCadastrado(nome);
        }
        return CategoriaCardapioResponse.de(categoriaCardapioRepository.save(new CategoriaCardapio(nome)));
    }

    @Transactional
    public CategoriaCardapioResponse editar(Long id, CategoriaCardapioRequest request) {
        CategoriaCardapio categoria = buscarPorId(id);
        String nome = request.nome().trim();
        if (categoriaCardapioRepository.existsByNomeAndIdNot(nome, id)) {
            throw nomeJaCadastrado(nome);
        }
        categoria.atualizar(nome);
        return CategoriaCardapioResponse.de(categoria);
    }

    @Transactional
    public void inativar(Long id) {
        buscarPorId(id).inativar();
    }

    @Transactional
    public void ativar(Long id) {
        buscarPorId(id).ativar();
    }

    // RN-CAR-002: com prato vinculado, a categoria só pode ser inativada
    @Transactional
    public void excluir(Long id) {
        CategoriaCardapio categoria = buscarPorId(id);
        if (pratoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException(HttpStatus.CONFLICT, "CATEGORIA_COM_PRATOS",
                    "A categoria " + categoria.getNome() + " tem pratos vinculados e não pode ser excluída. Inative a categoria.");
        }
        categoriaCardapioRepository.delete(categoria);
    }

    private CategoriaCardapio buscarPorId(Long id) {
        return categoriaCardapioRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Categoria", id));
    }

    private RegraNegocioException nomeJaCadastrado(String nome) {
        return new RegraNegocioException(HttpStatus.CONFLICT, "CATEGORIA_JA_CADASTRADA",
                "Já existe uma categoria com o nome " + nome + ".");
    }
}
