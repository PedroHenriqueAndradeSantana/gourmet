package org.acme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchBebidaResponse {

    public List<BebidaRepresentation> bebidas;
    public PaginationMetadata pagination;
    public Map<String, String> _links;

    public SearchBebidaResponse() {
    }

    public static SearchBebidaResponse from(
            List<Bebida> bebidas, String query, String sort, String direction, int page, int size,
            long totalElements, long totalPages
    ) {
        SearchBebidaResponse response = new SearchBebidaResponse();
        response.bebidas = bebidas.stream()
                .map(BebidaRepresentation::from)
                .toList();

        response.pagination = new PaginationMetadata(page, size, totalElements, totalPages);
        response._links = buildLinks(query, sort, direction, page, size, totalPages);
        return response;
    }

    private static Map<String, String> buildLinks(
            String query, String sort, String direction, int page, int size, long totalPages
    ) {
        Map<String, String> links = new HashMap<>();
        String baseUrl = "/api/v1/bebidas/search";

        StringBuilder params = new StringBuilder();
        if (query != null && !query.isBlank()) {
            params.append("q=").append(query).append("&");
        }
        params.append("sort=").append(sort)
                .append("&direction=").append(direction)
                .append("&size=").append(size);

        links.put("self", baseUrl + "?" + params + "&page=" + page);
        links.put("first", baseUrl + "?" + params + "&page=1");
        links.put("last", baseUrl + "?" + params + "&page=" + totalPages);

        if (page > 1) {
            links.put("prev", baseUrl + "?" + params + "&page=" + (page - 1));
        }
        if (page < totalPages) {
            links.put("next", baseUrl + "?" + params + "&page=" + (page + 1));
        }

        links.put("bebidas", "/api/v1/bebidas");
        return links;
    }

    public static class PaginationMetadata {
        public int page;
        public int size;
        public long totalElements;
        public long totalPages;

        public PaginationMetadata(int page, int size, long totalElements, long totalPages) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }
    }
}
