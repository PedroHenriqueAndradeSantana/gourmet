package org.acme;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
public class Chef extends PanacheEntity {
    @NotBlank(message = "O nome do chef não pode ser nulo ou vazio")
    @Schema(example = "Paola Carosella")
    public String nome;

    @NotBlank(message = "A especialidade não pode ser nula ou vazia")
    @Schema(example = "Cozinha Italiana")
    public String especialidade;

    // Relacionamento One-to-One (lado inverso)
    @OneToOne(mappedBy = "chef")
    @JsonIgnore // Evita recursividade infinita na serialização JSON
    public Restaurante restaurante;
}