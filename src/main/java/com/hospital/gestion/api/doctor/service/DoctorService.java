package com.hospital.gestion.api.doctor.service;

import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.appointment.repository.AppointmentRepository;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.enums.Specialty;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.doctor.dto.DoctorRequestDTO;
import com.hospital.gestion.api.doctor.dto.DoctorResponseDTO;
import com.hospital.gestion.api.doctor.dto.DoctorUpdateDTO;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.doctor.mapper.DoctorMapper;
import com.hospital.gestion.api.doctor.repository.DoctorRepository;
import com.hospital.gestion.api.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static com.hospital.gestion.api.doctor.specification.DoctorSpecification.belongsToDepartment;
import static com.hospital.gestion.api.doctor.specification.DoctorSpecification.hasActiveStatus;
import static com.hospital.gestion.api.doctor.specification.DoctorSpecification.hasSpecialty;
import static com.hospital.gestion.api.doctor.specification.DoctorSpecification.maximumExperience;
import static com.hospital.gestion.api.doctor.specification.DoctorSpecification.minimumExperience;
import static com.hospital.gestion.api.doctor.specification.DoctorSpecification.textContains;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorService {



    private final DoctorRepository doctorRepository;

    private final AdmissionRepository admissionRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorMapper doctorMapper;
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
            "specialty",
            "medicalLicenseNumber",
            "yearsOfExperience",
            "createdAt",
            "updatedAt"
    );

    // ========================================
    // CREATE
    // ========================================

    @Transactional
    public DoctorResponseDTO createDoctor(
            DoctorRequestDTO request
    ) {
        log.info(
                "Creating doctor for user: {}",
                request.userId()
        );

        helper.validateSpecialty(request.specialty());
        helper.validateExperience(request.yearsOfExperience());

        String normalizedLicense =
                helper.normalizeLicense(
                        request.medicalLicenseNumber()
                );

        User user = helper.findUserByIdForUpdate(
                request.userId()
        );

        if (user.getRole() != Role.DOCTOR) {
            throw new ConflictException(
                    "User must have DOCTOR role"
            );
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ConflictException(
                    "Inactive user cannot be registered "
                            + "as a doctor"
            );
        }

        if (doctorRepository.existsByUser_Id(user.getId())) {
            throw new ConflictException(
                    "User is already associated with a doctor"
            );
        }

        if (doctorRepository
                .existsByMedicalLicenseNumberIgnoreCase(
                        normalizedLicense
                )) {
            throw new ConflictException(
                    "Medical license is already registered: "
                            + normalizedLicense
            );
        }

        Department department =
                helper.findActiveDepartmentById(
                        request.departmentId()
                );

        Doctor doctor = doctorMapper.toEntity(
                request,
                user,
                department
        );

        doctor.setMedicalLicenseNumber(
                normalizedLicense
        );

        doctor.setBiography(
                helper.normalizeNullableText(
                        request.biography()
                )
        );

        Doctor savedDoctor =
                doctorRepository.save(doctor);

        log.info(
                "Doctor created successfully with id: {}",
                savedDoctor.getId()
        );

        return doctorMapper.toResponseDTO(savedDoctor);
    }

    // ========================================
    // GET ALL
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getAllDoctors() {
        return doctorMapper.toResponseDTOList(
                doctorRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> getAllDoctors(
            Pageable pageable
    ) {
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return doctorRepository.findAll(pageable)
                .map(doctorMapper::toResponseDTO);
    }

    // ========================================
    // GET ORDERED
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO>
    getAllDoctorsOrdered() {
        return doctorMapper.toResponseDTOList(
                doctorRepository
                        .findAllByOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO>
    getActiveDoctorsOrdered() {
        return doctorMapper.toResponseDTOList(
                doctorRepository
                        .findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    // ========================================
    // GET BY ID
    // ========================================

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorById(Long id) {
        return doctorMapper.toResponseDTO(
                helper.findDoctorById(id)
        );
    }

    // ========================================
    // GET BY USER
    // ========================================

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorByUserId(
            Long userId
    ) {
        helper.validateId(userId, "User");

        Doctor doctor = doctorRepository
                .findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found for user: "
                                        + userId
                        )
                );

        return doctorMapper.toResponseDTO(doctor);
    }

    // ========================================
    // GET BY EMAIL
    // ========================================

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorByEmail(
            String email
    ) {
        String normalizedEmail =
                helper.normalizeRequiredText(email, "Email")
                        .toLowerCase(Locale.ROOT);

        Doctor doctor = doctorRepository
                .findByUser_EmailIgnoreCase(
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with email: "
                                        + normalizedEmail
                        )
                );

        return doctorMapper.toResponseDTO(doctor);
    }

    // ========================================
    // GET BY DOCUMENT
    // ========================================

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorByDocumentId(
            String documentId
    ) {
        String normalizedDocument =
                helper.normalizeRequiredText(
                        documentId,
                        "Document ID"
                );

        Doctor doctor = doctorRepository
                .findByUser_DocumentIdIgnoreCase(
                        normalizedDocument
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with document: "
                                        + normalizedDocument
                        )
                );

        return doctorMapper.toResponseDTO(doctor);
    }

    // ========================================
    // GET BY LICENSE
    // ========================================

    @Transactional(readOnly = true)
    public DoctorResponseDTO getDoctorByLicense(
            String license
    ) {
        String normalizedLicense =
                helper.normalizeLicense(license);

        Doctor doctor = doctorRepository
                .findByMedicalLicenseNumberIgnoreCase(
                        normalizedLicense
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with license: "
                                        + normalizedLicense
                        )
                );

        return doctorMapper.toResponseDTO(doctor);
    }

    // ========================================
    // GET BY SPECIALTY
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getDoctorsBySpecialty(
            Specialty specialty
    ) {
        helper.validateSpecialty(specialty);

        return doctorMapper.toResponseDTOList(
                doctorRepository.findBySpecialty(specialty)
        );
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> getDoctorsBySpecialty(
            Specialty specialty,
            Pageable pageable
    ) {
        helper.validateSpecialty(specialty);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return doctorRepository
                .findBySpecialty(specialty, pageable)
                .map(doctorMapper::toResponseDTO);
    }

    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getDoctorsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return doctorMapper.toResponseDTOList(
                doctorRepository.findByDepartment_Id(
                        departmentId
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> getDoctorsByDepartment(
            Long departmentId,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return doctorRepository
                .findByDepartment_Id(
                        departmentId,
                        pageable
                )
                .map(doctorMapper::toResponseDTO);
    }

    // ========================================
    // DEPARTMENT AND SPECIALTY
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO>
    getDoctorsByDepartmentAndSpecialty(
            Long departmentId,
            Specialty specialty
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateSpecialty(specialty);

        return doctorMapper.toResponseDTOList(
                doctorRepository
                        .findByDepartment_IdAndSpecialty(
                                departmentId,
                                specialty
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO>
    getDoctorsByDepartmentAndSpecialty(
            Long departmentId,
            Specialty specialty,
            Pageable pageable
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateSpecialty(specialty);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return doctorRepository
                .findByDepartment_IdAndSpecialty(
                        departmentId,
                        specialty,
                        pageable
                )
                .map(doctorMapper::toResponseDTO);
    }

    // ========================================
    // GET BY ACTIVE STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO>
    getDoctorsByActiveStatus(
            Boolean isActive
    ) {
        helper.validateActiveStatus(isActive);

        return doctorMapper.toResponseDTOList(
                doctorRepository.findByUser_IsActive(
                        isActive
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO>
    getDoctorsByActiveStatus(
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateActiveStatus(isActive);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return doctorRepository
                .findByUser_IsActive(
                        isActive,
                        pageable
                )
                .map(doctorMapper::toResponseDTO);
    }

    // ========================================
    // SPECIALTY AND ACTIVE STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO>
    getDoctorsBySpecialtyAndActiveStatus(
            Specialty specialty,
            Boolean isActive
    ) {
        helper.validateSpecialty(specialty);
        helper.validateActiveStatus(isActive);

        return doctorMapper.toResponseDTOList(
                doctorRepository
                        .findBySpecialtyAndUser_IsActive(
                                specialty,
                                isActive
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO>
    getDoctorsBySpecialtyAndActiveStatus(
            Specialty specialty,
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateSpecialty(specialty);
        helper.validateActiveStatus(isActive);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return doctorRepository
                .findBySpecialtyAndUser_IsActive(
                        specialty,
                        isActive,
                        pageable
                )
                .map(doctorMapper::toResponseDTO);
    }

    // ========================================
    // DEPARTMENT AND ACTIVE STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO>
    getDoctorsByDepartmentAndActiveStatus(
            Long departmentId,
            Boolean isActive
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateActiveStatus(isActive);

        return doctorMapper.toResponseDTOList(
                doctorRepository
                        .findByDepartment_IdAndUser_IsActive(
                                departmentId,
                                isActive
                        )
        );
    }

    // ========================================
    // SEARCH
    // ========================================

    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> searchDoctors(
            String text
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        return doctorMapper.toResponseDTOList(
                doctorRepository.searchDoctors(
                        normalizedText
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> searchDoctors(
            String text,
            Pageable pageable
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return doctorRepository
                .searchDoctors(
                        normalizedText,
                        pageable
                )
                .map(doctorMapper::toResponseDTO);
    }

    // ========================================
    // UPDATE
    // ========================================

    @Transactional
    public DoctorResponseDTO updateDoctor(
            Long id,
            DoctorUpdateDTO request
    ) {
        log.info("Updating doctor: {}", id);

        Doctor doctor = helper.findDoctorByIdForUpdate(id);

        helper.validateSpecialty(request.specialty());
        helper.validateExperience(
                request.yearsOfExperience()
        );

        Department department =
                helper.findActiveDepartmentById(
                        request.departmentId()
                );

        doctorMapper.updateEntity(
                doctor,
                request,
                department
        );

        doctor.setBiography(
                helper.normalizeNullableText(
                        request.biography()
                )
        );

        Doctor updatedDoctor =
                doctorRepository.save(doctor);

        log.info(
                "Doctor updated successfully: {}",
                id
        );

        return doctorMapper.toResponseDTO(
                updatedDoctor
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @Transactional
    public void deleteDoctor(Long id) {
        log.info("Deleting doctor: {}", id);

        Doctor doctor = helper.findDoctorByIdForUpdate(id);

        if (admissionRepository
                .countByAttendingDoctor_Id(id) > 0) {
            throw new ConflictException(
                    "Doctor cannot be deleted because "
                            + "they have admission history"
            );
        }

        if (appointmentRepository
                .countByDoctor_Id(id) > 0) {
            throw new ConflictException(
                    "Doctor cannot be deleted because "
                            + "they have appointment history"
            );
        }

        try {
            doctorRepository.delete(doctor);
            doctorRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    "Doctor cannot be deleted because "
                            + "they are associated with records"
            );
        }

        log.info(
                "Doctor deleted successfully: {}",
                id
        );
    }

    // ========================================
    // EXISTS
    // ========================================

    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        helper.validateId(userId, "User");

        return doctorRepository.existsByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public boolean existsByLicense(String license) {
        return doctorRepository
                .existsByMedicalLicenseNumberIgnoreCase(
                        helper.normalizeLicense(license)
                );
    }

    // ========================================
    // COUNT
    // ========================================

    @Transactional(readOnly = true)
    public long countAllDoctors() {
        return doctorRepository.count();
    }

    @Transactional(readOnly = true)
    public long countDoctorsBySpecialty(
            Specialty specialty
    ) {
        helper.validateSpecialty(specialty);

        return doctorRepository.countBySpecialty(
                specialty
        );
    }

    @Transactional(readOnly = true)
    public long countDoctorsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return doctorRepository.countByDepartment_Id(
                departmentId
        );
    }

    @Transactional(readOnly = true)
    public long countDoctorsByActiveStatus(
            Boolean isActive
    ) {
        helper.validateActiveStatus(isActive);

        return doctorRepository.countByUser_IsActive(
                isActive
        );
    }

    @Transactional(readOnly = true)
    public long countDoctorsBySpecialtyAndActiveStatus(
            Specialty specialty,
            Boolean isActive
    ) {
        helper.validateSpecialty(specialty);
        helper.validateActiveStatus(isActive);

        return doctorRepository
                .countBySpecialtyAndUser_IsActive(
                        specialty,
                        isActive
                );
    }

    @Transactional(readOnly = true)
    public long countDoctorsByDepartmentAndSpecialty(
            Long departmentId,
            Specialty specialty
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateSpecialty(specialty);

        return doctorRepository
                .countByDepartment_IdAndSpecialty(
                        departmentId,
                        specialty
                );
    }









    @Transactional(readOnly = true)
    public Page<DoctorResponseDTO> getDoctors(
            String text,
            Long departmentId,
            Specialty specialty,
            Boolean isActive,
            Integer minimumExperienceValue,
            Integer maximumExperienceValue,
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

        String normalizedText =
                normalizeOptionalFilter(text);

        log.info(
                "Fetching doctors with filters: "
                        + "text={}, departmentId={}, specialty={}, "
                        + "active={}, minimumExperience={}, "
                        + "maximumExperience={}",
                normalizedText,
                departmentId,
                specialty,
                isActive,
                minimumExperienceValue,
                maximumExperienceValue
        );

        Specification<Doctor> specification =
                textContains(normalizedText)
                        .and(belongsToDepartment(
                                departmentId
                        ))
                        .and(hasSpecialty(specialty))
                        .and(hasActiveStatus(isActive))
                        .and(minimumExperience(
                                minimumExperienceValue
                        ))
                        .and(maximumExperience(
                                maximumExperienceValue
                        ));

        return doctorRepository
                .findAll(specification, pageable)
                .map(doctorMapper::toResponseDTO);
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