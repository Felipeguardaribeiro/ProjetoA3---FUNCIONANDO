package projeto.faculdade.banco.controller;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import projeto.faculdade.banco.model.Conta;
import projeto.faculdade.banco.model.Usuario;
import projeto.faculdade.banco.service.UsuarioService;

@Controller
@RequestMapping("/api/atm")
public class AtmController {

    @Autowired
    private UsuarioService usuarioService;

    @RequestMapping("/login")
    public String atmLogin(HttpSession session, Model model) {
        Long idUsuarioLogado = (Long) session.getAttribute("usuarioId");
        if (idUsuarioLogado != null) {
            return "redirect:/api/atm"; // Redireciona para a página do ATM se já estiver logado
        }
        return "atm_login"; // Exibe a página de login se não estiver logado
    }

    @PostMapping("/login")
    public String realizarLogin(@RequestParam String email, @RequestParam String senha, HttpSession session, Model model) {
        Optional<Usuario> usuarioOptional = usuarioService.login(email, senha);

        if (usuarioOptional.isPresent()) {
            // Salva o ID do usuário na sessão
            session.setAttribute("usuarioId", usuarioOptional.get().getId());
            return "redirect:/api/atm"; // Redireciona para a página do ATM após o login
        } else {
            model.addAttribute("error", "Credenciais inválidas. Tente novamente.");
            return "atm_login"; // Retorna à página de login se falhar
        }
    }

    @RequestMapping("")
    public String mostrarAtm(HttpSession session, Model model) {
        Long idUsuarioLogado = (Long) session.getAttribute("usuarioId");
        if (idUsuarioLogado == null) {
            return "redirect:/api/atm/login"; // Redireciona para a página de login se não estiver logado
        }

        Optional<Conta> conta = usuarioService.findContaByUsuarioId(idUsuarioLogado);
        model.addAttribute("saldo", conta.map(Conta::getSaldo).orElse(BigDecimal.ZERO));
        return "atm"; // Retorna a página do ATM
    }

    @PostMapping("/sacar")
    public String sacar(@RequestParam BigDecimal valor, HttpSession session, Model model) {
        Long idUsuarioLogado = (Long) session.getAttribute("usuarioId");
        if (idUsuarioLogado == null) {
            return "redirect:/api/atm/login"; // Redireciona para a página de login se não estiver logado
        }

        boolean sucesso = usuarioService.sacar(idUsuarioLogado, valor);
        if (sucesso) {
            model.addAttribute("message", "Saque realizado com sucesso!");
        } else {
            model.addAttribute("message", "Falha no saque. Verifique se o saldo é suficiente.");
        }

        Optional<Conta> conta = usuarioService.findContaByUsuarioId(idUsuarioLogado);
        model.addAttribute("saldo", conta.map(Conta::getSaldo).orElse(BigDecimal.ZERO));
        return "atm"; // Retorna para a página do ATM após o saque
    }

    @PostMapping("/depositar")
    public String depositar(@RequestParam BigDecimal valor, HttpSession session, Model model) {
        Long idUsuarioLogado = (Long) session.getAttribute("usuarioId");
        if (idUsuarioLogado == null) {
            return "redirect:/api/atm/login"; // Redireciona para a página de login se não estiver logado
        }

        boolean sucesso = usuarioService.depositar(idUsuarioLogado, valor);
        if (sucesso) {
            model.addAttribute("message", "Depósito realizado com sucesso!");
        } else {
            model.addAttribute("message", "Falha no depósito.");
        }

        Optional<Conta> conta = usuarioService.findContaByUsuarioId(idUsuarioLogado);
        model.addAttribute("saldo", conta.map(Conta::getSaldo).orElse(BigDecimal.ZERO));
        return "atm"; // Retorna para a página do ATM após o depósito
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // Invalida a sessão para deslogar o usuário
        return "redirect:/api/atm/login"; // Redireciona para a página de login
    }
}
