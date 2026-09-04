package com.hospital.gestion.api.receptionist.service;

import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.receptionist.dto.ReceptionistRequestDTO;
import com.hospital.gestion.api.receptionist.dto.ReceptionistResponseDTO;
import com.hospital.gestion.api.receptionist.dto.ReceptionistUpdateDTO;
import com.hospital.gestion.api.receptionist.entity.Receptionist;
import com.hospital.gestion.api.receptionist.mapper.ReceptionistMapper;
import com.hospital.gestion.api.receptionist.repository.ReceptionistRepository;
import com.hospital.gestion.api.user.entity.User;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceptionistServiceTest {

    @Mock
    private ReceptionistRepository receptionistRepository;

    @Mock
    private ReceptionistMapper receptionistMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private ReceptionistService receptionistService;

    private User user;
    private Department department;
    private Receptionist receptionist;
    private ReceptionistResponseDTO response;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        department = mock(Department.class);
        response = mock(ReceptionistResponseDTO.class);

        lenient().when(user.getId())
                .thenReturn(4L);

        lenient().when(user.getRole())
                .thenReturn(Role.RECEPTIONIST);

        lenient().when(user.getIsActive())
                .thenReturn(true);

        receptionist = Receptionist.builder()
                .id(1L)
                .user(user)
                .department(department)
                .deskNumber("DESK-02")
                .shiftType(ShiftType.NIGHT)
                .build();
    }

    private ReceptionistRequestDTO request() {
        return new ReceptionistRequestDTO(
                4L,
                1L,
                " DESK-02 ",
                ShiftType.NIGHT
        );
    }

    @Test
    void createReceptionistNormalizesAndSavesProfile() {
        ReceptionistRequestDTO request = request();

        when(helper.findUserByIdForUpdate(4L))
                .thenReturn(user);

        when(receptionistRepository.existsByUser_Id(4L))
                .thenReturn(false);

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        when(
                helper.normalizeRequiredText(
                        " DESK-02 ",
                        "Desk number"
                )
        ).thenReturn("DESK-02");

        when(
                receptionistMapper.toEntity(
                        request,
                        user,
                        department
                )
        ).thenReturn(receptionist);

        when(receptionistRepository.save(receptionist))
                .thenReturn(receptionist);

        when(
                receptionistMapper.toResponseDTO(
                        receptionist
                )
        ).thenReturn(response);

        ReceptionistResponseDTO result =
                receptionistService
                        .createReceptionist(request);

        assertSame(response, result);

        assertEquals(
                "DESK-02",
                receptionist.getDeskNumber()
        );

        verify(helper).findUserByIdForUpdate(4L);
        verify(helper).findDepartmentById(1L);
        verify(receptionistRepository).save(receptionist);
    }

    @Test
    void createReceptionistRejectsIncorrectUserRole() {
        ReceptionistRequestDTO request = request();

        when(user.getRole()).thenReturn(Role.NURSE);

        when(helper.findUserByIdForUpdate(4L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> receptionistService
                        .createReceptionist(request)
        );

        assertEquals(
                "User must have RECEPTIONIST role",
                exception.getMessage()
        );

        verify(receptionistRepository, never())
                .existsByUser_Id(anyLong());

        verify(receptionistRepository, never())
                .save(any(Receptionist.class));
    }

    @Test
    void createReceptionistRejectsInactiveUser() {
        ReceptionistRequestDTO request = request();

        when(user.getIsActive()).thenReturn(false);

        when(helper.findUserByIdForUpdate(4L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> receptionistService
                        .createReceptionist(request)
        );

        assertEquals(
                "Inactive user cannot be registered as a receptionist",
                exception.getMessage()
        );

        verify(helper, never())
                .findDepartmentById(anyLong());

        verify(receptionistRepository, never())
                .save(any(Receptionist.class));
    }

    @Test
    void createReceptionistRejectsExistingProfile() {
        ReceptionistRequestDTO request = request();

        when(helper.findUserByIdForUpdate(4L))
                .thenReturn(user);

        when(receptionistRepository.existsByUser_Id(4L))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> receptionistService
                        .createReceptionist(request)
        );

        assertEquals(
                "A receptionist profile already exists for user: 4",
                exception.getMessage()
        );

        verify(helper, never())
                .findDepartmentById(anyLong());

        verify(receptionistRepository, never())
                .save(any(Receptionist.class));
    }

    @Test
    void getReceptionistByUserThrowsWhenMissing() {
        when(receptionistRepository.findByUser_Id(4L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> receptionistService
                        .getReceptionistByUserId(4L)
        );

        assertEquals(
                "Receptionist not found for user: 4",
                exception.getMessage()
        );

        verify(helper).validateId(4L, "User");
    }

    @Test
    void getReceptionistByEmailUsesNormalizedEmail() {
        when(
                helper.normalizeRequiredText(
                        " receptionist@hospital.com ",
                        "Email"
                )
        ).thenReturn("receptionist@hospital.com");

        when(
                receptionistRepository
                        .findByUser_EmailIgnoreCase(
                                "receptionist@hospital.com"
                        )
        ).thenReturn(Optional.of(receptionist));

        when(
                receptionistMapper.toResponseDTO(
                        receptionist
                )
        ).thenReturn(response);

        ReceptionistResponseDTO result =
                receptionistService.getReceptionistByEmail(
                        " receptionist@hospital.com "
                );

        assertSame(response, result);

        verify(receptionistRepository)
                .findByUser_EmailIgnoreCase(
                        "receptionist@hospital.com"
                );
    }

    @Test
    void getReceptionistsByDeskUsesNormalizedDesk() {
        when(
                helper.normalizeRequiredText(
                        " desk-02 ",
                        "Desk number"
                )
        ).thenReturn("DESK-02");

        when(
                receptionistRepository
                        .findByDeskNumberIgnoreCase(
                                "DESK-02"
                        )
        ).thenReturn(List.of(receptionist));

        when(
                receptionistMapper.toResponseDTOList(
                        List.of(receptionist)
                )
        ).thenReturn(List.of(response));

        List<ReceptionistResponseDTO> result =
                receptionistService
                        .getReceptionistsByDeskNumber(
                                " desk-02 "
                        );

        assertEquals(1, result.size());
        assertSame(response, result.getFirst());

        verify(receptionistRepository)
                .findByDeskNumberIgnoreCase(
                        "DESK-02"
                );
    }

    @Test
    void updateReceptionistUsesLockAndNormalizesDesk() {
        Department newDepartment =
                mock(Department.class);

        ReceptionistUpdateDTO request =
                new ReceptionistUpdateDTO(
                        2L,
                        " DESK-05 ",
                        ShiftType.MORNING
                );

        when(
                helper.findReceptionistByIdForUpdate(1L)
        ).thenReturn(receptionist);

        when(helper.findDepartmentById(2L))
                .thenReturn(newDepartment);

        when(
                helper.normalizeRequiredText(
                        " DESK-05 ",
                        "Desk number"
                )
        ).thenReturn("DESK-05");

        when(
                receptionistRepository
                        .saveAndFlush(receptionist)
        ).thenReturn(receptionist);

        when(
                receptionistMapper.toResponseDTO(
                        receptionist
                )
        ).thenReturn(response);

        ReceptionistResponseDTO result =
                receptionistService.updateReceptionist(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                "DESK-05",
                receptionist.getDeskNumber()
        );

        verify(receptionistMapper).updateEntity(
                receptionist,
                request,
                newDepartment
        );

        verify(receptionistRepository)
                .saveAndFlush(receptionist);
    }

    @Test
    void deleteReceptionistUsesLockAndDeletesProfile() {
        when(
                helper.findReceptionistByIdForUpdate(1L)
        ).thenReturn(receptionist);

        receptionistService.deleteReceptionist(1L);

        verify(helper)
                .findReceptionistByIdForUpdate(1L);

        verify(receptionistRepository)
                .delete(receptionist);
    }

    @Test
    void existsByUserIdValidatesAndQueriesRepository() {
        when(
                receptionistRepository.existsByUser_Id(4L)
        ).thenReturn(true);

        boolean result =
                receptionistService.existsByUserId(4L);

        assertTrue(result);

        verify(helper).validateId(4L, "User");

        verify(receptionistRepository)
                .existsByUser_Id(4L);
    }

    @Test
    void countByDepartmentAndShiftValidatesParameters() {
        when(
                receptionistRepository
                        .countByDepartment_IdAndShiftType(
                                1L,
                                ShiftType.NIGHT
                        )
        ).thenReturn(3L);

        long result =
                receptionistService
                        .countReceptionistsByDepartmentAndShift(
                                1L,
                                ShiftType.NIGHT
                        );

        assertEquals(3L, result);

        verify(helper).validateDepartmentExist(1L);

        verify(helper).validateShiftType(
                ShiftType.NIGHT
        );
    }

    @Test
    void searchReceptionistsUsesNormalizedText() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<Receptionist> page =
                new PageImpl<>(
                        List.of(receptionist),
                        pageable,
                        1
                );

        when(
                helper.normalizeRequiredText(
                        " ana ",
                        "Search text"
                )
        ).thenReturn("ana");

        when(
                receptionistRepository
                        .searchReceptionists(
                                "ana",
                                pageable
                        )
        ).thenReturn(page);

        when(
                receptionistMapper.toResponseDTO(
                        receptionist
                )
        ).thenReturn(response);

        Page<ReceptionistResponseDTO> result =
                receptionistService.searchReceptionists(
                        " ana ",
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        assertSame(
                response,
                result.getContent().getFirst()
        );

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void getReceptionistsAppliesAllFilters() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("user.lastName").ascending()
        );

        Page<Receptionist> receptionistPage =
                new PageImpl<>(
                        List.of(receptionist),
                        pageable,
                        1
                );

        when(
                receptionistRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(receptionistPage);

        when(
                receptionistMapper.toResponseDTO(
                        receptionist
                )
        ).thenReturn(response);

        Page<ReceptionistResponseDTO> result =
                receptionistService.getReceptionists(
                        " ana ",
                        1L,
                        ShiftType.NIGHT,
                        true,
                        " desk ",
                        pageable
                );

        assertEquals(1, result.getTotalElements());

        assertSame(
                response,
                result.getContent().getFirst()
        );

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );

        verify(helper).validateDepartmentExist(1L);

        verify(helper).validateShiftType(
                ShiftType.NIGHT
        );

        verify(receptionistRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }
}
