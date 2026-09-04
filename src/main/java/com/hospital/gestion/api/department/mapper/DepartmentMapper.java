package com.hospital.gestion.api.department.mapper;

import com.hospital.gestion.api.department.dto.DepartmentRequestDTO;
import com.hospital.gestion.api.department.dto.DepartmentResponseDTO;
import com.hospital.gestion.api.department.dto.DepartmentUpdateDTO;
import com.hospital.gestion.api.department.entity.Department;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DepartmentMapper {

    public Department toEntity(
            DepartmentRequestDTO request
    ) {

        return Department.builder()
                .departmentType(
                        request.departmentType()
                )
                .location(request.location())
                .phoneExtension(
                        request.phoneExtension()
                )
                .description(request.description())
                .build();
    }

    public DepartmentResponseDTO toResponseDTO(
            Department department
    ) {

        return new DepartmentResponseDTO(
                department.getId(),
                department.getDepartmentType(),
                department.getLocation(),
                department.getPhoneExtension(),
                department.getDescription(),
                department.getIsActive(),

                department.getWards() != null
                        ? department.getWards().size()
                        : 0,

                department.getCreatedAt(),
                department.getUpdatedAt()
        );
    }

    public void updateEntity(
            Department department,
            DepartmentUpdateDTO request
    ) {

        department.setLocation(
                request.location()
        );

        department.setPhoneExtension(
                request.phoneExtension()
        );

        department.setDescription(
                request.description()
        );

        if (request.isActive() != null) {
            department.setIsActive(
                    request.isActive()
            );
        }
    }

    public List<DepartmentResponseDTO> toResponseDTOList(List<Department> departments) {
        if (departments == null) {
            return List.of(); // Retorna lista vacía si es null
        }

        return departments.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de DepartmentRequestDTO a una lista de Departamentos
     */
    public List<Department> toEntityList(List<DepartmentRequestDTO> requests) {
        if (requests == null) {
            return List.of();
        }

        return requests.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}