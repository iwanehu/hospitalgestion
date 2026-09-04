package com.hospital.gestion.api.receptionist.mapper;

import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.receptionist.dto.ReceptionistRequestDTO;
import com.hospital.gestion.api.receptionist.dto.ReceptionistResponseDTO;
import com.hospital.gestion.api.receptionist.dto.ReceptionistUpdateDTO;
import com.hospital.gestion.api.receptionist.entity.Receptionist;
import com.hospital.gestion.api.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReceptionistMapper {

    public Receptionist toEntity(
            ReceptionistRequestDTO request,
            User user,
            Department department
    ) {

        return Receptionist.builder()
                .user(user)
                .department(department)
                .deskNumber(request.deskNumber())
                .shiftType(request.shiftType())
                .build();
    }

    public ReceptionistResponseDTO toResponseDTO(
            Receptionist receptionist
    ) {

        return new ReceptionistResponseDTO(
                receptionist.getId(),

                receptionist.getUser().getId(),
                receptionist.getFullName(),
                receptionist.getUser().getEmail(),

                receptionist.getDepartment().getId(),
                receptionist.getDepartment()
                        .getDepartmentType()
                        ,

                receptionist.getDeskNumber(),
                receptionist.getShiftType(),

                receptionist.getCreatedAt(),
                receptionist.getUpdatedAt()
        );
    }

    public void updateEntity(
            Receptionist receptionist,
            ReceptionistUpdateDTO request,
            Department department
    ) {

        receptionist.setDepartment(department);
        receptionist.setDeskNumber(
                request.deskNumber()
        );
        receptionist.setShiftType(
                request.shiftType()
        );
    }

    public List<ReceptionistResponseDTO> toResponseDTOList(List<Receptionist> receptionists) {
        return receptionists.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}