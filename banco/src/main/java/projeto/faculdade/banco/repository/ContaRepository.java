package projeto.faculdade.banco.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import projeto.faculdade.banco.model.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    Optional<Conta> findByUsuarioId(Long usuarioId);
}
