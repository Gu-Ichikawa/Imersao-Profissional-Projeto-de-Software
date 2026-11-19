package br.com.zentry.usuario.service;

import br.com.zentry.compartilhado.erro.NaoEncontradoException;
import br.com.zentry.usuario.model.Usuario;
import br.com.zentry.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Usado pela movimentação de estoque para gravar o responsável
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Usuário", id));
    }
}
