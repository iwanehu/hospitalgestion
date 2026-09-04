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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.jpa.domain.Specification;

import static com.hospital.gestion.api.department.specification.DepartmentSpecification.descriptionContains;
import static com.hospital.gestion.api.department.specification.DepartmentSpecification.hasActiveStatus;
import static com.hospital.gestion.api.department.specification.DepartmentSpecification.hasDepartmentType;
import static com.hospital.gestion.api.department.specification.DepartmentSpecification.locationContains;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentService {


    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    private final HospitalEntityHelper helper;


    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "departmentType",
            "location",
            "phoneExtension",
            "isActive",
            "createdAt",
            "updatedAt"
    );


    //create
    //================


    @Transactional
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO request) {
        log.info("Creating department with type : {}", request.departmentType());

        if(departmentRepository.existsByDepartmentType(request.departmentType())){
            throw new ConflictException("Department type already exists: "+ request.departmentType());
        }



        if (departmentRepository.existsByLocation(request.location())) {
            throw new ConflictException("Department location already in use: "+ request.location());
        }

        Department department = departmentMapper.toEntity(request);

        Department savedDepartment = departmentRepository.save(department);

        log.info("Department created successfully with id : {}", savedDepartment.getId());
        return departmentMapper.toResponseDTO(savedDepartment);
    }


    //=====================
    //Read
    //===========

    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        log.info("Fetching all departments");

        return departmentMapper.toResponseDTOList(departmentRepository.findAll());


    }




    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        log.debug("Fetching department with ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Department id cannot be null");
        }
        Department department = helper.findDepartmentById(id);
        return departmentMapper.toResponseDTO(department);
    }

    @Transactional(readOnly = true)
    public  DepartmentResponseDTO getDepartmentByType(DepartmentType departmentType) {
        log.info("Fetching department by type : {}", departmentType);

        if (departmentType == null) {
            throw new IllegalArgumentException("Department type cannot be null");
        }

        Department department = departmentRepository.findByDepartmentType(departmentType).orElseThrow(()-> new ResourceNotFoundException("Department not found with type : " + departmentType));

        return departmentMapper.toResponseDTO(department);
    }


    @Transactional(readOnly = true)
    public DepartmentResponseDTO getActiveDepartmentActiveByType(DepartmentType departmentType) {
        log.info("Fetching active department ordered by type");
        if (departmentType == null) {
            throw new IllegalArgumentException(
                    "Department type cannot be null"
            );
        }

        Department department= departmentRepository.findByDepartmentTypeAndActive(departmentType).orElseThrow(()-> new ResourceNotFoundException("Department not found with type : " + departmentType));
        return departmentMapper.toResponseDTO(department);
    }





   @Transactional(readOnly = true)
   public List<DepartmentResponseDTO> getDepartmentByActiveStatus(Boolean isActive) {
        log.info("Fetching department by active status : {}", isActive);

        if (isActive == null) {
            throw new IllegalArgumentException("Active status cannot be null");
        }
        return departmentMapper.toResponseDTOList(departmentRepository.findByIsActive(isActive));
   }



    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getActiveDepartmentsOrdered() {
        log.info(
                "Fetching active departments ordered by type"
        );

        return departmentMapper.toResponseDTOList(
                departmentRepository
                        .findByIsActiveTrueOrderByDepartmentTypeAsc()
        );
    }


    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartmentsOrderedByType(){
        log.info("Fetching all departments ordered by type");
        return departmentMapper.toResponseDTOList(departmentRepository.findAllByOrderByDepartmentTypeAsc());
    }


    //=======
    //Location
    //=========


    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentByLocation(
            String location
    ) {
        log.info("Fetching department by location: {}", location);

        helper.validateRequiredText(location, "Location");

        String normalizedLocation = location.trim();

        Department department = departmentRepository
                .findByLocation(normalizedLocation)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with location: "
                                        + normalizedLocation
                        )
                );

        return departmentMapper.toResponseDTO(department);
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getDepartmentsByLocation(
            String location
    ) {
        log.info(
                "Searching departments by location: {}",
                location
        );

        helper.validateRequiredText(location, "Location");

        return departmentMapper.toResponseDTOList(
                departmentRepository
                        .findByLocationContainingIgnoreCase(
                                location.trim()
                        )
        );
    }


    //===========
    //Description
    //========
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getDepartmentsByDescription(
            String description
    ) {
        log.info(
                "Searching departments by description: {}",
                description
        );

        helper.validateRequiredText(description, "Description");

        return departmentMapper.toResponseDTOList(
                departmentRepository
                        .findByDescriptionContainingIgnoreCase(
                                description.trim()
                        )
        );
    }


    //================
    //update
    //=============================

    @Transactional
    public DepartmentResponseDTO updateDepartmentById(
            Long id,
            DepartmentUpdateDTO request
    ) {
        log.info("Updating department by id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException(
                    "Department id cannot be null"
            );
        }

        Department department = helper.findDepartmentById(id);

        validateUpdatedLocation(department, request);

        departmentMapper.updateEntity(department, request);

        Department updatedDepartment =
                departmentRepository.saveAndFlush(department);

        log.info(
                "Department updated successfully with id: {}",
                updatedDepartment.getId()
        );

        return departmentMapper.toResponseDTO(updatedDepartment);
    }



        //==============
        // Delete
        //===========


    @Transactional
    public void deleteDepartment(Long id) {
        log.info("Deleting department by id : {}", id);

        Department department= helper.findDepartmentById(id);
        departmentRepository.delete(department);
        log.info("Department deleted successfully with id: {}", id);

    }


    //=======================
    //Activate/Deactivate
    //=======================



    @Transactional
    public void activateDepartment(Long id) {
        log.info("Activating department by id : {}", id);

        Department department = helper.findDepartmentById(id);

        if (Boolean.TRUE.equals(department.getIsActive())) {
            throw new ConflictException(
                    "Department is already active with id: " + id
            );
        }

        department.setIsActive(true);

        log.info(
                "Department activated successfully with id: {}",
                id
        );
    }


    @Transactional
    public void deactivateDepartment(Long id) {
        log.info("Deactivating department by id : {}", id);

        Department department = helper.findDepartmentById(id);

        if (Boolean.FALSE.equals(department.getIsActive())) {
            throw new ConflictException(
                    "Department is already inactive with id: " + id
            );
        }

        department.setIsActive(false);

        log.info(
                "Department deactivated successfully with id: {}",
                id
        );
    }




    //=======
    //Exists
    //======================

    @Transactional(readOnly = true)
    public boolean existsByDepartmentType(
            DepartmentType departmentType
    ) {
        if (departmentType == null) {
            throw new IllegalArgumentException(
                    "Department type cannot be null"
            );
        }

        return departmentRepository.existsByDepartmentType(
                departmentType
        );
    }


    @Transactional(readOnly = true)
    public boolean existsActiveByDepartmentType(
            DepartmentType departmentType
    ) {
        if (departmentType == null) {
            throw new IllegalArgumentException(
                    "Department type cannot be null"
            );
        }

        return departmentRepository
                .findByDepartmentTypeAndActive(departmentType)
                .isPresent();
    }



    //Count
    @Transactional(readOnly = true)
    public long countDepartmentsByActiveStatus(
            Boolean isActive
    ) {
        log.info(
                "Counting departments with active status: {}",
                isActive
        );

        if (isActive == null) {
            throw new IllegalArgumentException(
                    "Active status cannot be null"
            );
        }

        return departmentRepository.countByIsActive(isActive);
    }




    @Transactional(readOnly = true)
    public long countActiveDepartments() {
        log.info("Counting active departments");

        return departmentRepository.countByIsActive(true);
    }

    //===========
    //Ward
    //=============

    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentWithWardsById(
            Long id
    ) {
        log.info(
                "Fetching department with wards by id: {}",
                id
        );

        if (id == null) {
            throw new IllegalArgumentException(
                    "Department id cannot be null"
            );
        }

        Department department = helper.findDepartmentById(id);

        return departmentMapper.toResponseDTO(department);
    }


    public List<DepartmentResponseDTO> getAllDepartmentsWithWards() {
        log.info("Fetching all departments with wards");

        return departmentMapper.toResponseDTOList(
                departmentRepository.findAllWithWards()
        );
    }




    // ========================================
    // PRIVATE HELPERS
    // ========================================





    private void validateUpdatedLocation(
            Department department,
            DepartmentUpdateDTO request
    ) {
        if (request.location() == null) {
            return;
        }

        if (request.location().isBlank()) {
            throw new IllegalArgumentException(
                    "Location cannot be empty"
            );
        }

        String newLocation = request.location().trim();
        String currentLocation = department.getLocation();

        boolean locationChanged =
                currentLocation == null
                        || !newLocation.equalsIgnoreCase(
                        currentLocation.trim()
                );

        if (locationChanged
                && departmentRepository.existsByLocation(newLocation)) {
            throw new ConflictException(
                    "Location already in use: " + newLocation
            );
        }
    }


    @Transactional(readOnly = true)
    public Page<DepartmentResponseDTO> getDepartments(
            DepartmentType departmentType,
            Boolean isActive,
            String location,
            String description,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        String normalizedLocation =
                normalizeOptionalFilter(location);

        String normalizedDescription =
                normalizeOptionalFilter(description);

        log.info(
                "Fetching departments with filters: "
                        + "type={}, active={}, location={}, "
                        + "description={}",
                departmentType,
                isActive,
                normalizedLocation,
                normalizedDescription
        );

        Specification<Department> specification =
                hasDepartmentType(departmentType)
                        .and(hasActiveStatus(isActive))
                        .and(locationContains(
                                normalizedLocation
                        ))
                        .and(descriptionContains(
                                normalizedDescription
                        ));

        return departmentRepository
                .findAll(specification, pageable)
                .map(departmentMapper::toResponseDTO);
    }

    private String normalizeOptionalFilter(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

}


