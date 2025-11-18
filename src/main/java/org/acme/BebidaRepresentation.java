package org.acme;

import java.math.BigDecimal;
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

    public static BebidaRepresentation from(Bebida bebida) {
        BebidaRepresentation rep = new BebidaRepresentation();
        rep.id = bebida.id;
        rep.nome = bebida.nome;
        rep.preco = bebida.preco;
        rep.volumeMl = bebida.volumeMl;
        rep.alcoolica = bebida.alcoolica;

        rep._links = new HashMap<>();
        rep._links.put("self", "/api/v1/bebidas/" + bebida.id);
        rep._links.put("all", "/api/v1/bebidas");
        rep._links.put("delete", "/api/v1/bebidas/" + bebida.id);
        rep._links.put("update", "/api/v1/bebidas/" + bebida.id);

        return rep;
    }
}
