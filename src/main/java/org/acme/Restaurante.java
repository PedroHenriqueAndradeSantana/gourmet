package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
public class Restaurante extends PanacheEntity {
    @NotBlank(message = "O nome não pode ser nulo ou vazio")
    @Size(min = 2, max = 50, message = "O nome deve ter entre 2 e 50 caracteres")
    @Schema(example = "Cantina da Nona")
    public String nome;

    @NotBlank(message = "O endereço não pode ser nulo ou vazio")
    @Schema(example = "Rua das Flores, 123")
    public String endereco;




    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "chef_id", referencedColumnName = "id")
    @NotNull(message = "O restaurante deve ter um chef")
    public Chef chef;


    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    public List<Prato> cardapio;
}