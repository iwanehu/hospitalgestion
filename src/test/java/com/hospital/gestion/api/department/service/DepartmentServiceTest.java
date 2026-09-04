package com.hospital.gestion.api.department.service;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.dto.DepartmentRequestDTO;
import com.hospital.gestion.api.department.dto.DepartmentResponseDTO;
import com.hospital.gestion.api.department.dto.DepartmentUpdateDTO;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.mapper.DepartmentMapper;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
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
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentMapper departmentMapper;

    @Mock
    private HospitalEntityHelper helper;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void createDepartmentSavesAndReturnsResponse() {
        DepartmentRequestDTO request =
                new DepartmentRequestDTO(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "201",
                        "Cardiology department"
                );

        Department department = department(1L, true);
        DepartmentResponseDTO expected =
                response(1L, true);

        when(
                departmentRepository.existsByDepartmentType(
                        DepartmentType.CARDIOLOGY
                )
        ).thenReturn(false);

        when(
                departmentRepository.existsByLocation(
                        "Floor 2"
                )
        ).thenReturn(false);

        when(departmentMapper.toEntity(request))
                .thenReturn(department);

        when(departmentRepository.save(department))
                .thenReturn(department);

        when(departmentMapper.toResponseDTO(department))
                .thenReturn(expected);

        DepartmentResponseDTO result =
                departmentService.createDepartment(request);

        assertSame(expected, result);

        verify(departmentRepository).save(department);
        verify(departmentMapper).toResponseDTO(department);
    }

    @Test
    void createDepartmentRejectsDuplicatedType() {
        DepartmentRequestDTO request =
                new DepartmentRequestDTO(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "201",
                        "Cardiology department"
                );

        when(
                departmentRepository.existsByDepartmentType(
                        DepartmentType.CARDIOLOGY
                )
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> departmentService.createDepartment(request)
        );

        verify(
                departmentRepository,
                never()
        ).existsByLocation(anyString());

        verify(
                departmentRepository,
                never()
        ).save(any(Department.class));

        verifyNoInteractions(departmentMapper);
    }

    @Test
    void createDepartmentRejectsDuplicatedLocation() {
        DepartmentRequestDTO request =
                new DepartmentRequestDTO(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "201",
                        "Cardiology department"
                );

        when(
                departmentRepository.existsByDepartmentType(
                        DepartmentType.CARDIOLOGY
                )
        ).thenReturn(false);

        when(
                departmentRepository.existsByLocation(
                        "Floor 2"
                )
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> departmentService.createDepartment(request)
        );

        verify(
                departmentRepository,
                never()
        ).save(any(Department.class));

        verifyNoInteractions(departmentMapper);
    }

    @Test
    void getDepartmentByIdReturnsMappedDepartment() {
        Department department = department(1L, true);
        DepartmentResponseDTO expected =
                response(1L, true);

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        when(departmentMapper.toResponseDTO(department))
                .thenReturn(expected);

        DepartmentResponseDTO result =
                departmentService.getDepartmentById(1L);

        assertSame(expected, result);

        verify(helper).findDepartmentById(1L);
    }

    @Test
    void getDepartmentByIdRejectsNullId() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> departmentService
                                .getDepartmentById(null)
                );

        assertEquals(
                "Department id cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(
                departmentRepository,
                departmentMapper,
                helper
        );
    }

    @Test
    void getDepartmentByTypeThrowsWhenNotFound() {
        when(
                departmentRepository.findByDepartmentType(
                        DepartmentType.CARDIOLOGY
                )
        ).thenReturn(java.util.Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.getDepartmentByType(
                        DepartmentType.CARDIOLOGY
                )
        );

        verifyNoInteractions(departmentMapper);
    }

    @Test
    void updateDepartmentUpdatesAndReturnsResponse() {
        Department department = department(1L, true);

        DepartmentUpdateDTO request =
                new DepartmentUpdateDTO(
                        "Floor 3",
                        "301",
                        "Updated department",
                        true
                );

        DepartmentResponseDTO expected =
                new DepartmentResponseDTO(
                        1L,
                        DepartmentType.CARDIOLOGY,
                        "Floor 3",
                        "301",
                        "Updated department",
                        true,
                        0,
                        null,
                        null
                );

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        when(
                departmentRepository.existsByLocation(
                        "Floor 3"
                )
        ).thenReturn(false);

        when(
                departmentRepository.saveAndFlush(department)
        ).thenReturn(department);

        when(departmentMapper.toResponseDTO(department))
                .thenReturn(expected);

        DepartmentResponseDTO result =
                departmentService.updateDepartmentById(
                        1L,
                        request
                );

        assertSame(expected, result);

        verify(departmentMapper).updateEntity(
                department,
                request
        );

        verify(departmentRepository)
                .saveAndFlush(department);
    }

    @Test
    void updateDepartmentRejectsDuplicatedLocation() {
        Department department = department(1L, true);

        DepartmentUpdateDTO request =
                new DepartmentUpdateDTO(
                        "Floor 3",
                        "301",
                        "Updated department",
                        true
                );

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        when(
                departmentRepository.existsByLocation(
                        "Floor 3"
                )
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> departmentService
                        .updateDepartmentById(1L, request)
        );

        verify(
                departmentMapper,
                never()
        ).updateEntity(any(), any());

        verify(
                departmentRepository,
                never()
        ).saveAndFlush(any());
    }

    @Test
    void activateDepartmentChangesInactiveDepartment() {
        Department department = department(1L, false);

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        departmentService.activateDepartment(1L);

        assertTrue(department.getIsActive());
    }

    @Test
    void activateDepartmentRejectsAlreadyActiveDepartment() {
        Department department = department(1L, true);

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        assertThrows(
                ConflictException.class,
                () -> departmentService.activateDepartment(1L)
        );

        assertTrue(department.getIsActive());
    }

    @Test
    void deactivateDepartmentChangesActiveDepartment() {
        Department department = department(1L, true);

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        departmentService.deactivateDepartment(1L);

        assertFalse(department.getIsActive());
    }

    @Test
    void deleteDepartmentDeletesResolvedEntity() {
        Department department = department(1L, true);

        when(helper.findDepartmentById(1L))
                .thenReturn(department);

        departmentService.deleteDepartment(1L);

        verify(departmentRepository).delete(department);
    }

    @Test
    void getDepartmentsReturnsFilteredPage() {
        Department department = department(1L, true);
        DepartmentResponseDTO expected =
                response(1L, true);

        Pageable pageable =
                PageRequest.of(
                        0,
                        20,
                        Sort.by("departmentType").ascending()
                );

        Page<Department> departments =
                new PageImpl<>(
                        List.of(department),
                        pageable,
                        1
                );

        when(
                departmentRepository.findAll(
                        any(Specification.class),
                        eq(pageable)
                )
        ).thenReturn(departments);

        when(departmentMapper.toResponseDTO(department))
                .thenReturn(expected);

        Page<DepartmentResponseDTO> result =
                departmentService.getDepartments(
                        DepartmentType.CARDIOLOGY,
                        true,
                        " Floor ",
                        " cardiac ",
                        pageable
                );

        assertEquals(1, result.getTotalElements());
        assertSame(expected, result.getContent().getFirst());

        verify(helper).validatePageable(
                eq(pageable),
                anySet()
        );

        verify(departmentRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    private Department department(
            Long id,
            Boolean isActive
    ) {
        return Department.builder()
                .id(id)
                .departmentType(
                        DepartmentType.CARDIOLOGY
                )
                .location("Floor 2")
                .phoneExtension("201")
                .description("Cardiology department")
                .isActive(isActive)
                .build();
    }

    private DepartmentResponseDTO response(
            Long id,
            Boolean isActive
    ) {
        return new DepartmentResponseDTO(
                id,
                DepartmentType.CARDIOLOGY,
                "Floor 2",
                "201",
                "Cardiology department",
                isActive,
                0,
                null,
                null
        );
    }
}

