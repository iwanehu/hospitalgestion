package com.hospital.gestion.api.bed.service;

import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.bed.dto.BedRequestDTO;
import com.hospital.gestion.api.bed.dto.BedResponseDTO;
import com.hospital.gestion.api.bed.dto.BedUpdateDTO;
import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.bed.mapper.BedMapper;
import com.hospital.gestion.api.bed.repository.BedRepository;
import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.room.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BedServiceTest {

    @Mock
    private BedRepository bedRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BedMapper bedMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private BedService bedService;

    private Room room;
    private Bed bed;
    private BedResponseDTO response;

    @BeforeEach
    void setUp() {
        room = Room.builder()
                .id(1L)
                .number("CARD-201")
                .capacity(4)
                .build();

        bed = Bed.builder()
                .id(1L)
                .bedNumber("BED-001")
                .room(room)
                .status(BedStatus.AVAILABLE)
                .notes("Cardiology bed")
                .build();

        response = mock(BedResponseDTO.class);
    }

    @Test
    void createBedNormalizesAndSavesBed() {
        BedRequestDTO request = new BedRequestDTO(
                " BED-001 ",
                1L,
                "  Cardiology bed  "
        );

        when(helper.findRoomByIdForUpdate(1L))
                .thenReturn(room);

        when(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                "BED-001",
                                1L
                        )
        ).thenReturn(false);

        when(bedRepository.countByRoom_Id(1L))
                .thenReturn(2L);

        when(bedMapper.toEntity(request, room))
                .thenReturn(bed);

        when(
                helper.normalizeNullableText(
                        "  Cardiology bed  "
                )
        ).thenReturn("Cardiology bed");

        when(bedRepository.save(bed))
                .thenReturn(bed);

        when(bedMapper.toResponseDTO(bed))
                .thenReturn(response);

        BedResponseDTO result =
                bedService.createBed(request);

        assertSame(response, result);
        assertEquals("BED-001", bed.getBedNumber());
        assertEquals("Cardiology bed", bed.getNotes());

        verify(helper).validateBedNumber(" BED-001 ");
        verify(helper).findRoomByIdForUpdate(1L);
        verify(bedRepository).save(bed);
    }

    @Test
    void createBedRejectsDuplicateNumberInRoom() {
        BedRequestDTO request = new BedRequestDTO(
                " BED-001 ",
                1L,
                null
        );

        when(helper.findRoomByIdForUpdate(1L))
                .thenReturn(room);

        when(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                "BED-001",
                                1L
                        )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bedService.createBed(request)
        );

        assertEquals(
                "Bed number already exists in room: BED-001",
                exception.getMessage()
        );

        verify(bedRepository, never())
                .save(any(Bed.class));
    }

    @Test
    void createBedRejectsRoomAtMaximumCapacity() {
        BedRequestDTO request = new BedRequestDTO(
                "BED-005",
                1L,
                null
        );

        when(helper.findRoomByIdForUpdate(1L))
                .thenReturn(room);

        when(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                "BED-005",
                                1L
                        )
        ).thenReturn(false);

        when(bedRepository.countByRoom_Id(1L))
                .thenReturn(4L);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bedService.createBed(request)
        );

        assertEquals(
                "Room has reached its maximum bed capacity",
                exception.getMessage()
        );

        verify(bedMapper, never())
                .toEntity(any(), any());

        verify(bedRepository, never())
                .save(any(Bed.class));
    }

    @Test
    void getBedByIdReturnsMappedBed() {
        when(helper.findBedById(1L))
                .thenReturn(bed);

        when(bedMapper.toResponseDTO(bed))
                .thenReturn(response);

        BedResponseDTO result =
                bedService.getBedById(1L);

        assertSame(response, result);

        verify(helper).findBedById(1L);
        verify(bedMapper).toResponseDTO(bed);
    }

    @Test
    void getBedByNumberAndRoomThrowsWhenMissing() {
        when(
                bedRepository
                        .findByBedNumberIgnoreCaseAndRoom_Id(
                                "BED-999",
                                1L
                        )
        ).thenReturn(java.util.Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> bedService.getBedByNumberAndRoom(
                        " BED-999 ",
                        1L
                )
        );

        assertEquals(
                "Bed not found with number: BED-999 in room: 1",
                exception.getMessage()
        );

        verify(helper).validateBedNumber(" BED-999 ");
        verify(helper).validateRoomExists(1L);
    }

    @Test
    void updateBedNormalizesAndSavesChanges() {
        BedUpdateDTO request = new BedUpdateDTO(
                " BED-002 ",
                "  Updated notes  "
        );

        when(helper.findBedById(1L))
                .thenReturn(bed);

        when(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                "BED-002",
                                1L
                        )
        ).thenReturn(false);

        when(
                helper.normalizeNullableText(
                        "  Updated notes  "
                )
        ).thenReturn("Updated notes");

        when(bedRepository.save(bed))
                .thenReturn(bed);

        when(bedMapper.toResponseDTO(bed))
                .thenReturn(response);

        BedResponseDTO result =
                bedService.updateBedById(1L, request);

        assertSame(response, result);
        assertEquals("BED-002", bed.getBedNumber());
        assertEquals("Updated notes", bed.getNotes());

        verify(bedMapper).updateEntity(bed, request);
        verify(bedRepository).save(bed);
    }

    @Test
    void updateBedRejectsDuplicateNumberInSameRoom() {
        BedUpdateDTO request = new BedUpdateDTO(
                " BED-002 ",
                null
        );

        when(helper.findBedById(1L))
                .thenReturn(bed);

        when(
                bedRepository
                        .existsByBedNumberIgnoreCaseAndRoom_Id(
                                "BED-002",
                                1L
                        )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bedService.updateBedById(
                        1L,
                        request
                )
        );

        assertEquals(
                "Bed number already exists in room: BED-002",
                exception.getMessage()
        );

        verify(bedMapper, never())
                .updateEntity(any(), any());

        verify(bedRepository, never())
                .save(any(Bed.class));
    }

    @Test
    void bedCompletesFullValidStatusLifecycle() {
        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        when(bedMapper.toResponseDTO(bed))
                .thenReturn(response);

        bedService.reserveBed(1L);
        assertEquals(BedStatus.RESERVED, bed.getStatus());

        bedService.cancelBedReservation(1L);
        assertEquals(BedStatus.AVAILABLE, bed.getStatus());

        bedService.occupyBed(1L);
        assertEquals(BedStatus.OCCUPIED, bed.getStatus());

        bedService.releaseBed(1L);
        assertEquals(BedStatus.CLEANING, bed.getStatus());

        bedService.finishBedCleaning(1L);
        assertEquals(BedStatus.AVAILABLE, bed.getStatus());

        bedService.sendBedToMaintenance(1L);
        assertEquals(BedStatus.MAINTENANCE, bed.getStatus());

        bedService.finishBedMaintenance(1L);
        assertEquals(BedStatus.AVAILABLE, bed.getStatus());

        verify(helper, times(7))
                .findBedByIdForUpdate(1L);
    }

    @Test
    void invalidStatusTransitionBecomesConflictException() {
        bed.reserve();

        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bedService.reserveBed(1L)
        );

        assertEquals(
                "Only available beds can be reserved",
                exception.getMessage()
        );

        assertEquals(
                BedStatus.RESERVED,
                bed.getStatus()
        );

        verify(bedMapper, never())
                .toResponseDTO(any(Bed.class));
    }

    @Test
    void deleteBedDeletesAvailableBedWithoutHistory() {
        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        bedService.deleteBed(1L);

        verify(bedRepository).delete(bed);
    }

    @Test
    void deleteBedRejectsOccupiedBed() {
        bed.occupy();

        when(helper.findBedByIdForUpdate(1L))
                .thenReturn(bed);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bedService.deleteBed(1L)
        );

        assertEquals(
                "Occupied or reserved bed cannot be deleted",
                exception.getMessage()
        );

        verify(bedRepository, never())
                .delete(any(Bed.class));
    }

    @Test
    void deleteBedRejectsBedWithAdmissionHistory() {
        Bed bedWithHistory = Bed.builder()
                .id(2L)
                .bedNumber("BED-002")
                .room(room)
                .status(BedStatus.AVAILABLE)
                .admissions(List.of(mock(Admission.class)))
                .build();

        when(helper.findBedByIdForUpdate(2L))
                .thenReturn(bedWithHistory);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> bedService.deleteBed(2L)
        );

        assertEquals(
                "Bed cannot be deleted because it has admission history",
                exception.getMessage()
        );

        verify(bedRepository, never())
                .delete(any(Bed.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void getBedsAppliesFiltersAndReturnsMappedPage() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("bedNumber").ascending()
        );

        Page<Bed> bedPage =
                new PageImpl<>(List.of(bed), pageable, 1);

        when(
                bedRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(bedPage);

        when(bedMapper.toResponseDTO(bed))
                .thenReturn(response);

        Page<BedResponseDTO> result =
                bedService.getBeds(
                        " BED ",
                        BedStatus.AVAILABLE,
                        1L,
                        2L,
                        3L,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().getFirst());

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );

        verify(helper)
                .validateBedStatus(BedStatus.AVAILABLE);

        verify(helper).validateRoomExists(1L);
        verify(helper).validateWardExists(2L);
        verify(helper).validateDepartmentExist(3L);

        verify(bedRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}
