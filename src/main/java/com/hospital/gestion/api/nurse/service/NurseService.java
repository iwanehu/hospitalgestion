package com.hospital.gestion.api.nurse.service;

import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.ShiftType;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Set;

import static com.hospital.gestion.api.nurse.specification.NurseSpecification.belongsToDepartment;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.hasActiveStatus;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.hasChargeStatus;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.hasShiftType;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.hasSpecialty;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.hiredFrom;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.hiredTo;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.maximumExperience;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.minimumExperience;
import static com.hospital.gestion.api.nurse.specification.NurseSpecification.textContains;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NurseService {



    private final NurseRepository nurseRepository;

    private final NurseMapper nurseMapper;
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
            "licenseNumber",
            "specialty",
            "shiftType",
            "yearsOfExperience",
            "hireDate",
            "maxPatientsPerShift",
            "isChargeNurse",
            "vacationDaysAvailable",
            "createdAt",
            "updatedAt"
    );

    // ============================================================
// CREATE
// ============================================================

    @Transactional
    public NurseResponseDTO createNurse(
            NurseRequestDTO request
    ) {
        log.info(
                "Creating nurse for user: {} in department: {}",
                request.userId(),
                request.departmentId()
        );

        User user = helper.findUserByIdForUpdate(
                request.userId()
        );

        if (user.getRole() != Role.NURSE) {
            throw new ConflictException(
                    "User must have NURSE role"
            );
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ConflictException(
                    "Inactive user cannot be registered "
                            + "as a nurse"
            );
        }

        if (nurseRepository.existsByUser_Id(user.getId())) {
            throw new ConflictException(
                    "A nurse profile already exists for user: "
                            + user.getId()
            );
        }

        String normalizedLicense =
                helper.normalizeRequiredText(
                        request.licenseNumber(),
                        "License number"
                );

        if (nurseRepository
                .existsByLicenseNumberIgnoreCase(
                        normalizedLicense
                )) {
            throw new ConflictException(
                    "License number already exists: "
                            + normalizedLicense
            );
        }

        Department department =
                helper.findDepartmentById(
                        request.departmentId()
                );

        Nurse nurse = nurseMapper.toEntity(
                request,
                user,
                department
        );

        nurse.setLicenseNumber(normalizedLicense);

        nurse.setBiography(
                helper.normalizeNullableText(
                        request.biography()
                )
        );

        if (nurse.getSpecialty() == null) {
            nurse.setSpecialty(
                    NurseSpecialty.GENERAL
            );
        }

        if (nurse.getIsChargeNurse() == null) {
            nurse.setIsChargeNurse(false);
        }

        Nurse savedNurse =
                nurseRepository.save(nurse);

        log.info(
                "Nurse created successfully with id: {}",
                savedNurse.getId()
        );

        return nurseMapper.toResponseDTO(savedNurse);
    }

    // ============================================================
    // GET ALL
    // ============================================================

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> getAllNurses() {
        log.info("Fetching all nurses");

        return nurseMapper.toResponseDTOList(
                nurseRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<NurseResponseDTO> getAllNurses(
            Pageable pageable
    ) {
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        log.info(
                "Fetching all nurses with pagination: {}",
                pageable
        );

        return nurseRepository.findAll(pageable)
                .map(nurseMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> getAllNursesOrdered() {
        log.info("Fetching all nurses ordered by name");

        return nurseMapper.toResponseDTOList(
                nurseRepository
                        .findAllByOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> getActiveNursesOrdered() {
        log.info("Fetching active nurses ordered by name");

        return nurseMapper.toResponseDTOList(
                nurseRepository
                        .findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Transactional(readOnly = true)
    public NurseResponseDTO getNurseById(Long id) {
        log.info("Fetching nurse by id: {}", id);

        return nurseMapper.toResponseDTO(
                helper.findNurseById(id)
        );
    }

    // ============================================================
    // GET BY USER
    // ============================================================

    @Transactional(readOnly = true)
    public NurseResponseDTO getNurseByUserId(
            Long userId
    ) {
        helper.validateId(userId, "User");

        log.info(
                "Fetching nurse by user id: {}",
                userId
        );

        Nurse nurse = nurseRepository
                .findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Nurse not found for user: "
                                        + userId
                        )
                );

        return nurseMapper.toResponseDTO(nurse);
    }

    @Transactional(readOnly = true)
    public NurseResponseDTO getNurseByEmail(
            String email
    ) {
        String normalizedEmail =
                helper.normalizeRequiredText(email, "Email");

        log.info(
                "Fetching nurse by email: {}",
                normalizedEmail
        );

        Nurse nurse = nurseRepository
                .findByUser_EmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Nurse not found with email: "
                                        + normalizedEmail
                        )
                );

        return nurseMapper.toResponseDTO(nurse);
    }

    @Transactional(readOnly = true)
    public NurseResponseDTO getNurseByDocumentId(
            String documentId
    ) {
        String normalizedDocument =
                helper.normalizeRequiredText(
                        documentId,
                        "Document ID"
                );

        log.info(
                "Fetching nurse by document ID: {}",
                normalizedDocument
        );

        Nurse nurse = nurseRepository
                .findByUser_DocumentIdIgnoreCase(
                        normalizedDocument
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Nurse not found with document ID: "
                                        + normalizedDocument
                        )
                );

        return nurseMapper.toResponseDTO(nurse);
    }

    // ============================================================
    // GET BY LICENSE
    // ============================================================

    @Transactional(readOnly = true)
    public NurseResponseDTO getNurseByLicense(
            String licenseNumber
    ) {
        String normalizedLicense =
                helper.normalizeRequiredText(
                        licenseNumber,
                        "License number"
                );

        log.info(
                "Fetching nurse by license: {}",
                normalizedLicense
        );

        Nurse nurse = nurseRepository
                .findByLicenseNumberIgnoreCase(
                        normalizedLicense
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Nurse not found with license: "
                                        + normalizedLicense
                        )
                );

        return nurseMapper.toResponseDTO(nurse);
    }

    // ============================================================
    // BY DEPARTMENT
    // ============================================================

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> getNursesByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return nurseMapper.toResponseDTOList(
                nurseRepository.findByDepartment_Id(
                        departmentId
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<NurseResponseDTO> getNursesByDepartment(
            Long departmentId,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return nurseRepository
                .findByDepartment_Id(
                        departmentId,
                        pageable
                )
                .map(nurseMapper::toResponseDTO);
    }

    // ============================================================
    // BY SPECIALTY
    // ============================================================

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> getNursesBySpecialty(
            NurseSpecialty specialty
    ) {
        helper.validateSpecialty(specialty);

        return nurseMapper.toResponseDTOList(
                nurseRepository.findBySpecialty(specialty)
        );
    }

    @Transactional(readOnly = true)
    public Page<NurseResponseDTO> getNursesBySpecialty(
            NurseSpecialty specialty,
            Pageable pageable
    ) {
        helper.validateSpecialty(specialty);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return nurseRepository
                .findBySpecialty(specialty, pageable)
                .map(nurseMapper::toResponseDTO);
    }

    // ============================================================
    // BY SHIFT
    // ============================================================

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> getNursesByShift(
            ShiftType shiftType
    ) {
        helper.validateShiftType(shiftType);

        return nurseMapper.toResponseDTOList(
                nurseRepository.findByShiftType(shiftType)
        );
    }

    @Transactional(readOnly = true)
    public Page<NurseResponseDTO> getNursesByShift(
            ShiftType shiftType,
            Pageable pageable
    ) {
        helper.validateShiftType(shiftType);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return nurseRepository
                .findByShiftType(shiftType, pageable)
                .map(nurseMapper::toResponseDTO);
    }

    // ============================================================
    // BY ACTIVE STATUS
    // ============================================================

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> getNursesByActiveStatus(
            Boolean isActive
    ) {
        helper.validateBoolean(isActive, "Active status");

        return nurseMapper.toResponseDTOList(
                nurseRepository.findByUser_IsActive(isActive)
        );
    }

    @Transactional(readOnly = true)
    public Page<NurseResponseDTO> getNursesByActiveStatus(
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateBoolean(isActive, "Active status");
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return nurseRepository
                .findByUser_IsActive(isActive, pageable)
                .map(nurseMapper::toResponseDTO);
    }

    // ============================================================
    // CHARGE NURSES
    // ============================================================

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> getChargeNurses(
            Boolean isChargeNurse
    ) {
        helper.validateBoolean(
                isChargeNurse,
                "Charge nurse status"
        );

        return nurseMapper.toResponseDTOList(
                nurseRepository.findByIsChargeNurse(
                        isChargeNurse
                )
        );
    }

    // ============================================================
    // COMBINED FILTERS
    // ============================================================

    @Transactional(readOnly = true)
    public List<NurseResponseDTO>
    getNursesByDepartmentAndSpecialty(
            Long departmentId,
            NurseSpecialty specialty
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateSpecialty(specialty);

        return nurseMapper.toResponseDTOList(
                nurseRepository
                        .findByDepartment_IdAndSpecialty(
                                departmentId,
                                specialty
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<NurseResponseDTO>
    getNursesByDepartmentAndShift(
            Long departmentId,
            ShiftType shiftType
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateShiftType(shiftType);

        return nurseMapper.toResponseDTOList(
                nurseRepository
                        .findByDepartment_IdAndShiftType(
                                departmentId,
                                shiftType
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<NurseResponseDTO>
    getNursesBySpecialtyAndShift(
            NurseSpecialty specialty,
            ShiftType shiftType
    ) {
        helper.validateSpecialty(specialty);
        helper.validateShiftType(shiftType);

        return nurseMapper.toResponseDTOList(
                nurseRepository
                        .findBySpecialtyAndShiftType(
                                specialty,
                                shiftType
                        )
        );
    }

    @Transactional(readOnly = true)
    public List<NurseResponseDTO>
    getNursesBySpecialtyAndActiveStatus(
            NurseSpecialty specialty,
            Boolean isActive
    ) {
        helper.validateSpecialty(specialty);
        helper.validateBoolean(isActive, "Active status");

        return nurseMapper.toResponseDTOList(
                nurseRepository
                        .findBySpecialtyAndUser_IsActive(
                                specialty,
                                isActive
                        )
        );
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @Transactional(readOnly = true)
    public List<NurseResponseDTO> searchNurses(
            String text
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        log.info(
                "Searching nurses with text: {}",
                normalizedText
        );

        return nurseMapper.toResponseDTOList(
                nurseRepository.searchNurses(
                        normalizedText
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<NurseResponseDTO> searchNurses(
            String text,
            Pageable pageable
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return nurseRepository
                .searchNurses(normalizedText, pageable)
                .map(nurseMapper::toResponseDTO);
    }

    // ============================================================
// UPDATE
// ============================================================

    @Transactional
    public NurseResponseDTO updateNurse(
            Long id,
            NurseUpdateDTO request
    ) {
        log.info(
                "Updating nurse with id: {}",
                id
        );

        Nurse nurse =
                helper.findNurseByIdForUpdate(id);

        Department department = null;

        if (request.departmentId() != null) {
            department = helper.findDepartmentById(
                    request.departmentId()
            );
        }

        nurseMapper.updateEntity(
                nurse,
                request,
                department
        );

        nurse.setBiography(
                helper.normalizeNullableText(
                        request.biography()
                )
        );

        Nurse updatedNurse =
                nurseRepository.saveAndFlush(nurse);

        log.info(
                "Nurse updated successfully with id: {}",
                updatedNurse.getId()
        );

        return nurseMapper.toResponseDTO(updatedNurse);
    }
// ============================================================
// DELETE
// ============================================================

    @Transactional
    public void deleteNurse(Long id) {
        log.info(
                "Deleting nurse with id: {}",
                id
        );

        Nurse nurse =
                helper.findNurseByIdForUpdate(id);

        nurseRepository.delete(nurse);

        log.info(
                "Nurse deleted successfully with id: {}",
                id
        );
    }
    // ============================================================
    // EXISTS
    // ============================================================

    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        helper.validateId(userId, "User");

        return nurseRepository.existsByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public boolean existsByLicense(
            String licenseNumber
    ) {
        String normalizedLicense =
                helper.normalizeRequiredText(
                        licenseNumber,
                        "License number"
                );

        return nurseRepository
                .existsByLicenseNumberIgnoreCase(
                        normalizedLicense
                );
    }

    // ============================================================
    // COUNT
    // ============================================================

    @Transactional(readOnly = true)
    public long countAllNurses() {
        return nurseRepository.count();
    }

    @Transactional(readOnly = true)
    public long countNursesByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return nurseRepository.countByDepartment_Id(
                departmentId
        );
    }

    @Transactional(readOnly = true)
    public long countNursesBySpecialty(
            NurseSpecialty specialty
    ) {
        helper.validateSpecialty(specialty);

        return nurseRepository.countBySpecialty(
                specialty
        );
    }

    @Transactional(readOnly = true)
    public long countNursesByShift(
            ShiftType shiftType
    ) {
        helper.validateShiftType(shiftType);

        return nurseRepository.countByShiftType(
                shiftType
        );
    }

    @Transactional(readOnly = true)
    public long countNursesByActiveStatus(
            Boolean isActive
    ) {
        helper.validateBoolean(isActive, "Active status");

        return nurseRepository.countByUser_IsActive(
                isActive
        );
    }

    @Transactional(readOnly = true)
    public long countChargeNurses(
            Boolean isChargeNurse
    ) {
        helper.validateBoolean(
                isChargeNurse,
                "Charge nurse status"
        );

        return nurseRepository.countByIsChargeNurse(
                isChargeNurse
        );
    }

    // ============================================================
    // STATS
    // ============================================================

    @Transactional(readOnly = true)
    public NurseStatsResponse getNurseStats() {
        log.info("Fetching nurse statistics");

        long total = nurseRepository.count();

        long morning = nurseRepository.countByShiftType(
                ShiftType.MORNING
        );

        long afternoon = nurseRepository.countByShiftType(
                ShiftType.AFTERNOON
        );

        long night = nurseRepository.countByShiftType(
                ShiftType.NIGHT
        );

        long rotating = nurseRepository.countByShiftType(
                ShiftType.ROTATING
        );

        return new NurseStatsResponse(
                total,
                morning,
                afternoon,
                night,
                rotating
        );
    }



    @Transactional(readOnly = true)
    public Page<NurseResponseDTO> getNurses(
            String text,
            Long departmentId,
            NurseSpecialty specialty,
            ShiftType shiftType,
            Boolean isActive,
            Boolean isChargeNurse,
            Integer minimumExperienceValue,
            Integer maximumExperienceValue,
            LocalDate hiredFromValue,
            LocalDate hiredToValue,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (departmentId != null) {
            helper.validateDepartmentExist(departmentId);
        }

        if (specialty != null) {
            helper.validateSpecialty(specialty);
        }

        if (shiftType != null) {
            helper.validateShiftType(shiftType);
        }

        if (minimumExperienceValue != null) {
            helper.validateExperience(
                    minimumExperienceValue
            );
        }

        if (maximumExperienceValue != null) {
            helper.validateExperience(
                    maximumExperienceValue
            );
        }

        if (minimumExperienceValue != null
                && maximumExperienceValue != null
                && minimumExperienceValue
                > maximumExperienceValue) {
            throw new IllegalArgumentException(
                    "Minimum experience cannot be greater "
                            + "than maximum experience"
            );
        }

        if (hiredFromValue != null
                && hiredToValue != null) {
            helper.validateDateRangeLocalDate(
                    hiredFromValue,
                    hiredToValue
            );
        }

        String normalizedText =
                normalizeOptionalFilter(text);

        log.info(
                "Fetching nurses with filters: "
                        + "text={}, departmentId={}, specialty={}, "
                        + "shiftType={}, active={}, charge={}, "
                        + "minimumExperience={}, maximumExperience={}, "
                        + "hiredFrom={}, hiredTo={}",
                normalizedText,
                departmentId,
                specialty,
                shiftType,
                isActive,
                isChargeNurse,
                minimumExperienceValue,
                maximumExperienceValue,
                hiredFromValue,
                hiredToValue
        );

        Specification<Nurse> specification =
                textContains(normalizedText)
                        .and(belongsToDepartment(
                                departmentId
                        ))
                        .and(hasSpecialty(specialty))
                        .and(hasShiftType(shiftType))
                        .and(hasActiveStatus(isActive))
                        .and(hasChargeStatus(
                                isChargeNurse
                        ))
                        .and(minimumExperience(
                                minimumExperienceValue
                        ))
                        .and(maximumExperience(
                                maximumExperienceValue
                        ))
                        .and(hiredFrom(hiredFromValue))
                        .and(hiredTo(hiredToValue));

        return nurseRepository
                .findAll(specification, pageable)
                .map(nurseMapper::toResponseDTO);
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