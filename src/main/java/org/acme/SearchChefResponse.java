package org.acme;

import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchChefResponse {

    public List<ChefRepresentation> chefs;
    public PaginationMetadata pagination;
    public Map<String, String> _links;

    public SearchChefResponse() {
    }

    public static SearchChefResponse from(
            List<Chef> chefs, UriInfo uriInfo, String query, String sort, String direction, int page, int size,
            long totalElements, long totalPages
    ) {
        SearchChefResponse response = new SearchChefResponse();

        response.chefs = chefs.stream()
                .map(chef -> ChefRepresentation.from(chef, uriInfo))
                .toList();

        response.pagination = new PaginationMetadata(page, size, totalElements, totalPages);
        response._links = buildLinks(uriInfo, query, sort, direction, page, size, totalPages);

        return response;
    }

    private static Map<String, String> buildLinks(
            UriInfo uriInfo, String query, String sort,
            String direction, int page, int size, long totalPages
    ) {
        Map<String, String> links = new HashMap<>();
        URI baseUri = uriInfo.getBaseUri();
        String baseUrl = baseUri + "chefs/search";

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

        links.put("chefs", baseUri + "chefs");
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