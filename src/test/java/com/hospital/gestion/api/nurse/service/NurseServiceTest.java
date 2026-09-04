package com.hospital.gestion.api.nurse.service;

import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.nurse.dto.NurseRequestDTO;
import com.hospital.gestion.api.nurse.dto.NurseResponseDTO;
import com.hospital.gestion.api.nurse.dto.NurseStatsResponse;
import com.hospital.gestion.api.nurse.dto.NurseUpdateDTO;
import com.hospital.gestion.api.nurse.entity.Nurse;
import com.hospital.gestion.api.nurse.mapper.NurseMapper;
import com.hospital.gestion.api.nurse.repository.NurseRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NurseServiceTest {

    @Mock
    private NurseRepository nurseRepository;

    @Mock
    private NurseMapper nurseMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private NurseService nurseService;

    private User user;
    private Department department;
    private Nurse nurse;
    private NurseResponseDTO response;


    @BeforeEach
    void setUp() {
        user = mock(User.class);
        department = mock(Department.class);
        response = mock(NurseResponseDTO.class);

        lenient().when(user.getId())
                .thenReturn(3L);

        lenient().when(user.getRole())
                .thenReturn(Role.NURSE);

        lenient().when(user.getIsActive())
                .thenReturn(true);

        nurse = Nurse.builder()
                .id(1L)
                .user(user)
                .department(department)
                .licenseNumber("NUR-12345")
                .specialty(NurseSpecialty.EMERGENCY)
                .shiftType(ShiftType.ROTATING)
                .yearsOfExperience(8)
                .hireDate(LocalDate.of(2020, 6, 15))
                .biography("Emergency nurse")
                .maxPatientsPerShift(7)
                .isChargeNurse(true)
                .vacationDaysAvailable(19)
                .build();
    }
    private NurseRequestDTO request() {
        return new NurseRequestDTO(
                3L,
                2L,
                " NUR-12345 ",
                NurseSpecialty.EMERGENCY,
                ShiftType.ROTATING,
                8,
                LocalDate.of(2020, 6, 15),
                " Emergency nurse ",
                "Juan Familiar",
                "633555777",
                "BROTHER",
                7,
                true,
                19
        );
    }

    @Test
    void createNurseNormalizesAndSavesNurse() {
        NurseRequestDTO request = request();

        when(helper.findUserByIdForUpdate(3L))
                .thenReturn(user);

        when(helper.findDepartmentById(2L))
                .thenReturn(department);

        when(nurseRepository.existsByUser_Id(3L))
                .thenReturn(false);

        when(
                helper.normalizeRequiredText(
                        " NUR-12345 ",
                        "License number"
                )
        ).thenReturn("NUR-12345");

        when(
                nurseRepository
                        .existsByLicenseNumberIgnoreCase(
                                "NUR-12345"
                        )
        ).thenReturn(false);

        when(
                nurseMapper.toEntity(
                        request,
                        user,
                        department
                )
        ).thenReturn(nurse);

        when(
                helper.normalizeNullableText(
                        " Emergency nurse "
                )
        ).thenReturn("Emergency nurse");

        when(nurseRepository.save(nurse))
                .thenReturn(nurse);

        when(nurseMapper.toResponseDTO(nurse))
                .thenReturn(response);

        NurseResponseDTO result =
                nurseService.createNurse(request);

        assertSame(response, result);
        assertEquals("NUR-12345", nurse.getLicenseNumber());
        assertEquals("Emergency nurse", nurse.getBiography());

        verify(helper).findUserByIdForUpdate(3L);
        verify(helper).findDepartmentById(2L);
        verify(nurseRepository).save(nurse);
    }

    @Test
    void createNurseRejectsExistingUserProfile() {
        NurseRequestDTO request = request();

        when(helper.findUserByIdForUpdate(3L))
                .thenReturn(user);

        when(nurseRepository.existsByUser_Id(3L))
                .thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> nurseService.createNurse(request)
        );

        assertEquals(
                "A nurse profile already exists for user: 3",
                exception.getMessage()
        );

        verify(helper, never()).normalizeRequiredText(
                anyString(),
                anyString()
        );

        verify(helper, never())
                .findDepartmentById(anyLong());

        verify(nurseRepository, never())
                .save(any(Nurse.class));
    }

    @Test
    void createNurseRejectsDuplicateLicense() {
        NurseRequestDTO request = request();

        when(helper.findUserByIdForUpdate(3L))
                .thenReturn(user);

        when(
                helper.normalizeRequiredText(
                        " NUR-12345 ",
                        "License number"
                )
        ).thenReturn("NUR-12345");

        when(
                nurseRepository
                        .existsByLicenseNumberIgnoreCase(
                                "NUR-12345"
                        )
        ).thenReturn(true);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> nurseService.createNurse(request)
        );

        assertEquals(
                "License number already exists: NUR-12345",
                exception.getMessage()
        );

        verify(helper, never())
                .findDepartmentById(anyLong());

        verify(nurseMapper, never()).toEntity(
                any(),
                any(),
                any()
        );

        verify(nurseRepository, never())
                .save(any(Nurse.class));
    }

    @Test
    void createNurseAppliesDefaultValues() {
        NurseRequestDTO request = request();

        Nurse nurseWithoutDefaults = Nurse.builder()
                .id(2L)
                .user(user)
                .department(department)
                .licenseNumber("NUR-99999")
                .specialty(null)
                .shiftType(ShiftType.MORNING)
                .isChargeNurse(null)
                .build();

        when(helper.findUserByIdForUpdate(3L))
                .thenReturn(user);

        when(helper.findDepartmentById(2L))
                .thenReturn(department);

        when(
                helper.normalizeRequiredText(
                        " NUR-12345 ",
                        "License number"
                )
        ).thenReturn("NUR-12345");

        when(
                nurseMapper.toEntity(
                        request,
                        user,
                        department
                )
        ).thenReturn(nurseWithoutDefaults);

        when(
                helper.normalizeNullableText(
                        " Emergency nurse "
                )
        ).thenReturn("Emergency nurse");

        when(
                nurseRepository.save(nurseWithoutDefaults)
        ).thenReturn(nurseWithoutDefaults);

        when(
                nurseMapper.toResponseDTO(
                        nurseWithoutDefaults
                )
        ).thenReturn(response);

        nurseService.createNurse(request);

        assertEquals(
                NurseSpecialty.GENERAL,
                nurseWithoutDefaults.getSpecialty()
        );

        assertFalse(
                nurseWithoutDefaults.getIsChargeNurse()
        );
    }

    @Test
    void getNurseByLicenseUsesNormalizedLicense() {
        when(
                helper.normalizeRequiredText(
                        " nur-12345 ",
                        "License number"
                )
        ).thenReturn("NUR-12345");

        when(
                nurseRepository.findByLicenseNumberIgnoreCase(
                        "NUR-12345"
                )
        ).thenReturn(Optional.of(nurse));

        when(nurseMapper.toResponseDTO(nurse))
                .thenReturn(response);

        NurseResponseDTO result =
                nurseService.getNurseByLicense(
                        " nur-12345 "
                );

        assertSame(response, result);

        verify(nurseRepository)
                .findByLicenseNumberIgnoreCase(
                        "NUR-12345"
                );
    }

    @Test
    void updateNurseUpdatesAndSavesNurse() {
        Department newDepartment =
                mock(Department.class);

        NurseUpdateDTO request =
                new NurseUpdateDTO(
                        1L,
                        NurseSpecialty.CARDIOLOGY,
                        ShiftType.MORNING,
                        10,
                        LocalDate.of(2020, 6, 15),
                        " Updated biography ",
                        "Maria Familiar",
                        "611222333",
                        "SISTER",
                        8,
                        false,
                        25
                );

        when(helper.findNurseByIdForUpdate(1L))
                .thenReturn(nurse);

        when(helper.findDepartmentById(1L))
                .thenReturn(newDepartment);

        when(
                helper.normalizeNullableText(
                        " Updated biography "
                )
        ).thenReturn("Updated biography");

        when(nurseRepository.saveAndFlush(nurse))
                .thenReturn(nurse);

        when(nurseMapper.toResponseDTO(nurse))
                .thenReturn(response);

        NurseResponseDTO result =
                nurseService.updateNurse(
                        1L,
                        request
                );

        assertSame(response, result);

        assertEquals(
                "Updated biography",
                nurse.getBiography()
        );

        verify(nurseMapper).updateEntity(
                nurse,
                request,
                newDepartment
        );

        verify(nurseRepository)
                .saveAndFlush(nurse);
    }

    @Test
    void deleteNurseDeletesExistingNurse() {
        when(helper.findNurseByIdForUpdate(1L))
                .thenReturn(nurse);

        nurseService.deleteNurse(1L);

        verify(helper).findNurseByIdForUpdate(1L);
        verify(nurseRepository).delete(nurse);
    }

    @Test
    void existsByUserIdValidatesAndQueriesRepository() {
        when(nurseRepository.existsByUser_Id(3L))
                .thenReturn(true);

        boolean result = nurseService.existsByUserId(3L);

        assertTrue(result);

        verify(helper).validateId(3L, "User");
        verify(nurseRepository).existsByUser_Id(3L);
    }

    @Test
    void existsByLicenseUsesNormalizedLicense() {
        when(
                helper.normalizeRequiredText(
                        " nur-12345 ",
                        "License number"
                )
        ).thenReturn("NUR-12345");

        when(
                nurseRepository
                        .existsByLicenseNumberIgnoreCase(
                                "NUR-12345"
                        )
        ).thenReturn(true);

        boolean result = nurseService.existsByLicense(
                " nur-12345 "
        );

        assertTrue(result);

        verify(nurseRepository)
                .existsByLicenseNumberIgnoreCase(
                        "NUR-12345"
                );
    }

    @Test
    void getNurseStatsReturnsAllShiftCounts() {
        when(nurseRepository.count())
                .thenReturn(10L);

        when(
                nurseRepository.countByShiftType(
                        ShiftType.MORNING
                )
        ).thenReturn(3L);

        when(
                nurseRepository.countByShiftType(
                        ShiftType.AFTERNOON
                )
        ).thenReturn(2L);

        when(
                nurseRepository.countByShiftType(
                        ShiftType.NIGHT
                )
        ).thenReturn(1L);

        when(
                nurseRepository.countByShiftType(
                        ShiftType.ROTATING
                )
        ).thenReturn(4L);

        NurseStatsResponse result =
                nurseService.getNurseStats();

        assertEquals(10L, result.total());
        assertEquals(3L, result.morning());
        assertEquals(2L, result.afternoon());
        assertEquals(1L, result.night());
        assertEquals(4L, result.rotating());
    }

    @Test
    void countNursesByDepartmentValidatesDepartment() {
        when(nurseRepository.countByDepartment_Id(2L))
                .thenReturn(5L);

        long result =
                nurseService.countNursesByDepartment(2L);

        assertEquals(5L, result);

        verify(helper).validateDepartmentExist(2L);
        verify(nurseRepository).countByDepartment_Id(2L);
    }

    @Test
    void getNursesRejectsInvalidExperienceRange() {
        Pageable pageable = PageRequest.of(0, 20);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> nurseService.getNurses(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        30,
                        10,
                        null,
                        null,
                        pageable
                )
        );

        assertEquals(
                "Minimum experience cannot be greater than maximum experience",
                exception.getMessage()
        );

        verify(helper).validateExperience(30);
        verify(helper).validateExperience(10);

        verify(nurseRepository, never()).findAll(
                any(Specification.class),
                any(Pageable.class)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    void getNursesAppliesAllFilters() {
        Pageable pageable = PageRequest.of(
                0,
                20,
                Sort.by("user.lastName").ascending()
        );

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate to = LocalDate.of(2020, 12, 31);

        Page<Nurse> nursePage =
                new PageImpl<>(
                        List.of(nurse),
                        pageable,
                        1
                );

        when(
                nurseRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(nursePage);

        when(nurseMapper.toResponseDTO(nurse))
                .thenReturn(response);

        Page<NurseResponseDTO> result =
                nurseService.getNurses(
                        " maria ",
                        2L,
                        NurseSpecialty.EMERGENCY,
                        ShiftType.ROTATING,
                        true,
                        true,
                        0,
                        60,
                        from,
                        to,
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

        verify(helper).validateDepartmentExist(2L);

        verify(helper).validateSpecialty(
                NurseSpecialty.EMERGENCY
        );

        verify(helper).validateShiftType(
                ShiftType.ROTATING
        );

        verify(helper).validateExperience(0);
        verify(helper).validateExperience(60);

        verify(helper).validateDateRangeLocalDate(
                from,
                to
        );

        verify(nurseRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }



    @Test
    void createNurseRejectsUserWithoutNurseRole() {
        NurseRequestDTO request = request();

        when(user.getRole()).thenReturn(Role.DOCTOR);

        when(helper.findUserByIdForUpdate(3L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> nurseService.createNurse(request)
        );

        assertEquals(
                "User must have NURSE role",
                exception.getMessage()
        );

        verify(helper, never())
                .findDepartmentById(anyLong());

        verify(nurseRepository, never())
                .save(any(Nurse.class));
    }

    @Test
    void createNurseRejectsInactiveUser() {
        NurseRequestDTO request = request();

        when(user.getIsActive()).thenReturn(false);

        when(helper.findUserByIdForUpdate(3L))
                .thenReturn(user);

        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> nurseService.createNurse(request)
        );

        assertEquals(
                "Inactive user cannot be registered as a nurse",
                exception.getMessage()
        );

        verify(helper, never())
                .findDepartmentById(anyLong());

        verify(nurseRepository, never())
                .save(any(Nurse.class));
    }
}
