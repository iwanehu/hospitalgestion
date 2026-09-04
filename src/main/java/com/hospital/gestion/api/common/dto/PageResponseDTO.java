package com.hospital.gestion.api.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDTO<T>(

        List<T> content,

        int page,
        int size,
        int numberOfElements,

        long totalElements,
        int totalPages,

        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious,
        boolean empty,

        List<SortResponseDTO> sort

) {

    public static <T> PageResponseDTO<T> from(
            Page<T> source
    ) {
        List<SortResponseDTO> sort = source
                .getSort()
                .stream()
                .map(order ->
                        new SortResponseDTO(
                                order.getProperty(),
                                order.getDirection().name()
                        )
                )
                .toList();

        return new PageResponseDTO<>(
                source.getContent(),
                source.getNumber(),
                source.getSize(),
                source.getNumberOfElements(),
                source.getTotalElements(),
                source.getTotalPages(),
                source.isFirst(),
                source.isLast(),
                source.hasNext(),
                source.hasPrevious(),
                source.isEmpty(),
                sort
        );
    }
}
