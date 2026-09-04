package com.hospital.gestion.api.ward.service;

import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.ward.dto.WardRequestDTO;
import com.hospital.gestion.api.ward.dto.WardResponseDTO;
import com.hospital.gestion.api.ward.dto.WardUpdateDTO;
import com.hospital.gestion.api.ward.entity.Ward;
import com.hospital.gestion.api.ward.mapper.WardMapper;
import com.hospital.gestion.api.ward.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;



import static com.hospital.gestion.api.ward.specification.WardSpecification.belongsToDepartment;
import static com.hospital.gestion.api.ward.specification.WardSpecification.descriptionContains;
import static com.hospital.gestion.api.ward.specification.WardSpecification.hasActiveStatus;
import static com.hospital.gestion.api.ward.specification.WardSpecification.nameContains;


@Service
@RequiredArgsConstructor
@Slf4j
public class WardService {




    private final WardRepository wardRepository;
    private final WardMapper wardMapper;
    private final HospitalEntityHelper helper;


    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "name",
            "description",
            "isActive",
            "department.id",
            "createdAt",
            "updatedAt"
    );










    //======0
    //CREATE
    //=========0


    @Transactional
    public WardResponseDTO createWard(
            WardRequestDTO request
    ) {
        log.info(
                "Creating ward with name: {} in department: {}",
                request.name(),
                request.departmentId()
        );

        Department department = helper.findDepartmentById(
                request.departmentId()
        );

        helper.validateWardName(request.name());

        String normalizedName = request.name().trim();

        if (wardRepository
                .existsByNameIgnoreCaseAndDepartment_Id(
                        normalizedName,
                        request.departmentId()
                )) {
            throw new ConflictException(
                    "Ward name already exists in department: "
                            + normalizedName
            );
        }

        Ward ward = wardMapper.toEntity(request, department);

        ward.setName(normalizedName);
        ward.setDescription(
                helper.normalizeNullableText(request.description())
        );

        Ward savedWard = wardRepository.save(ward);

        log.info(
                "Ward created successfully with id: {}",
                savedWard.getId()
        );

        return wardMapper.toResponseDTO(savedWard);
    }


    //========
    //GET ALL
    //=======
    @Transactional(readOnly = true)
    public List<WardResponseDTO> getAllWards() {
        log.info("Fetching all wards");

        return wardMapper.toResponseDTOList(wardRepository.findAll());
    }

    @Transactional(readOnly = true)
    public Page<WardResponseDTO> getAllWards(
            Pageable pageable
    ) {
        log.info(
                "Fetching wards with pagination: {}",
                pageable
        );

        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return wardRepository.findAll(pageable)
                .map(wardMapper::toResponseDTO);
    }


    //====
    //Get By Id
    //=========
    @Transactional(readOnly = true)
    public WardResponseDTO getWardById(Long id) {
        log.info("Fetching Ward with id: {}", id);

        Ward ward =  helper.findWardById(id);

        return wardMapper.toResponseDTO(ward);
    }


    //=======
    //Get By Active Status
    //================

    @Transactional(readOnly = true)
    public List<WardResponseDTO> getWardsByActiveStatus(Boolean active) {
        log.info("Fetching Wards by active status: {}", active);

        helper.validateActiveStatus(active);

        return wardMapper.toResponseDTOList(wardRepository.findByIsActive(active));
    }


    @Transactional(readOnly = true)
    public Page<WardResponseDTO> getWardsByActiveStatus(
            Boolean isActive,
            Pageable pageable
    ) {
        log.info(
                "Fetching wards by active status: {} with pagination",
                isActive
        );

        helper.validateActiveStatus(isActive);
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return wardRepository
                .findByIsActive(isActive, pageable)
                .map(wardMapper::toResponseDTO);
    }



    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @Transactional(readOnly = true)
    public List<WardResponseDTO> getWardsByDepartment(
            Long departmentId
    ) {
        log.info(
                "Fetching wards by department id: {}",
                departmentId
        );

        helper.validateDepartmentExist(departmentId);

        return wardMapper.toResponseDTOList(
                wardRepository.findByDepartment_Id(departmentId)
        );
    }

    @Transactional(readOnly = true)
    public Page<WardResponseDTO> getWardsByDepartment(
            Long departmentId,
            Pageable pageable
    ) {
        log.info(
                "Fetching wards by department id: {} with pagination",
                departmentId
        );

        helper.validateDepartmentExist(departmentId);
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return wardRepository
                .findByDepartment_Id(departmentId, pageable)
                .map(wardMapper::toResponseDTO);
    }

    // ========================================
    // GET BY DEPARTMENT AND ACTIVE STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<WardResponseDTO>
    getWardsByDepartmentAndActiveStatus(
            Long departmentId,
            Boolean isActive
    ) {
        log.info(
                "Fetching wards by department: {} and status: {}",
                departmentId,
                isActive
        );

        helper.validateDepartmentExist(departmentId);
        helper.validateActiveStatus(isActive);

        return wardMapper.toResponseDTOList(
                wardRepository.findByDepartment_IdAndIsActive(
                        departmentId,
                        isActive
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<WardResponseDTO>
    getWardsByDepartmentAndActiveStatus(
            Long departmentId,
            Boolean isActive,
            Pageable pageable
    ) {
        log.info(
                "Fetching wards by department: {}, status: {} "
                        + "with pagination",
                departmentId,
                isActive
        );

        helper.validateDepartmentExist(departmentId);
        helper.validateActiveStatus(isActive);
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return wardRepository
                .findByDepartment_IdAndIsActive(
                        departmentId,
                        isActive,
                        pageable
                )
                .map(wardMapper::toResponseDTO);
    }


    // ========================================
    // GET BY EXACT NAME AND DEPARTMENT
    // ========================================


    @Transactional(readOnly = true)
    public WardResponseDTO  getWardByNameAndDepartment(
            String name,
            Long departmentId
    ) {
        log.info(
                "Fetching ward with name: {} in department: {}",
                name,
                departmentId
        );

        helper.validateWardName(name);
        helper.validateDepartmentExist(departmentId);

        String normalizedName = name.trim();

        Ward ward = wardRepository
                .findByNameIgnoreCaseAndDepartment_Id(
                        normalizedName,
                        departmentId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ward not found with name: "
                                        + normalizedName
                                        + " in department: "
                                        + departmentId
                        )
                );

        return wardMapper.toResponseDTO(ward);
    }



    // ========================================
    // SEARCH BY NAME
    // ========================================

    @Transactional(readOnly = true)
    public List<WardResponseDTO> searchWardsByName(
            String name
    ) {
        log.info("Searching wards by name: {}", name);

        helper.validateWardName(name);

        return wardMapper.toResponseDTOList(
                wardRepository.findByNameContainingIgnoreCase(
                        name.trim()
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<WardResponseDTO> searchWardsByName(
            String name,
            Pageable pageable
    ) {
        log.info(
                "Searching wards by name: {} with pagination",
                name
        );

        helper.validateWardName(name);
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        return wardRepository
                .findByNameContainingIgnoreCase(
                        name.trim(),
                        pageable
                )
                .map(wardMapper::toResponseDTO);
    }

    // ========================================
    // UPDATE
    // ========================================

    @Transactional
    public WardResponseDTO updateWardById(
            Long id,
            WardUpdateDTO request
    ) {
        log.info("Updating ward by id: {}", id);

        Ward ward = helper.findWardById(id);

        helper.validateWardName(request.name());

        String normalizedName = request.name().trim();

        validateWardNameForUpdate(ward, normalizedName);

        wardMapper.updateEntity(ward, request);

        ward.setName(normalizedName);
        ward.setDescription(
                helper.normalizeNullableText(request.description())
        );

        Ward updatedWard = wardRepository.save(ward);

        log.info(
                "Ward updated successfully with id: {}",
                updatedWard.getId()
        );

        return wardMapper.toResponseDTO(updatedWard);
    }

    private void validateWardNameForUpdate(
            Ward ward,
            String newName
    ) {
        String currentName = ward.getName();

        boolean nameChanged =
                currentName == null
                        || !newName.equalsIgnoreCase(
                        currentName.trim()
                );

        if (nameChanged
                && wardRepository
                .existsByNameIgnoreCaseAndDepartment_Id(
                        newName,
                        ward.getDepartment().getId()
                )) {
            throw new ConflictException(
                    "Ward name already exists in department: "
                            + newName
            );
        }
    }



    // ========================================
    // DELETE
    // ========================================

    @Transactional
    public void deleteWard(Long id) {
        log.info("Deleting ward by id: {}", id);

        Ward ward = helper.findWardById(id);

        if (ward.getRooms() != null
                && !ward.getRooms().isEmpty()) {
            throw new ConflictException(
                    "Ward cannot be deleted because it contains rooms"
            );
        }

        wardRepository.delete(ward);

        log.info(
                "Ward deleted successfully with id: {}",
                id
        );
    }


    // ========================================
    // ACTIVATE / DEACTIVATE
    // ========================================

    @Transactional
    public void activateWard(Long id) {
        log.info("Activating ward by id: {}", id);

        Ward ward = helper.findWardById(id);

        if (Boolean.TRUE.equals(ward.getIsActive())) {
            throw new ConflictException(
                    "Ward is already active with id: " + id
            );
        }

        ward.setIsActive(true);

        log.info(
                "Ward activated successfully with id: {}",
                id
        );
    }

    @Transactional
    public void deactivateWard(Long id) {
        log.info("Deactivating ward by id: {}", id);

        Ward ward = helper.findWardById(id);

        if (Boolean.FALSE.equals(ward.getIsActive())) {
            throw new ConflictException(
                    "Ward is already inactive with id: " + id
            );
        }

        ward.setIsActive(false);

        log.info(
                "Ward deactivated successfully with id: {}",
                id
        );
    }


    // ========================================
    // EXISTS
    // ========================================

    @Transactional(readOnly = true)
    public boolean existsByNameAndDepartment(
            String name,
            Long departmentId
    ) {
        log.info(
                "Checking ward name: {} in department: {}",
                name,
                departmentId
        );

        helper.validateWardName(name);
        helper.validateDepartmentExist(departmentId);

        return wardRepository
                .existsByNameIgnoreCaseAndDepartment_Id(
                        name.trim(),
                        departmentId
                );
    }


    // ========================================
    // COUNT
    // ========================================

    @Transactional(readOnly = true)
    public long countAllWards() {
        log.info("Counting all wards");

        return wardRepository.count();
    }

    @Transactional(readOnly = true)
    public long countWardsByActiveStatus(
            Boolean isActive
    ) {
        log.info(
                "Counting wards with active status: {}",
                isActive
        );

        helper.validateActiveStatus(isActive);

        return wardRepository.countByIsActive(isActive);
    }

    @Transactional(readOnly = true)
    public long countWardsByDepartment(
            Long departmentId
    ) {
        log.info(
                "Counting wards in department: {}",
                departmentId
        );

        helper.validateDepartmentExist(departmentId);

        return wardRepository.countByDepartment_Id(
                departmentId
        );
    }

    @Transactional(readOnly = true)
    public long countWardsByDepartmentAndActiveStatus(
            Long departmentId,
            Boolean isActive
    ) {
        log.info(
                "Counting wards in department: {} with status: {}",
                departmentId,
                isActive
        );

        helper.validateDepartmentExist(departmentId);
        helper.validateActiveStatus(isActive);

        return wardRepository
                .countByDepartment_IdAndIsActive(
                        departmentId,
                        isActive
                );
    }


    @Transactional(readOnly = true)
    public Page<WardResponseDTO> getWards(
            String name,
            String description,
            Boolean isActive,
            Long departmentId,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (departmentId != null) {
            helper.validateDepartmentExist(departmentId);
        }

        String normalizedName =
                normalizeOptionalFilter(name);

        String normalizedDescription =
                normalizeOptionalFilter(description);

        log.info(
                "Fetching wards with filters: "
                        + "name={}, description={}, "
                        + "active={}, departmentId={}",
                normalizedName,
                normalizedDescription,
                isActive,
                departmentId
        );

        Specification<Ward> specification =
                nameContains(normalizedName)
                        .and(descriptionContains(
                                normalizedDescription
                        ))
                        .and(hasActiveStatus(isActive))
                        .and(belongsToDepartment(
                                departmentId
                        ));

        return wardRepository
                .findAll(specification, pageable)
                .map(wardMapper::toResponseDTO);
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
