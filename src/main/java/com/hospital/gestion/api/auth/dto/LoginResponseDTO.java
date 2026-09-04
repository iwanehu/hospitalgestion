package com.hospital.gestion.api.auth.dto;

public record LoginResponseDTO(

        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String email,
        String role

) {
}