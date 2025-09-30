package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Chef extends PanacheEntity {

    @NotNull
    @Size(min = 2, max = 100, message = "O nome do chef deve ter entre 2 a 100 letras")
    public String nome;

    @NotNull
    @Size(min = 3, max = 50, message = "A especialidade deve ter entre 3 a 50 letras")
    public String especialidade; 

    @Min(value = 0, message = "Os anos de experiência não podem ser negativos")
    public int anosDeExperiencia;

    
    @OneToOne(mappedBy = "chefResponsavel")
    public Prato pratoAssinado;

    public Chef() {
    }

    public Chef(String nome, String especialidade, int anosDeExperiencia) {
        this.nome = nome;
        this.especialidade = especialidade;
        this.anosDeExperiencia = anosDeExperiencia;
    }
}