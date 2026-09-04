package com.hospital.gestion.api.patient.service;

import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.appointment.repository.AppointmentRepository;
import com.hospital.gestion.api.common.enums.BloodType;
import com.hospital.gestion.api.common.enums.Role;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.patient.dto.PatientRequestDTO;
import com.hospital.gestion.api.patient.dto.PatientResponseDTO;
import com.hospital.gestion.api.patient.dto.PatientUpdateDTO;
import com.hospital.gestion.api.patient.entity.EmergencyContact;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.patient.mapper.PatientMapper;
import com.hospital.gestion.api.patient.repository.PatientRepository;
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

import static com.hospital.gestion.api.patient.specification.PatientSpecification.birthDateFrom;
import static com.hospital.gestion.api.patient.specification.PatientSpecification.birthDateTo;
import static com.hospital.gestion.api.patient.specification.PatientSpecification.hasActiveStatus;
import static com.hospital.gestion.api.patient.specification.PatientSpecification.hasBloodType;
import static com.hospital.gestion.api.patient.specification.PatientSpecification.hasInsuranceStatus;
import static com.hospital.gestion.api.patient.specification.PatientSpecification.insuranceProviderContains;
import static com.hospital.gestion.api.patient.specification.PatientSpecification.textContains;


import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {





    private final PatientRepository patientRepository;

    private final AdmissionRepository admissionRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientMapper patientMapper;
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
            "bloodType",
            "birthDate",
            "hasHealthInsurance",
            "healthInsuranceProvider",
            "createdAt",
            "updatedAt"
    );




    // ========================================
    // CREATE
    // ========================================

    @Transactional
    public PatientResponseDTO createPatient(
            PatientRequestDTO request
    ) {
        log.info(
                "Creating patient for user: {}",
                request.userId()
        );

        helper.validateBirthDate(request.birthDate());
        helper.validateEmergencyContact(
                request.emergencyContactName(),
                request.emergencyContactPhone(),
                request.emergencyContactRelationship()
        );

        User user = helper.findUserByIdForUpdate(
                request.userId()
        );

        if (user.getRole() != Role.PATIENT) {
            throw new ConflictException(
                    "User must have PATIENT role"
            );
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new ConflictException(
                    "Inactive user cannot be registered "
                            + "as a patient"
            );
        }

        if (patientRepository.existsByUser_Id(user.getId())) {
            throw new ConflictException(
                    "User is already associated with a patient"
            );
        }

        helper.validateInsurance(
                request.hasHealthInsurance(),
                request.healthInsuranceProvider(),
                request.healthInsuranceNumber()
        );

        Patient patient =
                patientMapper.toEntity(request, user);

        helper.normalizePatient(
                patient,
                request.hasHealthInsurance()
        );

        Patient savedPatient =
                patientRepository.save(patient);

        log.info(
                "Patient created successfully with id: {}",
                savedPatient.getId()
        );

        return patientMapper.toResponseDTO(savedPatient);
    }

    // ========================================
    // GET ALL
    // ========================================

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getAllPatients() {
        log.info("Fetching all patients");

        return patientMapper.toResponseDTOList(
                patientRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDTO> getAllPatients(
            Pageable pageable
    ) {
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return patientRepository.findAll(pageable)
                .map(patientMapper::toResponseDTO);
    }

    // ========================================
    // GET ORDERED
    // ========================================

    @Transactional(readOnly = true)
    public List<PatientResponseDTO>
    getAllPatientsOrdered() {
        return patientMapper.toResponseDTOList(
                patientRepository
                        .findAllByOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    @Transactional(readOnly = true)
    public List<PatientResponseDTO>
    getActivePatientsOrdered() {
        return patientMapper.toResponseDTOList(
                patientRepository
                        .findByUser_IsActiveTrueOrderByUser_LastNameAscUser_FirstNameAsc()
        );
    }

    // ========================================
    // GET BY ID
    // ========================================

    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientById(Long id) {
        return patientMapper.toResponseDTO(
                helper.findPatientById(id)
        );
    }

    // ========================================
    // GET BY USER
    // ========================================

    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientByUserId(
            Long userId
    ) {
        helper.validateId(userId, "User");

        Patient patient = patientRepository
                .findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found for user: "
                                        + userId
                        )
                );

        return patientMapper.toResponseDTO(patient);
    }

    // ========================================
    // GET BY EMAIL
    // ========================================

    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientByEmail(
            String email
    ) {
        String normalizedEmail =
                helper.normalizeRequiredText(email, "Email")
                        .toLowerCase(Locale.ROOT);

        Patient patient = patientRepository
                .findByUser_EmailIgnoreCase(
                        normalizedEmail
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with email: "
                                        + normalizedEmail
                        )
                );

        return patientMapper.toResponseDTO(patient);
    }

    // ========================================
    // GET BY DOCUMENT
    // ========================================

    @Transactional(readOnly = true)
    public PatientResponseDTO getPatientByDocumentId(
            String documentId
    ) {
        String normalizedDocument =
                helper.normalizeRequiredText(
                        documentId,
                        "Document ID"
                ).toUpperCase(Locale.ROOT);

        Patient patient = patientRepository
                .findByUser_DocumentIdIgnoreCase(
                        normalizedDocument
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with document: "
                                        + normalizedDocument
                        )
                );

        return patientMapper.toResponseDTO(patient);
    }

    // ========================================
    // GET BY BLOOD TYPE
    // ========================================

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> getPatientsByBloodType(
            BloodType bloodType
    ) {
        helper.validateBloodType(bloodType);

        return patientMapper.toResponseDTOList(
                patientRepository.findByBloodType(bloodType)
        );
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDTO> getPatientsByBloodType(
            BloodType bloodType,
            Pageable pageable
    ) {
        helper.validateBloodType(bloodType);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return patientRepository
                .findByBloodType(bloodType, pageable)
                .map(patientMapper::toResponseDTO);
    }

    // ========================================
    // GET BY INSURANCE
    // ========================================

    @Transactional(readOnly = true)
    public List<PatientResponseDTO>
    getPatientsByInsuranceStatus(
            Boolean hasHealthInsurance
    ) {
        helper.validateBoolean(
                hasHealthInsurance,
                "Insurance status"
        );

        return patientMapper.toResponseDTOList(
                patientRepository.findByHasHealthInsurance(
                        hasHealthInsurance
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDTO>
    getPatientsByInsuranceStatus(
            Boolean hasHealthInsurance,
            Pageable pageable
    ) {
        helper.validateBoolean(
                hasHealthInsurance,
                "Insurance status"
        );
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return patientRepository
                .findByHasHealthInsurance(
                        hasHealthInsurance,
                        pageable
                )
                .map(patientMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<PatientResponseDTO>
    searchPatientsByInsuranceProvider(
            String provider
    ) {
        String normalizedProvider =
                helper.normalizeRequiredText(
                        provider,
                        "Insurance provider"
                );

        return patientMapper.toResponseDTOList(
                patientRepository
                        .findByHealthInsuranceProviderContainingIgnoreCase(
                                normalizedProvider
                        )
        );
    }

    // ========================================
    // GET BY USER STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<PatientResponseDTO>
    getPatientsByActiveStatus(
            Boolean isActive
    ) {
        helper.validateBoolean(isActive, "Active status");

        return patientMapper.toResponseDTOList(
                patientRepository.findByUser_IsActive(
                        isActive
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDTO>
    getPatientsByActiveStatus(
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateBoolean(isActive, "Active status");
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return patientRepository
                .findByUser_IsActive(
                        isActive,
                        pageable
                )
                .map(patientMapper::toResponseDTO);
    }

    // ========================================
    // BLOOD TYPE AND USER STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<PatientResponseDTO>
    getPatientsByBloodTypeAndActiveStatus(
            BloodType bloodType,
            Boolean isActive
    ) {
        helper.validateBloodType(bloodType);
        helper.validateBoolean(isActive, "Active status");

        return patientMapper.toResponseDTOList(
                patientRepository
                        .findByBloodTypeAndUser_IsActive(
                                bloodType,
                                isActive
                        )
        );
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDTO>
    getPatientsByBloodTypeAndActiveStatus(
            BloodType bloodType,
            Boolean isActive,
            Pageable pageable
    ) {
        helper.validateBloodType(bloodType);
        helper.validateBoolean(isActive, "Active status");
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return patientRepository
                .findByBloodTypeAndUser_IsActive(
                        bloodType,
                        isActive,
                        pageable
                )
                .map(patientMapper::toResponseDTO);
    }

    // ========================================
    // GET BY BIRTH DATE
    // ========================================

    @Transactional(readOnly = true)
    public List<PatientResponseDTO>
    getPatientsByBirthDateRange(
            LocalDate start,
            LocalDate end
    ) {
        helper.validateDateRangeLocalDate(start, end);

        return patientMapper.toResponseDTOList(
                patientRepository.findByBirthDateBetween(
                        start,
                        end
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDTO>
    getPatientsByBirthDateRange(
            LocalDate start,
            LocalDate end,
            Pageable pageable
    ) {
        helper.validateDateRangeLocalDate(start, end);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return patientRepository
                .findByBirthDateBetween(
                        start,
                        end,
                        pageable
                )
                .map(patientMapper::toResponseDTO);
    }

    // ========================================
    // SEARCH
    // ========================================

    @Transactional(readOnly = true)
    public List<PatientResponseDTO> searchPatients(
            String text
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        return patientMapper.toResponseDTOList(
                patientRepository.searchPatients(
                        normalizedText
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<PatientResponseDTO> searchPatients(
            String text,
            Pageable pageable
    ) {
        String normalizedText =
                helper.normalizeRequiredText(
                        text,
                        "Search text"
                );

        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return patientRepository
                .searchPatients(
                        normalizedText,
                        pageable
                )
                .map(patientMapper::toResponseDTO);
    }

    // ========================================
    // UPDATE
    // ========================================

    @Transactional
    public PatientResponseDTO updatePatient(
            Long id,
            PatientUpdateDTO request
    ) {
        log.info("Updating patient: {}", id);

        Patient patient = helper.findPatientByIdForUpdate(id);

        updateBloodType(patient, request);
        updateEmergencyContact(patient, request);
        updateMedicalInformation(patient, request);
        updateInsurance(patient, request);

        Patient updatedPatient =
                patientRepository.save(patient);

        log.info(
                "Patient updated successfully: {}",
                id
        );

        return patientMapper.toResponseDTO(
                updatedPatient
        );
    }

    private void updateBloodType(
            Patient patient,
            PatientUpdateDTO request
    ) {
        if (request.bloodType() != null) {
            patient.setBloodType(request.bloodType());
        }
    }

    private void updateEmergencyContact(
            Patient patient,
            PatientUpdateDTO request
    ) {
        boolean contactIncluded =
                request.emergencyContactName() != null
                        || request.emergencyContactPhone() != null
                        || request.emergencyContactRelationship()
                        != null;

        if (!contactIncluded) {
            return;
        }

        EmergencyContact current =
                patient.getEmergencyContact();

        if (current == null) {
            current = new EmergencyContact();
            patient.setEmergencyContact(current);
        }

        if (request.emergencyContactName() != null) {
            current.setName(
                    helper.normalizeNullableText(
                            request.emergencyContactName()
                    )
            );
        }

        if (request.emergencyContactPhone() != null) {
            current.setPhone(
                    helper.normalizeNullableText(
                            request.emergencyContactPhone()
                    )
            );
        }

        if (request.emergencyContactRelationship()
                != null) {
            current.setRelationship(
                    helper.normalizeNullableText(
                            request.emergencyContactRelationship()
                    )
            );
        }

        helper.validateEmergencyContact(
                current.getName(),
                current.getPhone(),
                current.getRelationship()
        );
    }

    private void updateMedicalInformation(
            Patient patient,
            PatientUpdateDTO request
    ) {
        if (request.allergies() != null) {
            patient.setAllergies(
                    helper.normalizeNullableText(
                            request.allergies()
                    )
            );
        }

        if (request.medicalHistory() != null) {
            patient.setMedicalHistory(
                    helper.normalizeNullableText(
                            request.medicalHistory()
                    )
            );
        }
    }

    private void updateInsurance(
            Patient patient,
            PatientUpdateDTO request
    ) {
        if (Boolean.FALSE.equals(
                request.hasHealthInsurance()
        )) {
            patient.setHasHealthInsurance(false);
            patient.setHealthInsuranceProvider(null);
            patient.setHealthInsuranceNumber(null);
            return;
        }

        if (Boolean.TRUE.equals(
                request.hasHealthInsurance()
        )) {
            patient.setHasHealthInsurance(true);
        }

        if (request.healthInsuranceProvider() != null) {
            patient.setHealthInsuranceProvider(
                    helper.normalizeNullableText(
                            request.healthInsuranceProvider()
                    )
            );
        }

        if (request.healthInsuranceNumber() != null) {
            patient.setHealthInsuranceNumber(
                    helper.normalizeNullableText(
                            request.healthInsuranceNumber()
                    )
            );
        }

        helper.validateInsurance(
                patient.getHasHealthInsurance(),
                patient.getHealthInsuranceProvider(),
                patient.getHealthInsuranceNumber()
        );
    }

    // ========================================
    // DELETE
    // ========================================

    @Transactional
    public void deletePatient(Long id) {
        log.info("Deleting patient: {}", id);

        Patient patient = helper.findPatientByIdForUpdate(id);

        if (admissionRepository.countByPatient_Id(id) > 0) {
            throw new ConflictException(
                    "Patient cannot be deleted because "
                            + "they have admission history"
            );
        }

        if (appointmentRepository.countByPatient_Id(id) > 0) {
            throw new ConflictException(
                    "Patient cannot be deleted because "
                            + "they have appointment history"
            );
        }

        try {
            patientRepository.delete(patient);
            patientRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    "Patient cannot be deleted because "
                            + "they are associated with "
                            + "hospital records"
            );
        }

        log.info(
                "Patient deleted successfully: {}",
                id
        );
    }

    // ========================================
    // EXISTS
    // ========================================

    @Transactional(readOnly = true)
    public boolean existsByUserId(Long userId) {
        helper.validateId(userId, "User");

        return patientRepository.existsByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        String normalizedEmail =
                helper.normalizeRequiredText(email, "Email");

        return patientRepository
                .existsByUser_EmailIgnoreCase(
                        normalizedEmail
                );
    }

    @Transactional(readOnly = true)
    public boolean existsByDocumentId(
            String documentId
    ) {
        String normalizedDocument =
                helper.normalizeRequiredText(
                        documentId,
                        "Document ID"
                );

        return patientRepository
                .existsByUser_DocumentIdIgnoreCase(
                        normalizedDocument
                );
    }

    // ========================================
    // COUNT
    // ========================================

    @Transactional(readOnly = true)
    public long countAllPatients() {
        return patientRepository.count();
    }

    @Transactional(readOnly = true)
    public long countPatientsByBloodType(
            BloodType bloodType
    ) {
        helper.validateBloodType(bloodType);

        return patientRepository.countByBloodType(
                bloodType
        );
    }

    @Transactional(readOnly = true)
    public long countPatientsByInsuranceStatus(
            Boolean hasHealthInsurance
    ) {
        helper.validateBoolean(
                hasHealthInsurance,
                "Insurance status"
        );

        return patientRepository
                .countByHasHealthInsurance(
                        hasHealthInsurance
                );
    }

    @Transactional(readOnly = true)
    public long countPatientsByActiveStatus(
            Boolean isActive
    ) {
        helper.validateBoolean(isActive, "Active status");

        return patientRepository.countByUser_IsActive(
                isActive
        );
    }

    @Transactional(readOnly = true)
    public long countPatientsByBloodTypeAndActiveStatus(
            BloodType bloodType,
            Boolean isActive
    ) {
        helper.validateBloodType(bloodType);
        helper.validateBoolean(isActive, "Active status");

        return patientRepository
                .countByBloodTypeAndUser_IsActive(
                        bloodType,
                        isActive
                );
    }







    @Transactional(readOnly = true)
    public Page<PatientResponseDTO> getPatients(
            String text,
            BloodType bloodType,
            Boolean hasHealthInsurance,
            String insuranceProvider,
            Boolean isActive,
            LocalDate birthDateFromValue,
            LocalDate birthDateToValue,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (bloodType != null) {
            helper.validateBloodType(bloodType);
        }

        if (birthDateFromValue != null
                && birthDateToValue != null) {
            helper.validateDateRangeLocalDate(
                    birthDateFromValue,
                    birthDateToValue
            );
        }

        String normalizedText =
                normalizeOptionalFilter(text);

        String normalizedInsuranceProvider =
                normalizeOptionalFilter(insuranceProvider);

        log.info(
                "Fetching patients with filters: "
                        + "text={}, bloodType={}, insured={}, "
                        + "provider={}, active={}, birthFrom={}, "
                        + "birthTo={}",
                normalizedText,
                bloodType,
                hasHealthInsurance,
                normalizedInsuranceProvider,
                isActive,
                birthDateFromValue,
                birthDateToValue
        );

        Specification<Patient> specification =
                textContains(normalizedText)
                        .and(hasBloodType(bloodType))
                        .and(hasInsuranceStatus(
                                hasHealthInsurance
                        ))
                        .and(insuranceProviderContains(
                                normalizedInsuranceProvider
                        ))
                        .and(hasActiveStatus(isActive))
                        .and(birthDateFrom(
                                birthDateFromValue
                        ))
                        .and(birthDateTo(
                                birthDateToValue
                        ));

        return patientRepository
                .findAll(specification, pageable)
                .map(patientMapper::toResponseDTO);
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