package org.acme;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;

    public class RestauranteRepresentation {
        public Long id;
        public String nome;
        public String endereco;
        public Map<String, URI> _links;

        public static RestauranteRepresentation from(Restaurante restaurante, UriInfo uriInfo) {
            RestauranteRepresentation rep = new RestauranteRepresentation();
            rep.id = restaurante.id;
            rep.nome = restaurante.nome;
            rep.endereco = restaurante.endereco;

            URI selfUri = uriInfo.getBaseUriBuilder()
                    .path("restaurantes")
                    .path(Long.toString(restaurante.id))
                    .build();
            rep._links = Map.of("self", selfUri);

            return rep;
        }
    }

