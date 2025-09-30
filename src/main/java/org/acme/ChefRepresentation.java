package org.acme;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ChefRepresentation {
    public Long id;
    public String nome;
    public String especialidade;
    public int anosDeExperiencia;
    public Map<String, String> _links;

    public ChefRepresentation() {
    }

    public static ChefRepresentation from(Chef chef, UriInfo uriInfo) {
        ChefRepresentation rep = new ChefRepresentation();
        rep.id = chef.id; 
        rep.nome = chef.nome;
        rep.especialidade = chef.especialidade;
        rep.anosDeExperiencia = chef.anosDeExperiencia;

        URI baseUri = uriInfo.getBaseUri();
        rep._links = new HashMap<>();
        rep._links.put("self", baseUri + "chefs/" + chef.id);
        rep._links.put("all", baseUri + "chefs");
        rep._links.put("delete", baseUri + "chefs/" + chef.id);
        rep._links.put("update", baseUri + "chefs/" + chef.id);
        
        

        return rep;
    }
}