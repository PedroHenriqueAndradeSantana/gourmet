package org.acme;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
public class Ingrediente extends PanacheEntity {
    @NotBlank(message="O nome do ingrediente é obrigatório")
    @Column(unique = true)
    @Schema(example = "Tomate Cereja")
    public String nome;
}