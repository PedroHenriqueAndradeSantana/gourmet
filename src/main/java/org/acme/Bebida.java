package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
public class Bebida extends PanacheEntity {

    @NotNull
    @Size(min = 2, max = 80, message = "O nome da bebida deve ter entre 2 a 80 letras")
    public String nome;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero")
    public BigDecimal preco;


    @Min(value = 50, message = "O volume mínimo é de 50ml")
    public int volumeMl;

    @NotNull
    public boolean alcoolica;



    public Bebida() {
    }

    public Bebida(String nome, BigDecimal preco, int volumeMl, boolean alcoolica) {
        this.nome = nome;
        this.preco = preco;
        this.volumeMl = volumeMl;
        this.alcoolica = alcoolica;

    }
}