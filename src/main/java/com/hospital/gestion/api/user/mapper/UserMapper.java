package com.hospital.gestion.api.user.mapper;

import com.hospital.gestion.api.user.dto.UserRequestDTO;
import com.hospital.gestion.api.user.dto.UserResponseDTO;
import com.hospital.gestion.api.user.dto.UserUpdateDTO;
import com.hospital.gestion.api.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public User toEntity(UserRequestDTO request) {
        return User.builder()
                .role(request.role())
                .email(request.email())
                .documentId(request.documentId())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .build();
    }

    public UserResponseDTO toResponseDTO(User user) {

        return new UserResponseDTO(
                user.getId(),
                user.getRole().name(),
                user.getEmail(),
                user.getIsActive(),
                user.getDocumentId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public void updateEntity(
            User user,
            UserUpdateDTO request
    ) {

        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
    }

    public List<UserResponseDTO> toResponseDTOList(
            List<User> users
    ) {
        return users.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}