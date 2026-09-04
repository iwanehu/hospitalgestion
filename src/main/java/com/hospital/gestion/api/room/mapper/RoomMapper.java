package com.hospital.gestion.api.room.mapper;

import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.room.dto.RoomRequestDTO;
import com.hospital.gestion.api.room.dto.RoomResponseDTO;
import com.hospital.gestion.api.room.dto.RoomUpdateDTO;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.ward.entity.Ward;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoomMapper {

    public Room toEntity(
            RoomRequestDTO request,
            Ward ward
    ) {

        return Room.builder()
                .number(request.number())
                .floor(request.floor())
                .roomType(request.roomType())
                .capacity(request.capacity())
                .ward(ward)
                .notes(request.notes())
                .build();
    }

    public RoomResponseDTO toResponseDTO(
            Room room
    ) {

        long availableBeds =
                room.getBeds()
                        .stream()
                        .filter(bed ->
                                bed.getStatus()
                                        == BedStatus.AVAILABLE
                        )
                        .count();

        long occupiedBeds =
                room.getBeds()
                        .stream()
                        .filter(bed ->
                                bed.getStatus()
                                        == BedStatus.OCCUPIED
                        )
                        .count();

        return new RoomResponseDTO(
                room.getId(),
                room.getNumber(),
                room.getFloor(),

                room.getRoomType(),
                room.getStatus(),

                room.getCapacity(),
                room.getTotalBeds(),

                availableBeds,
                occupiedBeds,

                room.getWard().getId(),
                room.getWard().getName(),

                room.getWard()
                        .getDepartment()
                        .getId(),

                room.getWard()
                        .getDepartment()
                        .getDepartmentType()
                        .name(),

                room.getNotes(),

                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }

    public void updateEntity(
            Room room,
            RoomUpdateDTO request
    ) {

        room.setNumber(request.number());
        room.setFloor(request.floor());
        room.setRoomType(request.roomType());
        room.setCapacity(request.capacity());
        room.setNotes(request.notes());
    }

    public List<RoomResponseDTO> toResponseDTOList(List<Room> rooms) {
        return rooms.stream()
                .map(this::toResponseDTO)
                .toList();
    }

}