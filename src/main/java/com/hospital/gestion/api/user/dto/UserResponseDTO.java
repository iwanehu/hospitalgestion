package com.hospital.gestion.api.user.dto;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String role,
        String email,
        Boolean isActive,

        String documentId,
        String firstName,
        String lastName,
        String phone,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}