package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;


@Entity
public class Prato extends PanacheEntity {

    @NotNull
    @Size(min = 2, max = 100, message = "O nome do prato deve ter entre 2 a 100 letras")
    public String nome;

    @NotNull
    @Size(min = 2, max = 200, message = "A descrição do prato deve ter entre 2 a 200 letras")
    public String descricao;

    @Min(value = 5, message = "O tempo mínimo de preparo é 5 minutos")
    @Max(value = 90, message = "O tempo máximo de preparo é 90 minutos")
    public int tempoPreparoMinutos;

    // relacionamento OneToOne 
    @OneToOne
    @JoinColumn(name = "chef_id", unique = true)
    public Chef chefResponsavel;

    // relacionamento ManyToMany 
    @ManyToMany(mappedBy = "pratos")
    public Set<Pedido> pedidos = new HashSet<>();

    public Prato() {
    }

    
    public Prato(String nome, String descricao, int tempoPreparoMinutos, Chef chefResponsavel) {
        this.nome = nome;
        this.descricao = descricao;
        this.tempoPreparoMinutos = tempoPreparoMinutos;
        this.chefResponsavel = chefResponsavel;
    }
}