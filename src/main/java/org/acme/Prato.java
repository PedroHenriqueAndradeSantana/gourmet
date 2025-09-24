package org.acme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.acme.CategoriaPrato;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Set;

@Entity
public class Prato extends PanacheEntity {
    @NotBlank(message = "O nome não pode ser nulo ou vazio")
    @Schema(example = "Spaghetti Carbonara")
    public String nome;

    @NotNull(message = "O preço não pode ser nulo")
    @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero")
    @Digits(integer=6, fraction=2, message = "Formato de preço inválido")
    @Schema(example = "59.90")
    public BigDecimal preco;

    @NotNull(message = "A categoria não pode ser nula")
    @Enumerated(EnumType.STRING)
    @Schema(implementation = CategoriaPrato.class, example = "PRATO_PRINCIPAL")
    public CategoriaPrato categoria;




    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurante_id")
    @NotNull(message = "O prato deve pertencer a um restaurante")
    @JsonIgnore
    public Restaurante restaurante;


    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinTable(name = "prato_ingrediente",
            joinColumns = @JoinColumn(name = "prato_id"),
            inverseJoinColumns = @JoinColumn(name = "ingrediente_id"))
    @NotEmpty(message = "O prato deve conter ao menos um ingrediente")
    public Set<Ingrediente> ingredientes;
}