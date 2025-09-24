package org.acme;

import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

public class SearchRestauranteResponse {

    public List<RestauranteRepresentation> data;
    public long totalElements;
    public long totalPages;
    public int currentPage;
    public int pageSize;
    public String sort;
    public String direction;
    public String query;

    public static SearchRestauranteResponse from(List<Restaurante> restaurantes, UriInfo uriInfo, String q, String sort, String direction,
                                                 int page,
                                                 int size,
                                                 long totalElements,
                                                 long totalPages)
    {

        SearchRestauranteResponse response = new SearchRestauranteResponse();
        response.data = restaurantes.stream()
                .map(r -> RestauranteRepresentation.from(r, uriInfo))
                .collect(Collectors.toList());
        response.totalElements = totalElements;
        response.totalPages = totalPages;
        response.currentPage = page;
        response.pageSize = size;
        response.sort = sort;
        response.direction = direction;
        response.query = q;
        return response;
    }
}