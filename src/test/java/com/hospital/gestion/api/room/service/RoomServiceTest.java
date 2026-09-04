package com.hospital.gestion.api.room.service;

import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.enums.RoomStatus;
import com.hospital.gestion.api.common.enums.RoomType;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.room.dto.*;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.room.mapper.RoomMapper;
import com.hospital.gestion.api.room.repository.RoomRepository;
import com.hospital.gestion.api.ward.entity.Ward;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private RoomService roomService;

    @Test
    void createRoomNormalizesAndSavesRoom() {
        Ward ward = ward();
        RoomRequestDTO request =
                new RoomRequestDTO(
                        " CARD-201 ",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        4,
                        1L,
                        " Intensive care "
                );

        Room room = room();
        RoomResponseDTO expected = response();

        when(
                roomRepository.existsByNumberIgnoreCase(
                        "CARD-201"
                )
        ).thenReturn(false);

        when(helper.findWardById(1L))
                .thenReturn(ward);

        when(roomMapper.toEntity(request, ward))
                .thenReturn(room);

        when(
                helper.normalizeNullableText(
                        " Intensive care "
                )
        ).thenReturn("Intensive care");

        when(roomRepository.save(room))
                .thenReturn(room);

        when(roomMapper.toResponseDTO(room))
                .thenReturn(expected);

        RoomResponseDTO result =
                roomService.createRoom(request);

        assertSame(expected, result);
        assertEquals("CARD-201", room.getNumber());
        assertEquals("Intensive care", room.getNotes());

        verify(helper).validateRoomNumber(
                " CARD-201 "
        );

        verify(roomRepository).save(room);
    }

    @Test
    void createRoomRejectsDuplicateNumber() {
        RoomRequestDTO request =
                new RoomRequestDTO(
                        " CARD-201 ",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        4,
                        1L,
                        null
                );

        when(
                roomRepository.existsByNumberIgnoreCase(
                        "CARD-201"
                )
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> roomService.createRoom(request)
        );

        verify(helper, never()).findWardById(anyLong());
        verify(roomRepository, never()).save(any(Room.class));
        verifyNoInteractions(roomMapper);
    }

    @Test
    void getRoomByIdReturnsMappedRoom() {
        Room room = room();
        RoomResponseDTO expected = response();

        when(helper.findRoomById(1L))
                .thenReturn(room);

        when(roomMapper.toResponseDTO(room))
                .thenReturn(expected);

        RoomResponseDTO result =
                roomService.getRoomById(1L);

        assertSame(expected, result);
    }

    @Test
    void getRoomByNumberThrowsWhenMissing() {
        when(
                roomRepository.findByNumberIgnoreCase(
                        "CARD-999"
                )
        ).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> roomService.getRoomByNumber(
                        " CARD-999 "
                )
        );

        verifyNoInteractions(roomMapper);
    }

    @Test
    void updateRoomValidatesAndSavesRoom() {
        Room room = room();

        RoomUpdateDTO request =
                new RoomUpdateDTO(
                        " CARD-202 ",
                        3,
                        RoomType.CARDIOLOGY_ICU,
                        5,
                        " Updated notes "
                );

        RoomResponseDTO expected = response();

        when(helper.findRoomById(1L))
                .thenReturn(room);

        when(
                helper.normalizeNullableText(
                        " Updated notes "
                )
        ).thenReturn("Updated notes");

        when(roomRepository.saveAndFlush(room))
                .thenReturn(room);

        when(roomMapper.toResponseDTO(room))
                .thenReturn(expected);

        RoomResponseDTO result =
                roomService.updateRoomById(
                        1L,
                        request
                );

        assertSame(expected, result);
        assertEquals("CARD-202", room.getNumber());
        assertEquals("Updated notes", room.getNotes());

        verify(helper).validateRoomNumberForUpdate(
                room,
                "CARD-202"
        );

        verify(helper).validateCapacityForUpdate(
                room,
                5
        );

        verify(roomMapper).updateEntity(room, request);
        verify(roomRepository).saveAndFlush(room);
    }

    @Test
    void updateRoomStopsWhenNumberValidationFails() {
        Room room = room();

        RoomUpdateDTO request =
                new RoomUpdateDTO(
                        "CARD-202",
                        3,
                        RoomType.CARDIOLOGY_ICU,
                        5,
                        null
                );

        when(helper.findRoomById(1L))
                .thenReturn(room);

        doThrow(
                new ConflictException(
                        "Room already exists: CARD-202"
                )
        ).when(helper).validateRoomNumberForUpdate(
                room,
                "CARD-202"
        );

        assertThrows(
                ConflictException.class,
                () -> roomService.updateRoomById(
                        1L,
                        request
                )
        );

        verify(
                helper,
                never()
        ).validateCapacityForUpdate(any(), any());

        verify(
                roomMapper,
                never()
        ).updateEntity(any(), any());

        verify(
                roomRepository,
                never()
        ).saveAndFlush(any(Room.class));
    }

    @Test
    void updateRoomStatusChangesAndSavesStatus() {
        Room room = room();

        RoomStatusUpdateDTO request =
                new RoomStatusUpdateDTO(
                        RoomStatus.MAINTENANCE,
                        " Technical inspection "
                );

        RoomResponseDTO expected = response();

        when(helper.findRoomById(1L))
                .thenReturn(room);

        when(
                helper.normalizeNullableText(
                        " Technical inspection "
                )
        ).thenReturn("Technical inspection");

        when(roomRepository.saveAndFlush(room))
                .thenReturn(room);

        when(roomMapper.toResponseDTO(room))
                .thenReturn(expected);

        RoomResponseDTO result =
                roomService.updateRoomStatus(
                        1L,
                        request
                );

        assertSame(expected, result);
        assertEquals(
                RoomStatus.MAINTENANCE,
                room.getStatus()
        );
        assertEquals(
                "Technical inspection",
                room.getNotes()
        );

        verify(roomRepository).saveAndFlush(room);
    }

    @Test
    void updateRoomStatusRejectsSameStatus() {
        Room room = room();

        RoomStatusUpdateDTO request =
                new RoomStatusUpdateDTO(
                        RoomStatus.AVAILABLE,
                        null
                );

        when(helper.findRoomById(1L))
                .thenReturn(room);

        assertThrows(
                ConflictException.class,
                () -> roomService.updateRoomStatus(
                        1L,
                        request
                )
        );

        verify(
                roomRepository,
                never()
        ).saveAndFlush(any(Room.class));
    }

    @Test
    void deleteRoomDeletesRoomWithoutBeds() {
        Room room = room();

        when(helper.findRoomById(1L))
                .thenReturn(room);

        roomService.deleteRoom(1L);

        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoomRejectsRoomContainingBeds() {
        Room room = room();
        room.getBeds().add(mock(Bed.class));

        when(helper.findRoomById(1L))
                .thenReturn(room);

        assertThrows(
                ConflictException.class,
                () -> roomService.deleteRoom(1L)
        );

        verify(
                roomRepository,
                never()
        ).delete(any(Room.class));
    }

    @Test
    void existsByNumberNormalizesNumber() {
        when(
                roomRepository.existsByNumberIgnoreCase(
                        "CARD-201"
                )
        ).thenReturn(true);

        boolean result =
                roomService.existsByNumber(
                        " CARD-201 "
                );

        assertTrue(result);

        verify(helper).validateRoomNumber(
                " CARD-201 "
        );

        verify(roomRepository)
                .existsByNumberIgnoreCase(
                        "CARD-201"
                );
    }

    @Test
    void getRoomStatsBuildsCompleteStatistics() {
        when(
                roomRepository.countByStatus(
                        RoomStatus.AVAILABLE
                )
        ).thenReturn(4L);

        when(
                roomRepository.countByStatus(
                        RoomStatus.OCCUPIED
                )
        ).thenReturn(3L);

        when(
                roomRepository.countByStatus(
                        RoomStatus.MAINTENANCE
                )
        ).thenReturn(2L);

        when(
                roomRepository.countByStatus(
                        RoomStatus.CLEANING
                )
        ).thenReturn(1L);

        when(
                roomRepository.countByStatus(
                        RoomStatus.RESERVED
                )
        ).thenReturn(5L);

        when(
                roomRepository.countBedsByStatus(
                        BedStatus.AVAILABLE
                )
        ).thenReturn(10L);

        RoomStatsResponse result =
                roomService.getRoomStats();

        assertEquals(4L, result.available());
        assertEquals(3L, result.occupied());
        assertEquals(2L, result.maintenance());
        assertEquals(1L, result.cleaning());
        assertEquals(5L, result.reserved());
        assertEquals(10L, result.availableBeds());
    }

    @Test
    void getRoomsReturnsFilteredPage() {
        Room room = room();
        RoomResponseDTO expected = response();

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by("number").ascending()
                );

        Page<Room> rooms =
                new PageImpl<>(
                        List.of(room),
                        pageable,
                        1
                );

        when(
                roomRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(rooms);

        when(roomMapper.toResponseDTO(room))
                .thenReturn(expected);

        Page<RoomResponseDTO> result =
                roomService.getRooms(
                        " CARD ",
                        2,
                        RoomType.CARDIOLOGY_ICU,
                        RoomStatus.AVAILABLE,
                        1L,
                        1L,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertSame(
                expected,
                result.getContent().getFirst()
        );

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );

        verify(helper).validateFloor(2);
        verify(helper).validateRoomType(
                RoomType.CARDIOLOGY_ICU
        );
        verify(helper).validateRoomStatus(
                RoomStatus.AVAILABLE
        );
        verify(helper).validateWardExists(1L);
        verify(helper).validateDepartmentExist(1L);

        verify(roomRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    private Department department() {
        return Department.builder()
                .id(1L)
                .departmentType(
                        DepartmentType.CARDIOLOGY
                )
                .location("Floor 2")
                .isActive(true)
                .build();
    }

    private Ward ward() {
        return Ward.builder()
                .id(1L)
                .name("Cardiology Ward")
                .department(department())
                .isActive(true)
                .build();
    }

    private Room room() {
        return Room.builder()
                .id(1L)
                .number("CARD-201")
                .floor(2)
                .roomType(
                        RoomType.CARDIOLOGY_ICU
                )
                .status(RoomStatus.AVAILABLE)
                .capacity(4)
                .ward(ward())
                .notes("Cardiology room")
                .build();
    }

    private RoomResponseDTO response() {
        return new RoomResponseDTO(
                1L,
                "CARD-201",
                2,
                RoomType.CARDIOLOGY_ICU,
                RoomStatus.AVAILABLE,
                4,
                0,
                0L,
                0L,
                1L,
                "Cardiology Ward",
                1L,
                DepartmentType.CARDIOLOGY.name(),
                "Cardiology room",
                null,
                null
        );
    }
}
