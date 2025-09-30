package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Pedido extends PanacheEntity {


    public enum StatusPedido {
        RECEBIDO,
        EM_PREPARO,
        PRONTO_PARA_ENTREGA,
        FINALIZADO,
        CANCELADO
    }

    @NotNull
    @Size(min = 2, max = 100, message = "O nome do cliente deve ter entre 2 a 100 letras")
    public String nomeCliente;

    @Min(value = 1, message = "O número da mesa deve ser no mínimo 1")
    public int numeroMesa;


    @NotNull
    @Enumerated(EnumType.STRING)
    public StatusPedido status;


    @ManyToMany
    @JoinTable(name = "pedido_prato",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "prato_id"))
    public Set<Prato> pratos = new HashSet<>();


    public Pedido() {
    }

    public Pedido(String nomeCliente, int numeroMesa) {
        this.nomeCliente = nomeCliente;
        this.numeroMesa = numeroMesa;
        this.status = StatusPedido.RECEBIDO; // Status inicial padrão
    }
}