package org.acme;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PratoRepresentation {

    public Long id;
    public String nome;
    public String descricao;
    public int tempoPreparoMinutos;
    public Map<String, String> _links;

    public PratoRepresentation() {
    }

    public static PratoRepresentation from(Prato prato, UriInfo uriInfo) {
        PratoRepresentation rep = new PratoRepresentation();
        rep.id = prato.id;
        rep.nome = prato.nome;
        rep.descricao = prato.descricao;
        rep.tempoPreparoMinutos = prato.tempoPreparoMinutos;

        URI baseUri = uriInfo.getBaseUri();
        rep._links = new HashMap<>();
        rep._links.put("self", baseUri + "pratos/" + prato.id);
        rep._links.put("all", baseUri + "pratos");
        rep._links.put("delete", baseUri + "pratos/" + prato.id);
        rep._links.put("update", baseUri + "pratos/" + prato.id);

        return rep;
    }
}