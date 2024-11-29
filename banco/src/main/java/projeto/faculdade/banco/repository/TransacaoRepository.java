package projeto.faculdade.banco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import projeto.faculdade.banco.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    // Método para encontrar todas as transações de um usuário específico como origem
    List<Transacao> findByIdUsuarioOrigem(Long idUsuario);

    // Método para encontrar todas as transações de um usuário específico como destino
    List<Transacao> findByIdUsuarioDestino(Long idUsuario);

    // Método para encontrar todas as transações de um usuário, tanto como origem quanto destino, ordenadas pela data (mais recente primeiro)
    List<Transacao> findByIdUsuarioOrigemOrIdUsuarioDestinoOrderByDataDesc(Long idUsuarioLogado, Long idUsuarioLogadoDestino);
}
