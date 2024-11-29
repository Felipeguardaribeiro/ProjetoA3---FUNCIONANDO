package projeto.faculdade.banco.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo")
    private String tipo; // Tipo da transação (ex: Transferência)

    @Column(name = "valor")
    private BigDecimal valor; // Valor da transação

    @Column(name = "data")
    @Temporal(TemporalType.TIMESTAMP)
    private Date data; // Data da transação

    @Column(name = "id_usuario_origem")
    private Long idUsuarioOrigem; // ID do usuário de origem

    @Column(name = "id_usuario_destino")
    private Long idUsuarioDestino; // ID do usuário de destino

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Long getIdUsuarioOrigem() {
        return idUsuarioOrigem;
    }

    public void setIdUsuarioOrigem(Long idUsuarioOrigem) {
        this.idUsuarioOrigem = idUsuarioOrigem;
    }

    public Long getIdUsuarioDestino() {
        return idUsuarioDestino;
    }

    public void setIdUsuarioDestino(Long idUsuarioDestino) {
        this.idUsuarioDestino = idUsuarioDestino;
    }
}
