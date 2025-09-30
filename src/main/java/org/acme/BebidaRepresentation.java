package org.acme;

import jakarta.ws.rs.core.UriInfo;
import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class BebidaRepresentation {

    public Long id;
    public String nome;
    public BigDecimal preco;
    public int volumeMl;
    public boolean alcoolica;
    public Map<String, String> _links;

    public BebidaRepresentation() {
    }

    public static BebidaRepresentation from(Bebida bebida, UriInfo uriInfo) {
        BebidaRepresentation rep = new BebidaRepresentation();
        rep.id = bebida.id;
        rep.nome = bebida.nome;
        rep.preco = bebida.preco;
        rep.volumeMl = bebida.volumeMl;
        rep.alcoolica = bebida.alcoolica;

        URI baseUri = uriInfo.getBaseUri();
        rep._links = new HashMap<>();
        rep._links.put("self", baseUri + "bebidas/" + bebida.id);
        rep._links.put("all", baseUri + "bebidas");
        rep._links.put("delete", baseUri + "bebidas/" + bebida.id);
        rep._links.put("update", baseUri + "bebidas/" + bebida.id);


        return rep;
    }
}