package com.hospital.gestion.api.bed.mapper;

import com.hospital.gestion.api.bed.dto.BedRequestDTO;
import com.hospital.gestion.api.bed.dto.BedResponseDTO;
import com.hospital.gestion.api.bed.dto.BedUpdateDTO;
import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.room.entity.Room;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BedMapper {

    public Bed toEntity(
            BedRequestDTO request,
            Room room
    ) {

        return Bed.builder()
                .bedNumber(request.bedNumber())
                .room(room)
                .notes(request.notes())
                .build();
    }

    public BedResponseDTO toResponseDTO(
            Bed bed
    ) {

        Room room = bed.getRoom();

        return new BedResponseDTO(
                bed.getId(),
                bed.getBedNumber(),

                bed.getStatus().name(),
                bed.isOccupied(),

                room.getId(),
                room.getNumber(),
                room.getFloor(),
                room.getRoomType().name(),

                room.getWard().getId(),
                room.getWard().getName(),

                room.getWard()
                        .getDepartment()
                        .getId(),

                room.getWard()
                        .getDepartment()
                        .getDepartmentType()
                        .name(),

                bed.getNotes(),

                bed.getCreatedAt(),
                bed.getUpdatedAt()
        );
    }

    public void updateEntity(
            Bed bed,
            BedUpdateDTO request
    ) {

        bed.setBedNumber(
                request.bedNumber()
        );

        bed.setNotes(
                request.notes()
        );
    }

    public List<BedResponseDTO> toResponseDTOList(
            List<Bed> beds
    ) {
        return beds.stream()
                .map(this::toResponseDTO)
                .toList();
    }

}