package com.hospital.gestion.api.ward.repository;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.ward.entity.Ward;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class WardRepositoryTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer(
                    "postgres:18.4-alpine"
            )
                    .withDatabaseName("hospital_test")
                    .withUsername("hospital_test")
                    .withPassword("hospital_test");

    @DynamicPropertySource
    static void configureDatabase(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                POSTGRES::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                POSTGRES::getUsername
        );
        registry.add(
                "spring.datasource.password",
                POSTGRES::getPassword
        );
        registry.add(
                "spring.datasource.driver-class-name",
                POSTGRES::getDriverClassName
        );
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "validate"
        );
        registry.add(
                "spring.flyway.enabled",
                () -> true
        );
    }

    @Autowired
    private WardRepository wardRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Test
    void savePersistsWardAndGeneratesMetadata() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        Ward saved =
                wardRepository.saveAndFlush(
                        ward(
                                "Cardiology Ward A",
                                department,
                                true
                        )
                );

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(
                "Cardiology Ward A",
                saved.getName()
        );
        assertEquals(
                department.getId(),
                saved.getDepartment().getId()
        );
        assertTrue(saved.getIsActive());
    }

    @Test
    void existsByNameIgnoreCaseAndDepartmentReturnsTrue() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        wardRepository.saveAndFlush(
                ward(
                        "Cardiology Ward A",
                        department,
                        true
                )
        );

        assertTrue(
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                "CARDIOLOGY WARD A",
                                department.getId()
                        )
        );

        assertFalse(
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                "Unknown ward",
                                department.getId()
                        )
        );
    }

    @Test
    void existsByNameRestrictsSearchToDepartment() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY,
                        "Floor 1"
                );

        wardRepository.saveAndFlush(
                ward(
                        "Ward A",
                        cardiology,
                        true
                )
        );

        assertTrue(
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                "ward a",
                                cardiology.getId()
                        )
        );

        assertFalse(
                wardRepository
                        .existsByNameIgnoreCaseAndDepartment_Id(
                                "ward a",
                                emergency.getId()
                        )
        );
    }

    @Test
    void findByNameIgnoreCaseAndDepartmentReturnsWard() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY,
                        "Floor 1"
                );

        wardRepository.saveAndFlush(
                ward(
                        "Emergency Ward A",
                        department,
                        true
                )
        );

        Optional<Ward> result =
                wardRepository
                        .findByNameIgnoreCaseAndDepartment_Id(
                                "EMERGENCY WARD A",
                                department.getId()
                        );

        assertTrue(result.isPresent());
        assertEquals(
                "Emergency Ward A",
                result.get().getName()
        );
    }

    @Test
    void findByIsActiveReturnsMatchingWards() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        wardRepository.save(
                ward(
                        "Active Ward",
                        department,
                        true
                )
        );

        wardRepository.save(
                ward(
                        "Inactive Ward",
                        department,
                        false
                )
        );

        wardRepository.flush();

        List<Ward> result =
                wardRepository.findByIsActive(true);

        assertEquals(1, result.size());
        assertEquals(
                "Active Ward",
                result.getFirst().getName()
        );
    }

    @Test
    void findByIsActiveReturnsPaginatedResult() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        wardRepository.save(
                ward("Ward C", department, true)
        );
        wardRepository.save(
                ward("Ward A", department, true)
        );
        wardRepository.save(
                ward("Ward B", department, true)
        );
        wardRepository.flush();

        Page<Ward> result =
                wardRepository.findByIsActive(
                        true,
                        PageRequest.of(
                                0,
                                2,
                                Sort.by("name").ascending()
                        )
                );

        assertEquals(2, result.getContent().size());
        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(
                "Ward A",
                result.getContent().getFirst().getName()
        );
    }

    @Test
    void findByDepartmentReturnsOnlyDepartmentWards() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY,
                        "Floor 1"
                );

        wardRepository.save(
                ward(
                        "Cardiology Ward",
                        cardiology,
                        true
                )
        );

        wardRepository.save(
                ward(
                        "Emergency Ward",
                        emergency,
                        true
                )
        );

        wardRepository.flush();

        List<Ward> result =
                wardRepository.findByDepartment_Id(
                        cardiology.getId()
                );

        assertEquals(1, result.size());
        assertEquals(
                "Cardiology Ward",
                result.getFirst().getName()
        );
    }

    @Test
    void findByDepartmentReturnsPaginatedResult() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        wardRepository.save(
                ward("Ward A", department, true)
        );
        wardRepository.save(
                ward("Ward B", department, true)
        );
        wardRepository.save(
                ward("Ward C", department, true)
        );
        wardRepository.flush();

        Page<Ward> result =
                wardRepository.findByDepartment_Id(
                        department.getId(),
                        PageRequest.of(1, 2)
                );

        assertEquals(1, result.getNumberOfElements());
        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getNumber());
    }

    @Test
    void findByDepartmentAndActiveReturnsMatchingWards() {
        Department department =
                saveDepartment(
                        DepartmentType.EMERGENCY,
                        "Floor 1"
                );

        wardRepository.save(
                ward(
                        "Active Emergency Ward",
                        department,
                        true
                )
        );

        wardRepository.save(
                ward(
                        "Inactive Emergency Ward",
                        department,
                        false
                )
        );

        wardRepository.flush();

        List<Ward> result =
                wardRepository
                        .findByDepartment_IdAndIsActive(
                                department.getId(),
                                true
                        );

        assertEquals(1, result.size());
        assertEquals(
                "Active Emergency Ward",
                result.getFirst().getName()
        );
    }

    @Test
    void findByDepartmentAndActiveReturnsPaginatedResult() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        wardRepository.save(
                ward("Active Ward A", department, true)
        );
        wardRepository.save(
                ward("Active Ward B", department, true)
        );
        wardRepository.save(
                ward("Inactive Ward", department, false)
        );
        wardRepository.flush();

        Page<Ward> result =
                wardRepository
                        .findByDepartment_IdAndIsActive(
                                department.getId(),
                                true,
                                PageRequest.of(0, 1)
                        );

        assertEquals(1, result.getNumberOfElements());
        assertEquals(2L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void findByNameContainingIgnoreCaseReturnsMatchingWards() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        wardRepository.save(
                ward(
                        "Cardiology Intensive Care",
                        department,
                        true
                )
        );

        wardRepository.save(
                ward(
                        "General Observation",
                        department,
                        true
                )
        );

        wardRepository.flush();

        List<Ward> result =
                wardRepository
                        .findByNameContainingIgnoreCase(
                                "INTENSIVE"
                        );

        assertEquals(1, result.size());
        assertEquals(
                "Cardiology Intensive Care",
                result.getFirst().getName()
        );
    }

    @Test
    void findByNameContainingIgnoreCaseReturnsPaginatedResult() {
        Department department =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        wardRepository.save(
                ward("Cardiology Ward A", department, true)
        );
        wardRepository.save(
                ward("Cardiology Ward B", department, true)
        );
        wardRepository.save(
                ward("General Ward", department, true)
        );
        wardRepository.flush();

        Page<Ward> result =
                wardRepository
                        .findByNameContainingIgnoreCase(
                                "cardiology",
                                PageRequest.of(0, 1)
                        );

        assertEquals(1, result.getNumberOfElements());
        assertEquals(2L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
    }

    @Test
    void countMethodsReturnCorrectValues() {
        Department cardiology =
                saveDepartment(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2"
                );

        Department emergency =
                saveDepartment(
                        DepartmentType.EMERGENCY,
                        "Floor 1"
                );

        wardRepository.save(
                ward(
                        "Cardiology Ward A",
                        cardiology,
                        true
                )
        );

        wardRepository.save(
                ward(
                        "Cardiology Ward B",
                        cardiology,
                        false
                )
        );

        wardRepository.save(
                ward(
                        "Emergency Ward",
                        emergency,
                        true
                )
        );

        wardRepository.flush();

        assertEquals(
                2L,
                wardRepository.countByIsActive(true)
        );

        assertEquals(
                2L,
                wardRepository.countByDepartment_Id(
                        cardiology.getId()
                )
        );

        assertEquals(
                1L,
                wardRepository
                        .countByDepartment_IdAndIsActive(
                                cardiology.getId(),
                                true
                        )
        );
    }

    private Department saveDepartment(
            DepartmentType type,
            String location
    ) {
        return departmentRepository.saveAndFlush(
                Department.builder()
                        .departmentType(type)
                        .location(location)
                        .phoneExtension("100")
                        .description(
                                type + " department"
                        )
                        .isActive(true)
                        .build()
        );
    }

    private Ward ward(
            String name,
            Department department,
            boolean active
    ) {
        return Ward.builder()
                .name(name)
                .description(name + " description")
                .isActive(active)
                .department(department)
                .build();
    }
}
