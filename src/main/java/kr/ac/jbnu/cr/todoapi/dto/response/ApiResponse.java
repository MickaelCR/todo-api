package kr.ac.jbnu.cr.todoapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private T data;
    private Meta meta;
    private Map<String, String> links;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        private String requestId;
        private String servedAt;

        public Meta(String requestId) {
            this.requestId = requestId;
            this.servedAt = Instant.now().toString();
        }
    }

    // Méthodes factory
    public static <T> ApiResponse<T> success(T data, String requestId) {
        return new ApiResponse<>(data, new Meta(requestId), null);
    }

    public static <T> ApiResponse<T> success(T data, String requestId, Map<String, String> links) {
        return new ApiResponse<>(data, new Meta(requestId), links);
    }
}