package org.acme;

import jakarta.ws.rs.core.UriInfo;
import org.acme.Pedido;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PedidoRepresentation {

    public long id;
    public String nomeCliente;
    public int numeroMesa;
    public Pedido.StatusPedido status;
    public Map<String, String> _links;
    public List<Map<String, Object>> pratos;

    public PedidoRepresentation() {
    }

    public static PedidoRepresentation from(Pedido pedido, UriInfo uriInfo) {
        PedidoRepresentation rep = new PedidoRepresentation();
        rep.id = pedido.id;
        rep.nomeCliente = pedido.nomeCliente;
        rep.numeroMesa = pedido.numeroMesa;
        rep.status = pedido.status;

        URI baseUri = uriInfo.getBaseUri();
        rep._links = new HashMap<>();
        rep._links.put("self", baseUri + "pedidos/" + pedido.id);
        rep._links.put("all", baseUri + "pedidos");
        rep._links.put("delete", baseUri + "pedidos/" + pedido.id);
        rep._links.put("update", baseUri + "pedidos/" + pedido.id);



        return rep;
    }
}