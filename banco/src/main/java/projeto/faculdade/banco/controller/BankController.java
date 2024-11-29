package projeto.faculdade.banco.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import projeto.faculdade.banco.model.Conta;
import projeto.faculdade.banco.model.Transacao;
import projeto.faculdade.banco.model.Usuario;
import projeto.faculdade.banco.repository.ContaRepository;
import projeto.faculdade.banco.repository.TransacaoRepository;
import projeto.faculdade.banco.repository.UsuarioRepository; // Importar o repositório de Usuario

@Controller
@RequestMapping("/api/bank")
public class BankController {

    @Autowired
    private UsuarioRepository usuarioRepository;  // Repositório de Usuario

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("error", ""); 
        return "login"; 
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String senha, Model model, HttpSession session) {
        System.out.println("Tentando login com email: " + email);
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);  // Alterado para buscar pelo email

        if (usuario.isPresent()) {
            session.setAttribute("usuarioId", usuario.get().getId());
            Optional<Conta> conta = contaRepository.findByUsuarioId(usuario.get().getId());
            model.addAttribute("usuario", usuario.get());
            model.addAttribute("saldo", conta.map(Conta::getSaldo).orElse(BigDecimal.ZERO));
            return "conta"; 
        } else {
            System.out.println("Login falhou para o email: " + email);
            model.addAttribute("error", "Email ou senha inválidos");
            return "login"; 
        }
    }

    @GetMapping("/conta")
    public String showContaPage(HttpSession session, Model model) {
        Long idUsuarioLogado = (Long) session.getAttribute("usuarioId");

        if (idUsuarioLogado == null) {
            model.addAttribute("error", "Usuário não está logado.");
            return "login"; 
        }

        Optional<Usuario> usuario = usuarioRepository.findById(idUsuarioLogado);
        if (usuario.isPresent()) {
            Optional<Conta> conta = contaRepository.findByUsuarioId(idUsuarioLogado);
            model.addAttribute("saldo", conta.map(Conta::getSaldo).orElse(BigDecimal.ZERO));
            model.addAttribute("usuario", usuario.get());
            return "conta"; 
        } else {
            model.addAttribute("error", "Usuário não encontrado");
            return "login"; 
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/api/bank/login"; 
    }

    @GetMapping("/transferir")
    public String showTransferPage(HttpSession session, Model model) {
        Long idUsuarioLogado = (Long) session.getAttribute("usuarioId");

        if (idUsuarioLogado == null) {
            model.addAttribute("error", "Usuário não está logado.");
            return "login"; 
        }

        return "transferir"; 
    }

    @PostMapping("/transferir")
    public String transferir(@RequestParam String emailDestino, @RequestParam BigDecimal valor, Model model, HttpSession session) {
        Long idUsuarioLogado = (Long) session.getAttribute("usuarioId");

        if (idUsuarioLogado == null) {
            model.addAttribute("error", "Usuário não está logado.");
            return "login"; 
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("message", "Valor inválido para transferência.");
            return "conta"; 
        }

        // Busca o usuário de destino pelo email
        Optional<Usuario> usuarioDestino = usuarioRepository.findByEmail(emailDestino);

        if (!usuarioDestino.isPresent()) {
            model.addAttribute("message", "Usuário de destino não encontrado.");
            return "conta"; // Retorna para a página da conta se o usuário não for encontrado
        }

        boolean sucesso = transferirSaldo(idUsuarioLogado, usuarioDestino.get().getId(), valor);  // Chama a lógica de transferência
        if (sucesso) {
            model.addAttribute("message", "Transferência realizada com sucesso!");
        } else {
            model.addAttribute("message", "Falha na transferência. Verifique se o saldo é suficiente e se o usuário existe.");
        }

        Optional<Usuario> usuario = usuarioRepository.findById(idUsuarioLogado);
        if (usuario.isPresent()) {
            Optional<Conta> conta = contaRepository.findByUsuarioId(idUsuarioLogado);
            model.addAttribute("saldo", conta.map(Conta::getSaldo).orElse(BigDecimal.ZERO));
            model.addAttribute("usuario", usuario.get());
        } else {
            model.addAttribute("error", "Usuário não encontrado");
            return "login"; 
        }

        return "conta"; 
    }

    // Novo método para realizar a transferência
    private boolean transferirSaldo(Long idUsuarioOrigem, Long idUsuarioDestino, BigDecimal valor) {
        // Lógica para a transferência de saldo entre contas
        Optional<Conta> contaOrigemOpt = contaRepository.findByUsuarioId(idUsuarioOrigem);
        Optional<Conta> contaDestinoOpt = contaRepository.findByUsuarioId(idUsuarioDestino);

        if (contaOrigemOpt.isPresent() && contaDestinoOpt.isPresent()) {
            Conta contaOrigem = contaOrigemOpt.get();
            Conta contaDestino = contaDestinoOpt.get();

            if (contaOrigem.getSaldo().compareTo(valor) >= 0) {
                // Realiza a transferência de saldo
                contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
                contaDestino.setSaldo(contaDestino.getSaldo().add(valor));

                contaRepository.save(contaOrigem);
                contaRepository.save(contaDestino);

                // Registra a transação
                Transacao transacao = new Transacao();
                transacao.setIdUsuarioOrigem(idUsuarioOrigem);
                transacao.setIdUsuarioDestino(idUsuarioDestino);
                transacao.setValor(valor);
                transacao.setTipo("Transferência");
                transacao.setData(new java.util.Date());
                transacaoRepository.save(transacao);

                return true;
            }
        }
        return false;
    }

    @GetMapping("/extrato")
    public String showExtratoPage(HttpSession session, Model model) {
        Long idUsuarioLogado = (Long) session.getAttribute("usuarioId");

        if (idUsuarioLogado == null) {
            model.addAttribute("error", "Usuário não está logado.");
            return "login"; // Redireciona para a página de login se o usuário não estiver logado
        }

        Optional<Conta> conta = contaRepository.findByUsuarioId(idUsuarioLogado);
        if (conta.isPresent()) {
            // Usando o método que traz as transações ordenadas por data
            List<Transacao> transacoes = transacaoRepository.findByIdUsuarioOrigemOrIdUsuarioDestinoOrderByDataDesc(idUsuarioLogado, idUsuarioLogado);
            model.addAttribute("transacoes", transacoes);
            model.addAttribute("usuario", conta.get().getUsuario());
            return "extrato"; // Retorna a view "extrato.html" com as transações do usuário
        } else {
            model.addAttribute("error", "Conta não encontrada");
            return "conta"; // Retorna à página da conta se a conta não for encontrada
        }
    }

}
