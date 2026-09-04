package com.hospital.gestion.api.ward.service;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.ward.dto.WardRequestDTO;
import com.hospital.gestion.api.ward.dto.WardResponseDTO;
import com.hospital.gestion.api.ward.dto.WardUpdateDTO;
import com.hospital.gestion.api.ward.entity.Ward;
import com.hospital.gestion.api.ward.mapper.WardMapper;
import com.hospital.gestion.api.ward.repository.WardRepository;
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
class WardServiceTest {

    @Mock
    private WardRepository wardRepository;

    @Mock
    private WardMapper wardMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private WardService wardService;

    @Test
    void createWardNormalizesAndSavesWard() {
        Department department = department(1L);

        WardRequestDTO request =
                new WardRequestDTO(
                        " Cardiology Ward ",
                        " Main hospitalization ward ",
                        1L
                );

        Ward ward = ward(
                1L,
                " Cardiology Ward ",
                true,
                department
        );

        WardResponseDTO expected = response(
                1L,
                "Cardiology Ward",
                true,
                1L
        );

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        when(
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                "Cardiology Ward",
                                1L
                        )
        ).thenReturn(false);

        when(wardMapper.toEntity(request, department))
                .thenReturn(ward);

        when(
                helper.normalizeNullableText(
                        " Main hospitalization ward "
                )
        ).thenReturn("Main hospitalization ward");

        when(wardRepository.save(ward))
                .thenReturn(ward);

        when(wardMapper.toResponseDTO(ward))
                .thenReturn(expected);

        WardResponseDTO result =
                wardService.createWard(request);

        assertSame(expected, result);
        assertEquals("Cardiology Ward", ward.getName());
        assertEquals(
                "Main hospitalization ward",
                ward.getDescription()
        );

        verify(helper).validateWardName(
                " Cardiology Ward "
        );

