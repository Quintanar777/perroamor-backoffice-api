package com.perroamor.inventory.shared.types;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T, R> PagedResponse<R> map(Page<T> source, java.util.function.Function<T, R> mapper) {
        return new PagedResponse<>(
                source.content().stream().map(mapper).toList(),
                source.page(),
                source.size(),
                source.totalElements(),
                source.totalPages());
    }
}
