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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static com.hospital.gestion.api.receptionist.specification.ReceptionistSpecification.belongsToDepartment;
import static com.hospital.gestion.api.receptionist.specification.ReceptionistSpecification.deskContains;
import static com.hospital.gestion.api.receptionist.specification.ReceptionistSpecification.hasActiveStatus;
import static com.hospital.gestion.api.receptionist.specification.ReceptionistSpecification.hasShiftType;
import static com.hospital.gestion.api.receptionist.specification.ReceptionistSpecification.textContains;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceptionistService {



    private final ReceptionistRepository receptionistRepository;

    private final ReceptionistMapper receptionistMapper;
    private final HospitalEntityHelper helper;


    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "user.id",
            "user.firstName",
            "user.lastName",
            "user.email",
            "user.documentId",
            "user.isActive",
            "department.id",
            "department.departmentType",
            "deskNumber",
            "shiftType",
            "createdAt",
            "updatedAt"
    );
// ============================================================
// CREATE
// ============================================================

    @Transactional
    public ReceptionistResponseDTO createReceptionist(
            ReceptionistRequestDTO request
    ) {
        log.info(
                "Creating receptionist for user: {} "
                        + "in department: {}",
                request.userId(),
                request.departmentId()
        );

        User user = helper.findUserByIdForUpdate(
                request.userId()
        );

        if (user.getRole() != Role.RECEPTIONIST) {
            throw new ConflictException(
                    "User must have RECEPTIONIST role"
            );
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ConflictException(
                    "Inactive user cannot be registered "
                            + "as a receptionist"
            );
        }

        if (receptionistRepository
                .existsByUser_Id(user.getId())) {
            throw new ConflictException(
                    "A receptionist profile already exists "
                            + "for user: "
                            + user.getId()
            );
        }

        Department department =
                helper.findDepartmentById(
                        request.departmentId()
                );

        String normalizedDeskNumber =
                helper.normalizeRequiredText(
                        request.deskNumber(),
                        "Desk number"
                );

        Receptionist receptionist =
                receptionistMapper.toEntity(
                        request,
                        user,
                        department
                );

        receptionist.setDeskNumber(
                normalizedDeskNumber
        );

        Receptionist savedReceptionist =
                receptionistRepository.save(
                        receptionist
                );

        log.info(
                "Receptionist created successfully with id: {}",
                savedReceptionist.getId()
        );

        return receptionistMapper.toResponseDTO(
                savedReceptionist
        );
    }

    // ============================================================
    // GET ALL
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getAllReceptionists() {
        log.info("Fetching all receptionists");

        return receptionistMapper.toResponseDTOList(
                receptionistRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<ReceptionistResponseDTO>
    getAllReceptionists(Pageable pageable) {
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return receptionistRepository
                .findAll(pageable)
                .map(receptionistMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getAllReceptionistsOrdered() {
        return receptionistMapper.toResponseDTOList(
                receptionistRepository
                        .findAllByOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getActiveReceptionistsOrdered() {
        return receptionistMapper.toResponseDTOList(
                receptionistRepository
                        .findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================
    @Transactional(readOnly = true)
    public ReceptionistResponseDTO getReceptionistById(
            Long id
    ) {
        log.info(
                "Fetching receptionist by id: {}",
                id
        );

        return receptionistMapper.toResponseDTO(
                helper.findReceptionistById(id)
        );
    }


    // ============================================================
    // GET BY USER
    // ============================================================

    @Transactional(readOnly = true)
    public ReceptionistResponseDTO getReceptionistByUserId(
            Long userId
    ) {
        helper.validateId(userId, "User");

        Receptionist receptionist =
                receptionistRepository
                        .findByUser_Id(userId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Receptionist not found for user: "
                                                + userId
                                )
                        );

        return receptionistMapper.toResponseDTO(
                receptionist
        );
    }

    @Transactional(readOnly = true)
    public ReceptionistResponseDTO getReceptionistByEmail(
            String email
    ) {
        String normalizedEmail =
                helper.normalizeRequiredText(email, "Email");

        Receptionist receptionist =
                receptionistRepository
                        .findByUser_EmailIgnoreCase(
                                normalizedEmail
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Receptionist not found with email: "
                                                + normalizedEmail
                                )
                        );

        return receptionistMapper.toResponseDTO(
                receptionist
        );
    }

    @Transactional(readOnly = true)
    public ReceptionistResponseDTO
    getReceptionistByDocumentId(
            String documentId
    ) {
        String normalizedDocument =
                helper.normalizeRequiredText(
                        documentId,
                        "Document ID"
                );

        Receptionist receptionist =
                receptionistRepository
                        .findByUser_DocumentIdIgnoreCase(
                                normalizedDocument
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Receptionist not found with document ID: "
                                                + normalizedDocument
                                )
                        );

        return receptionistMapper.toResponseDTO(
                receptionist
        );
    }

    // ============================================================
    // BY DEPARTMENT
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getReceptionistsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return receptionistMapper.toResponseDTOList(
                receptionistRepository.findByDepartment_Id(
                        departmentId
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<ReceptionistResponseDTO>
    getReceptionistsByDepartment(
            Long departmentId,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return receptionistRepository
                .findByDepartment_Id(
                        departmentId,
                        pageable
                )
                .map(receptionistMapper::toResponseDTO);
    }

    // ============================================================
    // BY SHIFT
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getReceptionistsByShift(
            ShiftType shiftType
    ) {
        helper.validateShiftType(shiftType);

        return receptionistMapper.toResponseDTOList(
                receptionistRepository.findByShiftType(
                        shiftType
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<ReceptionistResponseDTO>
    getReceptionistsByShift(
            ShiftType shiftType,
            Pageable pageable
    ) {
        helper.validateShiftType(shiftType);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return receptionistRepository
                .findByShiftType(
                        shiftType,
                        pageable
                )
                .map(receptionistMapper::toResponseDTO);
    }

    // ============================================================
    // BY DEPARTMENT AND SHIFT
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getReceptionistsByDepartmentAndShift(
            Long departmentId,
            ShiftType shiftType
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateShiftType(shiftType);

        return receptionistMapper.toResponseDTOList(
                receptionistRepository
                        .findByDepartment_IdAndShiftType(
                                departmentId,
                                shiftType
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<ReceptionistResponseDTO>
    getReceptionistsByDepartmentAndShift(
            Long departmentId,
            ShiftType shiftType,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateShiftType(shiftType);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return receptionistRepository
                .findByDepartment_IdAndShiftType(
                        departmentId,
                        shiftType,
                        pageable
                )
                .map(receptionistMapper::toResponseDTO);
    }

    // ============================================================
    // ACTIVE STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getReceptionistsByActiveStatus(
            Boolean isActive
    ) {
        helper.validateBoolean(isActive, "Active status");

        return receptionistMapper.toResponseDTOList(
                receptionistRepository
                        .findByUser_IsActive(isActive)
        );
    }

    @Transactional(readOnly = true)
    public Page<ReceptionistResponseDTO>
    getReceptionistsByActiveStatus(
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateBoolean(isActive, "Active status");
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return receptionistRepository
                .findByUser_IsActive(
                        isActive,
                        pageable
                )
                .map(receptionistMapper::toResponseDTO);
    }

    // ============================================================
    // DESK
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getReceptionistsByDeskNumber(
            String deskNumber
    ) {
        String normalizedDeskNumber =
                helper.normalizeRequiredText(
                        deskNumber,
                        "Desk number"
                );

        return receptionistMapper.toResponseDTOList(
                receptionistRepository
                        .findByDeskNumberIgnoreCase(
                                normalizedDeskNumber
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    getReceptionistsByDepartmentAndDeskNumber(
            Long departmentId,
            String deskNumber
    ) {
        helper.validateDepartmentExist(departmentId);

        String normalizedDeskNumber =
                helper.normalizeRequiredText(
                        deskNumber,
                        "Desk number"
                );

        return receptionistMapper.toResponseDTOList(
                receptionistRepository
                        .findByDepartment_IdAndDeskNumberIgnoreCase(
                                departmentId,
                                normalizedDeskNumber
                        )
        );
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @Transactional(readOnly = true)
    public List<ReceptionistResponseDTO>
    searchReceptionists(String text) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        return receptionistMapper.toResponseDTOList(
                receptionistRepository.searchReceptionists(
                        normalizedText
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<ReceptionistResponseDTO>
    searchReceptionists(
            String text,
            Pageable pageable
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return receptionistRepository
                .searchReceptionists(
                        normalizedText,
                        pageable
                )
                .map(receptionistMapper::toResponseDTO);
    }

    // ============================================================
// UPDATE
// ============================================================

    @Transactional
    public ReceptionistResponseDTO updateReceptionist(
            Long id,
            ReceptionistUpdateDTO request
    ) {
        log.info(
                "Updating receptionist with id: {}",
                id
        );

        Receptionist receptionist =
                helper.findReceptionistByIdForUpdate(id);

        Department department =
                helper.findDepartmentById(
                        request.departmentId()
                );

        receptionistMapper.updateEntity(
                receptionist,
                request,
                department
        );

        receptionist.setDeskNumber(
                helper.normalizeRequiredText(
                        request.deskNumber(),
                        "Desk number"
                )
        );

        Receptionist updatedReceptionist =
                receptionistRepository.saveAndFlush(
                        receptionist
                );

        log.info(
                "Receptionist updated successfully with id: {}",
                updatedReceptionist.getId()
        );

        return receptionistMapper.toResponseDTO(
                updatedReceptionist
        );
    }
    // ============================================================
// DELETE
// ============================================================

    @Transactional
    public void deleteReceptionist(Long id) {
        log.info(
                "Deleting receptionist with id: {}",
                id
        );

        Receptionist receptionist =
                helper.findReceptionistByIdForUpdate(id);

        receptionistRepository.delete(receptionist);

        log.info(
                "Receptionist deleted successfully with id: {}",
                id
        );
    }

    // ============================================================
    // EXISTS
    // ============================================================

    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        helper.validateId(userId, "User");

        return receptionistRepository
                .existsByUser_Id(userId);
    }

    // ============================================================
    // COUNT
    // ============================================================

    @Transactional(readOnly = true)
    public long countAllReceptionists() {
        return receptionistRepository.count();
    }

    @Transactional(readOnly = true)
    public long countReceptionistsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return receptionistRepository
                .countByDepartment_Id(departmentId);
    }

    @Transactional(readOnly = true)
    public long countReceptionistsByShift(
            ShiftType shiftType
    ) {
        helper.validateShiftType(shiftType);

        return receptionistRepository
                .countByShiftType(shiftType);
    }

    @Transactional(readOnly = true)
    public long countReceptionistsByDepartmentAndShift(
            Long departmentId,
            ShiftType shiftType
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateShiftType(shiftType);

        return receptionistRepository
                .countByDepartment_IdAndShiftType(
                        departmentId,
                        shiftType
                );
    }

    @Transactional(readOnly = true)
    public long countReceptionistsByActiveStatus(
            Boolean isActive
    ) {
        helper.validateBoolean(isActive, "Active status");

        return receptionistRepository
                .countByUser_IsActive(isActive);
    }



    @Transactional(readOnly = true)
    public Page<ReceptionistResponseDTO> getReceptionists(
            String text,
            Long departmentId,
            ShiftType shiftType,
            Boolean isActive,
            String deskNumber,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (departmentId != null) {
            helper.validateDepartmentExist(departmentId);
        }

        if (shiftType != null) {
            helper.validateShiftType(shiftType);
        }

        String normalizedText =
                normalizeOptionalFilter(text);

        String normalizedDeskNumber =
                normalizeOptionalFilter(deskNumber);

        log.info(
                "Fetching receptionists with filters: "
                        + "text={}, departmentId={}, shiftType={}, "
                        + "active={}, deskNumber={}",
                normalizedText,
                departmentId,
                shiftType,
                isActive,
                normalizedDeskNumber
        );

        Specification<Receptionist> specification =
                textContains(normalizedText)
                        .and(belongsToDepartment(
                                departmentId
                        ))
                        .and(hasShiftType(shiftType))
                        .and(hasActiveStatus(isActive))
                        .and(deskContains(
                                normalizedDeskNumber
                        ));

        return receptionistRepository
                .findAll(specification, pageable)
                .map(receptionistMapper::toResponseDTO);
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