        verify(wardRepository).save(ward);
    }

    @Test
    void createWardRejectsDuplicateInSameDepartment() {
        Department department = department(1L);

        WardRequestDTO request =
                new WardRequestDTO(
                        "Cardiology Ward",
                        "Description",
                        1L
                );

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        when(
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                "Cardiology Ward",
                                1L
                        )
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> wardService.createWard(request)
        );

        verify(
                wardRepository,
                never()
        ).save(any(Ward.class));

        verifyNoInteractions(wardMapper);
    }

    @Test
    void sameWardNameIsAllowedInDifferentDepartment() {
        Department department = department(2L);

        WardRequestDTO request =
                new WardRequestDTO(
                        "General Ward",
                        null,
                        2L
                );

        Ward ward = ward(
                2L,
                "General Ward",
                true,
                department
        );

        WardResponseDTO expected = response(
                2L,
                "General Ward",
                true,
                2L
        );

        when(helper.findDepartmentById(2L))
                .thenReturn(department);

        when(
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                "General Ward",
                                2L
                        )
        ).thenReturn(false);

        when(wardMapper.toEntity(request, department))
                .thenReturn(ward);

        when(wardRepository.save(ward))
                .thenReturn(ward);

        when(wardMapper.toResponseDTO(ward))
                .thenReturn(expected);

        WardResponseDTO result =
                wardService.createWard(request);

        assertSame(expected, result);

        verify(wardRepository)
                .existsByNameIgnoreCaseAndDepartment_Id(
                        "General Ward",
                        2L
                );
    }

    @Test
    void getWardByIdReturnsMappedWard() {
        Ward ward = ward(
                1L,
                "Cardiology Ward",
                true,
                department(1L)
        );

        WardResponseDTO expected = response(
                1L,
                "Cardiology Ward",
                true,
                1L
        );

        when(helper.findWardById(1L))
                .thenReturn(ward);

        when(wardMapper.toResponseDTO(ward))
                .thenReturn(expected);

        WardResponseDTO result =
                wardService.getWardById(1L);

        assertSame(expected, result);
    }

    @Test
    void getWardByNameAndDepartmentThrowsWhenMissing() {
        when(
                wardRepository
                        .findByNameIgnoreCaseAndDepartment_Id(
                                "Missing Ward",
                                1L
                        )
        ).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> wardService
                        .getWardByNameAndDepartment(
                                " Missing Ward ",
                                1L
                        )
        );

        verify(helper).validateWardName(
                " Missing Ward "
        );

        verify(helper).validateDepartmentExist(1L);
        verifyNoInteractions(wardMapper);
    }

    @Test
    void updateWardWithSameNameDoesNotCheckDuplicate() {
        Department department = department(1L);

        Ward ward = ward(
                1L,
                "Cardiology Ward",
                true,
                department
        );

        WardUpdateDTO request =
                new WardUpdateDTO(
                        " cardiology ward ",
                        " Updated description ",
                        true
                );

        WardResponseDTO expected = response(
                1L,
                "cardiology ward",
                true,
                1L
        );

        when(helper.findWardById(1L))
                .thenReturn(ward);

        when(
                helper.normalizeNullableText(
                        " Updated description "
                )
        ).thenReturn("Updated description");

        when(wardRepository.save(ward))
                .thenReturn(ward);

        when(wardMapper.toResponseDTO(ward))
                .thenReturn(expected);

        WardResponseDTO result =
                wardService.updateWardById(
                        1L,
                        request
                );

        assertSame(expected, result);
        assertEquals("cardiology ward", ward.getName());
        assertEquals(
                "Updated description",
                ward.getDescription()
        );

        verify(
                wardRepository,
                never()
        ).existsByNameIgnoreCaseAndDepartment_Id(
                anyString(),
                anyLong()
        );

        verify(wardMapper).updateEntity(ward, request);
    }

    @Test
    void updateWardRejectsDuplicateNameInDepartment() {
        Ward ward = ward(
                1L,
                "Old Ward",
                true,
                department(1L)
        );

        WardUpdateDTO request =
                new WardUpdateDTO(
                        "Existing Ward",
                        "Description",
                        true
                );

        when(helper.findWardById(1L))
                .thenReturn(ward);

        when(
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                "Existing Ward",
                                1L
                        )
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> wardService.updateWardById(
                        1L,
                        request
                )
        );

        verify(
                wardMapper,
                never()
        ).updateEntity(any(), any());

        verify(
                wardRepository,
                never()
        ).save(any());
    }

    @Test
    void deleteWardDeletesWardWithoutRooms() {
        Ward ward = ward(
                1L,
                "Cardiology Ward",
                true,
                department(1L)
        );

        when(helper.findWardById(1L))
                .thenReturn(ward);

        wardService.deleteWard(1L);

        verify(wardRepository).delete(ward);
    }

    @Test
    void deleteWardRejectsWardContainingRooms() {
        Ward ward = ward(
                1L,
                "Cardiology Ward",
                true,
                department(1L)
        );

        ward.getRooms().add(mock(Room.class));

        when(helper.findWardById(1L))
                .thenReturn(ward);

        assertThrows(
                ConflictException.class,
                () -> wardService.deleteWard(1L)
        );

        verify(
                wardRepository,
                never()
        ).delete(any(Ward.class));
    }

    @Test
    void activateWardChangesInactiveWard() {
        Ward ward = ward(
                1L,
                "Cardiology Ward",
                false,
                department(1L)
        );

        when(helper.findWardById(1L))
                .thenReturn(ward);

        wardService.activateWard(1L);

        assertTrue(ward.getIsActive());
    }

    @Test
    void activateWardRejectsAlreadyActiveWard() {
        Ward ward = ward(
                1L,
                "Cardiology Ward",
                true,
                department(1L)
        );

        when(helper.findWardById(1L))
                .thenReturn(ward);

        assertThrows(
                ConflictException.class,
                () -> wardService.activateWard(1L)
        );
    }

    @Test
    void deactivateWardChangesActiveWard() {
        Ward ward = ward(
                1L,
                "Cardiology Ward",
                true,
                department(1L)
        );

        when(helper.findWardById(1L))
                .thenReturn(ward);

        wardService.deactivateWard(1L);

        assertFalse(ward.getIsActive());
    }

    @Test
    void getWardsReturnsFilteredPage() {
        Ward ward = ward(
                1L,
                "Cardiology Ward",
                true,
                department(1L)
        );

        WardResponseDTO expected = response(
                1L,
                "Cardiology Ward",
                true,
                1L
        );

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by("name").ascending()
                );

        Page<Ward> wards =
                new PageImpl<>(
                        List.of(ward),
                        pageable,
                        1
                );

        when(
                wardRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(wards);

        when(wardMapper.toResponseDTO(ward))
                .thenReturn(expected);

        Page<WardResponseDTO> result =
                wardService.getWards(
                        " Cardiology ",
                        " Main ",
                        true,
                        1L,
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertSame(expected, result.getContent().getFirst());

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );

        verify(helper).validateDepartmentExist(1L);

        verify(wardRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    private Department department(Long id) {
        return Department.builder()
                .id(id)
                .departmentType(
                        DepartmentType.CARDIOLOGY
                )
                .location("Floor 2")
                .isActive(true)
                .build();
    }

    private Ward ward(
            Long id,
            String name,
            Boolean active,
            Department department
    ) {
        return Ward.builder()
                .id(id)
                .name(name)
                .description("Main ward")
                .isActive(active)
                .department(department)
                .build();
    }

    private WardResponseDTO response(
            Long id,
            String name,
            Boolean active,
            Long departmentId
    ) {
        return new WardResponseDTO(
                id,
                name,
                "Main ward",
                active,
                departmentId,
                DepartmentType.CARDIOLOGY.name(),
                0,
                null,
                null
        );
    }
}
