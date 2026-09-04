package com.hospital.gestion.api.ward.mapper;

import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.ward.dto.WardRequestDTO;
import com.hospital.gestion.api.ward.dto.WardResponseDTO;
import com.hospital.gestion.api.ward.dto.WardUpdateDTO;
import com.hospital.gestion.api.ward.entity.Ward;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WardMapper {

    public Ward toEntity(
            WardRequestDTO request,
            Department department
    ) {

        return Ward.builder()
                .name(request.name())
                .description(request.description())
                .department(department)
                .build();
    }

    public WardResponseDTO toResponseDTO(
            Ward ward
    ) {

        return new WardResponseDTO(
                ward.getId(),
                ward.getName(),
                ward.getDescription(),
                ward.getIsActive(),

                ward.getDepartment().getId(),
                ward.getDepartment()
                        .getDepartmentType()
                        .name(),

                ward.getRooms() != null
                        ? ward.getRooms().size()
                        : 0,

                ward.getCreatedAt(),
                ward.getUpdatedAt()
        );
    }

    public void updateEntity(
            Ward ward,
            WardUpdateDTO request
    ) {

        ward.setName(request.name());
        ward.setDescription(
                request.description()
        );

        if (request.isActive() != null) {
            ward.setIsActive(
                    request.isActive()
            );
        }
    }


    public List<WardResponseDTO> toResponseDTOList(
            List<Ward> wards
    ) {
        return wards.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}