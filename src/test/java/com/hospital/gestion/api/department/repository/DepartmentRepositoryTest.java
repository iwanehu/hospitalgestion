package com.hospital.gestion.api.department.repository;

import com.hospital.gestion.api.common.enums.DepartmentType;
import com.hospital.gestion.api.department.entity.Department;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
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
class DepartmentRepositoryTest {

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
    private DepartmentRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void savePersistsDepartmentAndGeneratesMetadata() {
        Department department =
                department(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "Cardiology department",
                        true
                );

        Department saved =
                repository.saveAndFlush(department);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(
                DepartmentType.CARDIOLOGY,
                saved.getDepartmentType()
        );
        assertTrue(saved.getIsActive());
    }

    @Test
    void findByDepartmentTypeReturnsMatchingDepartment() {
        repository.saveAndFlush(
                department(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "Cardiology department",
                        true
                )
        );

        Optional<Department> result =
                repository.findByDepartmentType(
                        DepartmentType.CARDIOLOGY
                );

        assertTrue(result.isPresent());
        assertEquals(
                "Floor 2",
                result.get().getLocation()
        );
    }

    @Test
    void existsByDepartmentTypeReturnsExpectedValue() {
        repository.saveAndFlush(
                department(
                        DepartmentType.EMERGENCY,
                        "Floor 1",
                        "Emergency department",
                        true
                )
        );

        assertTrue(
                repository.existsByDepartmentType(
                        DepartmentType.EMERGENCY
                )
        );

        assertFalse(
                repository.existsByDepartmentType(
                        DepartmentType.CARDIOLOGY
                )
        );
    }

    @Test
    void findByDepartmentTypeAndActiveReturnsOnlyActiveDepartment() {
        repository.saveAndFlush(
                department(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "Cardiology department",
                        false
                )
        );

        Optional<Department> result =
                repository.findByDepartmentTypeAndActive(
                        DepartmentType.CARDIOLOGY
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void findByIsActiveReturnsDepartmentsWithRequestedStatus() {
        repository.save(
                department(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "Cardiology department",
                        true
                )
        );

        repository.save(
                department(
                        DepartmentType.EMERGENCY,
                        "Floor 1",
                        "Emergency department",
                        false
                )
        );

        repository.flush();

        List<Department> activeDepartments =
                repository.findByIsActive(true);

        assertEquals(1, activeDepartments.size());
        assertEquals(
                DepartmentType.CARDIOLOGY,
                activeDepartments.getFirst()
                        .getDepartmentType()
        );
    }

    @Test
    void findActiveDepartmentsOrdersByDepartmentType() {
        repository.save(
                department(
                        DepartmentType.EMERGENCY,
                        "Floor 1",
                        "Emergency department",
                        true
                )
        );

        repository.save(
                department(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "Cardiology department",
                        true
                )
        );

        repository.flush();

        List<Department> result =
                repository
                        .findByIsActiveTrueOrderByDepartmentTypeAsc();

        assertEquals(2, result.size());
        assertEquals(
                DepartmentType.CARDIOLOGY,
                result.get(0).getDepartmentType()
        );
        assertEquals(
                DepartmentType.EMERGENCY,
                result.get(1).getDepartmentType()
        );
    }

    @Test
    void countByIsActiveReturnsCorrectCount() {
        repository.save(
                department(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "Cardiology department",
                        true
                )
        );

        repository.save(
                department(
                        DepartmentType.EMERGENCY,
                        "Floor 1",
                        "Emergency department",
                        false
                )
        );

        repository.flush();

        assertEquals(
                1L,
                repository.countByIsActive(true)
        );

        assertEquals(
                1L,
                repository.countByIsActive(false)
        );
    }

    @Test
    void findByLocationAndExistsByLocationWorkCorrectly() {
        repository.saveAndFlush(
                department(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Wing - Floor 2",
                        "Cardiology department",
                        true
                )
        );

        Optional<Department> result =
                repository.findByLocation(
                        "Cardiology Wing - Floor 2"
                );

        assertTrue(result.isPresent());
        assertTrue(
                repository.existsByLocation(
                        "Cardiology Wing - Floor 2"
                )
        );
        assertFalse(
                repository.existsByLocation(
                        "Unknown floor"
                )
        );
    }

    @Test
    void findByLocationContainingIgnoreCaseIgnoresCase() {
        repository.saveAndFlush(
                department(
                        DepartmentType.CARDIOLOGY,
                        "Cardiology Wing - Floor 2",
                        "Cardiology department",
                        true
                )
        );

        List<Department> result =
                repository
                        .findByLocationContainingIgnoreCase(
                                "FLOOR"
                        );

        assertEquals(1, result.size());
        assertEquals(
                DepartmentType.CARDIOLOGY,
                result.getFirst()
                        .getDepartmentType()
        );
    }

    @Test
    void findByDescriptionContainingIgnoreCaseIgnoresCase() {
        repository.saveAndFlush(
                department(
                        DepartmentType.EMERGENCY,
                        "Floor 1",
                        "Emergency and urgent care department",
                        true
                )
        );

        List<Department> result =
                repository
                        .findByDescriptionContainingIgnoreCase(
                                "URGENT CARE"
                        );

        assertEquals(1, result.size());
        assertEquals(
                DepartmentType.EMERGENCY,
                result.getFirst()
                        .getDepartmentType()
        );
    }

    @Test
    void findAllByOrderByDepartmentTypeAscReturnsOrderedDepartments() {
        repository.save(
                department(
                        DepartmentType.EMERGENCY,
                        "Floor 1",
                        "Emergency department",
                        true
                )
        );

        repository.save(
                department(
                        DepartmentType.CARDIOLOGY,
                        "Floor 2",
                        "Cardiology department",
                        true
                )
        );

        repository.flush();

        List<Department> result =
                repository
                        .findAllByOrderByDepartmentTypeAsc();

        assertEquals(2, result.size());
        assertEquals(
                DepartmentType.CARDIOLOGY,
                result.get(0).getDepartmentType()
        );
        assertEquals(
                DepartmentType.EMERGENCY,
                result.get(1).getDepartmentType()
        );
    }

    @Test
    void findByIdWithWardsFetchesWardsCollection() {
        Department saved =
                repository.saveAndFlush(
                        department(
                                DepartmentType.CARDIOLOGY,
                                "Floor 2",
                                "Cardiology department",
                                true
                        )
                );

        Long id = saved.getId();

        entityManager.clear();

        Department result =
                repository.findByIdWithWards(id)
                        .orElseThrow();

        assertTrue(
                Hibernate.isInitialized(
                        result.getWards()
                )
        );
        assertTrue(result.getWards().isEmpty());
    }

    @Test
    void findAllWithWardsFetchesWardsCollections() {
        repository.saveAndFlush(
                department(
                        DepartmentType.EMERGENCY,
                        "Floor 1",
                        "Emergency department",
                        true
                )
        );

        entityManager.clear();

        List<Department> result =
                repository.findAllWithWards();

        assertEquals(1, result.size());
        assertTrue(
                Hibernate.isInitialized(
                        result.getFirst().getWards()
                )
        );
    }

    private Department department(
            DepartmentType departmentType,
            String location,
            String description,
            boolean active
    ) {
        return Department.builder()
                .departmentType(departmentType)
                .location(location)
                .phoneExtension("100")
                .description(description)
                .isActive(active)
                .build();
    }
}
