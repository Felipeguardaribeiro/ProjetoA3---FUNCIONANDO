package projeto.faculdade.banco.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import projeto.faculdade.banco.model.Conta;
import projeto.faculdade.banco.model.Transacao;
import projeto.faculdade.banco.model.Usuario;
import projeto.faculdade.banco.repository.ContaRepository;
import projeto.faculdade.banco.repository.TransacaoRepository;
import projeto.faculdade.banco.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    public Optional<Usuario> login(String email, String senha) {
        return usuarioRepository.findByEmail(email).filter(usuario -> usuario.getSenha().equals(senha));
    }

    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }

    // Função para encontrar a conta pelo ID do usuário
    public Optional<Conta> findContaByUsuarioId(Long usuarioId) {
        return contaRepository.findByUsuarioId(usuarioId);
    }

    public boolean transferir(Long idUsuarioOrigem, Long idUsuarioDestino, BigDecimal valor) {
        logger.info("Iniciando transferência de {} de {} para {}", valor, idUsuarioOrigem, idUsuarioDestino);

        Optional<Conta> contaOrigemOptional = contaRepository.findByUsuarioId(idUsuarioOrigem);
        Optional<Conta> contaDestinoOptional = contaRepository.findByUsuarioId(idUsuarioDestino);

        if (contaOrigemOptional.isPresent() && contaDestinoOptional.isPresent()) {
            Conta contaOrigem = contaOrigemOptional.get();
            Conta contaDestino = contaDestinoOptional.get();

            // Verifica se há saldo suficiente
            if (contaOrigem.getSaldo().compareTo(valor) >= 0) {
                contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
                contaDestino.setSaldo(contaDestino.getSaldo().add(valor));
                contaRepository.save(contaOrigem);
                contaRepository.save(contaDestino);

                // Salvar a transação no histórico
                Transacao transacao = new Transacao();
                transacao.setIdUsuarioOrigem(idUsuarioOrigem);
                transacao.setIdUsuarioDestino(idUsuarioDestino);
                transacao.setValor(valor);
                transacao.setTipo("Transferência"); // Define o tipo da transação
                transacao.setData(new Date());

                try {
                    Transacao savedTransacao = transacaoRepository.save(transacao); // Salva a transação no banco de dados
                    if (savedTransacao != null) {
                        logger.info("Transação salva com sucesso: {}", savedTransacao.getId());
                        return true; // Retorna verdadeiro se a transferência foi bem-sucedida
                    }
                } catch (Exception e) {
                    logger.error("Erro ao salvar a transação: {}", e.getMessage());
                }
            } else {
                logger.error("Saldo insuficiente para a transferência.");
            }
        } else {
            logger.error("Uma das contas não foi encontrada.");
        }
        return false; // Retorna falso se a transferência falhou
    }

    // Método de saque
    public boolean sacar(Long usuarioId, BigDecimal valor) {
        Optional<Conta> contaOptional = contaRepository.findByUsuarioId(usuarioId);

        if (contaOptional.isPresent()) {
            Conta conta = contaOptional.get();

            // Verifica se há saldo suficiente para o saque
            if (conta.getSaldo().compareTo(valor) >= 0) {
                conta.setSaldo(conta.getSaldo().subtract(valor)); // Subtrai o valor do saldo da conta
                contaRepository.save(conta);

                // Registra a transação do saque
                Transacao transacao = new Transacao();
                transacao.setIdUsuarioOrigem(usuarioId);
                transacao.setValor(valor);
                transacao.setTipo("Saque");
                transacao.setData(new Date());
                transacaoRepository.save(transacao);

                return true; // Saque realizado com sucesso
            } else {
                logger.warn("Saldo insuficiente para o saque.");
            }
        } else {
            logger.warn("Conta não encontrada para o usuário com ID: {}", usuarioId);
        }
        return false; // Saque falhou
    }

    // Método de depósito
    public boolean depositar(Long usuarioId, BigDecimal valor) {
        Optional<Conta> contaOptional = contaRepository.findByUsuarioId(usuarioId);

        if (contaOptional.isPresent()) {
            Conta conta = contaOptional.get();
            conta.setSaldo(conta.getSaldo().add(valor)); // Adiciona o valor ao saldo da conta
            contaRepository.save(conta);

            // Registra a transação do depósito
            Transacao transacao = new Transacao();
            transacao.setIdUsuarioOrigem(usuarioId);
            transacao.setValor(valor);
            transacao.setTipo("Depósito");
            transacao.setData(new Date());
            transacaoRepository.save(transacao);

            return true; // Depósito realizado com sucesso
        } else {
            logger.warn("Conta não encontrada para o usuário com ID: {}", usuarioId);
        }
        return false; // Depósito falhou
    }
}
