package com.hospital.gestion.api.admission.service;

import com.hospital.gestion.api.admission.dto.AdmissionDischargeDTO;
import com.hospital.gestion.api.admission.dto.AdmissionRequestDTO;
import com.hospital.gestion.api.admission.dto.AdmissionResponseDTO;
import com.hospital.gestion.api.admission.dto.AdmissionTransferDTO;
import com.hospital.gestion.api.admission.dto.AdmissionUpdateDTO;
import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.admission.mapper.AdmissionMapper;
import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.common.enums.AdmissionStatus;
import com.hospital.gestion.api.common.enums.BedStatus;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.patient.entity.Patient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.admittedFrom;
import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.admittedTo;
import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.belongsToBed;
import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.belongsToDepartment;
import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.belongsToDoctor;
import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.belongsToPatient;
import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.belongsToRoom;
import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.belongsToWard;
import static com.hospital.gestion.api.admission.specification.AdmissionSpecification.hasStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdmissionService {










    private final AdmissionRepository admissionRepository;

    private final AdmissionMapper admissionMapper;
    private final HospitalEntityHelper helper;




    private static final Set<String>
            ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "status",
            "patient.id",
            "attendingDoctor.id",
            "bed.id",
            "bed.room.id",
            "bed.room.ward.id",
            "bed.room.ward.department.id",
            "admissionReason",
            "admittedAt",
            "dischargedAt",
            "createdAt",
            "updatedAt"
    );

    // ========================================
    // CREATE
    // ========================================

    @Transactional
    public AdmissionResponseDTO createAdmission(
            AdmissionRequestDTO request
    ) {
        log.info(
                "Creating admission for patient: {} in bed: {}",
                request.patientId(),
                request.bedId()
        );

        helper.validateAdmissionReason(
                request.admissionReason()
        );

        Patient patient = helper.findPatientByIdForUpdate(
                request.patientId()
        );

        if (admissionRepository
                .existsByPatient_IdAndStatus(
                        patient.getId(),
                        AdmissionStatus.ACTIVE
                )) {
            throw new ConflictException(
                    "Patient already has an active admission"
            );
        }

        Bed bed = helper.findBedByIdForUpdate(
                request.bedId()
        );

        validateBedCanBeOccupied(bed);

        if (admissionRepository
                .existsByBed_IdAndStatus(
                        bed.getId(),
                        AdmissionStatus.ACTIVE
                )) {
            throw new ConflictException(
                    "Bed already has an active admission"
            );
        }

        Doctor doctor = helper.findDoctorById(
                request.attendingDoctorId()
        );

        Admission admission = Admission.builder()
                .patient(patient)
                .bed(bed)
                .attendingDoctor(doctor)
                .admissionReason(
                        request.admissionReason().trim()
                )
                .admittedAt(
                        LocalDateTime.now(
                                Admission.ZONE_MADRID
                        )
                )
                .notes(
                        helper.normalizeNullableText(request.notes())
                )
                .build();

        executeBedTransition(
                bed::occupy
        );

        Admission savedAdmission =
                admissionRepository.save(admission);

        log.info(
                "Admission created successfully with id: {}",
                savedAdmission.getId()
        );

        return admissionMapper.toResponseDTO(
                savedAdmission
        );
    }

    // ========================================
    // GET ALL
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO> getAllAdmissions() {
        log.info("Fetching all admissions");

        return admissionMapper.toResponseDTOList(
                admissionRepository.findAll()
        );
    }

    @Transactional(readOnly = true)
    public Page<AdmissionResponseDTO> getAllAdmissions(
            Pageable pageable
    ) {
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return admissionRepository.findAll(pageable)
                .map(admissionMapper::toResponseDTO);
    }

    // ========================================
    // GET BY ID
    // ========================================

    @Transactional(readOnly = true)
    public AdmissionResponseDTO getAdmissionById(
            Long id
    ) {
        return admissionMapper.toResponseDTO(
                helper.findAdmissionById(id)
        );
    }

    // ========================================
    // GET ACTIVE ADMISSION BY PATIENT
    // ========================================

    @Transactional(readOnly = true)
    public AdmissionResponseDTO
    getActiveAdmissionByPatient(
            Long patientId
    ) {
        helper.validatePatientExists(patientId);

        Admission admission = admissionRepository
                .findFirstByPatient_IdAndStatus(
                        patientId,
                        AdmissionStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Active admission not found "
                                        + "for patient: "
                                        + patientId
                        )
                );

        return admissionMapper.toResponseDTO(admission);
    }

    // ========================================
    // GET BY PATIENT
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO> getAdmissionsByPatient(
            Long patientId
    ) {
        helper.validatePatientExists(patientId);

        return admissionMapper.toResponseDTOList(
                admissionRepository.findByPatient_Id(
                        patientId
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AdmissionResponseDTO> getAdmissionsByPatient(
            Long patientId,
            Pageable pageable
    ) {
        helper.validatePatientExists(patientId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return admissionRepository
                .findByPatient_Id(patientId, pageable)
                .map(admissionMapper::toResponseDTO);
    }

    // ========================================
    // GET BY STATUS
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO> getAdmissionsByStatus(
            AdmissionStatus status
    ) {
        helper.validateAdmissionStatus(status);

        return admissionMapper.toResponseDTOList(
                admissionRepository.findByStatus(status)
        );
    }

    @Transactional(readOnly = true)
    public Page<AdmissionResponseDTO> getAdmissionsByStatus(
            AdmissionStatus status,
            Pageable pageable
    ) {
        helper.validateAdmissionStatus(status);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return admissionRepository
                .findByStatus(status, pageable)
                .map(admissionMapper::toResponseDTO);
    }

    // ========================================
    // GET BY DOCTOR
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO> getAdmissionsByDoctor(
            Long doctorId
    ) {
        helper.validateDoctorExists(doctorId);

        return admissionMapper.toResponseDTOList(
                admissionRepository
                        .findByAttendingDoctor_Id(doctorId)
        );
    }

    @Transactional(readOnly = true)
    public Page<AdmissionResponseDTO> getAdmissionsByDoctor(
            Long doctorId,
            Pageable pageable
    ) {
        helper.validateDoctorExists(doctorId);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return admissionRepository
                .findByAttendingDoctor_Id(
                        doctorId,
                        pageable
                )
                .map(admissionMapper::toResponseDTO);
    }

    // ========================================
    // GET BY BED
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO> getAdmissionsByBed(
            Long bedId
    ) {
        helper.validateBedExists(bedId);

        return admissionMapper.toResponseDTOList(
                admissionRepository.findByBed_Id(bedId)
        );
    }

    // ========================================
    // GET BY ROOM
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO> getAdmissionsByRoom(
            Long roomId
    ) {
        helper.validateRoomExists(roomId);

        return admissionMapper.toResponseDTOList(
                admissionRepository.findByBed_Room_Id(
                        roomId
                )
        );
    }

    // ========================================
    // GET BY WARD
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO> getAdmissionsByWard(
            Long wardId
    ) {
        helper.validateWardExists(wardId);

        return admissionMapper.toResponseDTOList(
                admissionRepository
                        .findByBed_Room_Ward_Id(wardId)
        );
    }

    // ========================================
    // GET BY DEPARTMENT
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO>
    getAdmissionsByDepartment(
            Long departmentId
    ) {
        helper.validateDepartmentExist(departmentId);

        return admissionMapper.toResponseDTOList(
                admissionRepository
                        .findByBed_Room_Ward_Department_Id(
                                departmentId
                        )
        );
    }

    // ========================================
    // GET BY DATE RANGE
    // ========================================

    @Transactional(readOnly = true)
    public List<AdmissionResponseDTO>
    getAdmissionsByDateRange(
            LocalDateTime start,
            LocalDateTime end
    ) {
        helper.validateDateRange(start, end);

        return admissionMapper.toResponseDTOList(
                admissionRepository.findByAdmittedAtBetween(
                        start,
                        end
                )
        );
    }

    @Transactional(readOnly = true)
    public Page<AdmissionResponseDTO>
    getAdmissionsByDateRange(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        helper.validateDateRange(start, end);
        helper.validatePageable(pageable, ALLOWED_SORT_PROPERTIES);

        return admissionRepository
                .findByAdmittedAtBetween(
                        start,
                        end,
                        pageable
                )
                .map(admissionMapper::toResponseDTO);
    }

    // ========================================
    // UPDATE
    // ========================================

    @Transactional
    public AdmissionResponseDTO updateAdmission(
            Long id,
            AdmissionUpdateDTO request
    ) {
        log.info("Updating admission: {}", id);

        Admission admission =
                helper.findAdmissionByIdForUpdate(id);

        validateActiveAdmission(admission);
        helper.validateAdmissionReason(
                request.admissionReason()
        );

        Doctor doctor = helper.findDoctorById(
                request.attendingDoctorId()
        );

        admission.setAttendingDoctor(doctor);
        admission.setAdmissionReason(
                request.admissionReason().trim()
        );
        admission.setNotes(
                helper.normalizeNullableText(request.notes())
        );

        Admission updatedAdmission =
                admissionRepository.saveAndFlush(admission);

        return admissionMapper.toResponseDTO(
                updatedAdmission
        );
    }

    // ========================================
    // DISCHARGE
    // ========================================

    @Transactional
    public AdmissionResponseDTO dischargeAdmission(
            Long id,
            AdmissionDischargeDTO request
    ) {
        log.info("Discharging admission: {}", id);

        Admission admission =
                helper.findAdmissionByIdForUpdate(id);

        validateActiveAdmission(admission);

        Bed bed = helper.findBedByIdForUpdate(
                admission.getBed().getId()
        );

        executeAdmissionTransition(
                admission::discharge
        );

        executeBedTransition(
                bed::free
        );

        if (request != null
                && request.notes() != null
                && !request.notes().isBlank()) {

            admission.setNotes(
                    appendNotes(
                            admission.getNotes(),
                            request.notes().trim()
                    )
            );
        }

        Admission savedAdmission =
                admissionRepository.saveAndFlush(admission);

        log.info(
                "Admission discharged successfully: {}",
                savedAdmission.getId()
        );

        return admissionMapper.toResponseDTO(savedAdmission);
    }
    // ========================================
    // TRANSFER
    // ========================================

    @Transactional
    public AdmissionResponseDTO transferAdmission(
            Long id,
            AdmissionTransferDTO request
    ) {
        log.info(
                "Transferring admission: {} to bed: {}",
                id,
                request.newBedId()
        );

        Admission currentAdmission =
                helper.findAdmissionByIdForUpdate(id);

        validateActiveAdmission(currentAdmission);

        Patient patient = helper.findPatientByIdForUpdate(
                currentAdmission.getPatient().getId()
        );

        Long currentBedId =
                currentAdmission.getBed().getId();

        if (currentBedId.equals(request.newBedId())) {
            throw new ConflictException(
                    "Patient is already assigned to this bed"
            );
        }

        Bed currentBed =
                helper.findBedByIdForUpdate(currentBedId);

        Bed newBed =
                helper.findBedByIdForUpdate(request.newBedId());

        validateBedCanBeOccupied(newBed);

        if (admissionRepository
                .existsByBed_IdAndStatus(
                        newBed.getId(),
                        AdmissionStatus.ACTIVE
                )) {
            throw new ConflictException(
                    "New bed already has an active admission"
            );
        }

        executeAdmissionTransition(

                currentAdmission::transfer
        );

        executeBedTransition(
                currentBed::free
        );

        executeBedTransition(
                newBed::occupy
        );

        if (request.reason() != null
                && !request.reason().isBlank()) {

            String normalizedReason = request.reason().trim();

            currentAdmission.setNotes(
                    appendNotes(
                            currentAdmission.getNotes(),
                            "Transfer: " + normalizedReason
                    )
            );
        }

        admissionRepository.saveAndFlush(currentAdmission);


        String normalizedReason =
                request.reason() == null
                        || request.reason().isBlank()
                        ? null
                        : request.reason().trim();

        Admission newAdmission = Admission.builder()
                .patient(patient)
                .bed(newBed)
                .attendingDoctor(
                        currentAdmission.getAttendingDoctor()
                )
                .admissionReason(
                        currentAdmission.getAdmissionReason()
                )
                .admittedAt(
                        LocalDateTime.now(
                                Admission.ZONE_MADRID
                        )
                )
                .notes(
                        request.reason() == null
                                ? null
                                : helper.normalizeNullableText(
                                "Transferred from admission "
                                + currentAdmission.getId()
                                + ": "
                                + normalizedReason
                        )
                )
                .build();

        Admission savedAdmission =
                admissionRepository.saveAndFlush(newAdmission);

        log.info(
                "Admission transferred successfully. "
                        + "New admission id: {}",
                savedAdmission.getId()
        );

        return admissionMapper.toResponseDTO(
                savedAdmission
        );
    }

    // ========================================
    // CANCEL
    // ========================================

    @Transactional
    public AdmissionResponseDTO cancelAdmission(Long id) {
        log.info("Cancelling admission: {}", id);

        Admission admission =
                helper.findAdmissionByIdForUpdate(id);

        validateActiveAdmission(admission);

        Bed bed = helper.findBedByIdForUpdate(
                admission.getBed().getId()
        );

        executeAdmissionTransition(
                admission::cancel
        );

        executeBedTransition(
                bed::free
        );

        Admission savedAdmission =
                admissionRepository.saveAndFlush(admission);

        log.info(
                "Admission cancelled successfully: {}",
                savedAdmission.getId()
        );

        return admissionMapper.toResponseDTO(savedAdmission);
    }

    // ========================================
    // DELETE
    // ========================================

    @Transactional
    public void deleteAdmission(Long id) {
        log.info("Deleting admission: {}", id);

        Admission admission =
                helper.findAdmissionByIdForUpdate(id);

        if (admission.isActive()) {
            throw new ConflictException(
                    "Active admission cannot be deleted"
            );
        }

        if (admission.getStatus()
                != AdmissionStatus.CANCELLED) {
            throw new ConflictException(
                    "Only cancelled admissions can be deleted"
            );
        }

        admissionRepository.delete(admission);

        log.info(
                "Admission deleted successfully: {}",
                id
        );
    }

    // ========================================
    // EXISTS
    // ========================================

    @Transactional(readOnly = true)
    public boolean patientHasActiveAdmission(
            Long patientId
    ) {
        helper.validatePatientExists(patientId);

        return admissionRepository
                .existsByPatient_IdAndStatus(
                        patientId,
                        AdmissionStatus.ACTIVE
                );
    }

    @Transactional(readOnly = true)
    public boolean bedHasActiveAdmission(
            Long bedId
    ) {
        helper.validateBedExists(bedId);

        return admissionRepository
                .existsByBed_IdAndStatus(
                        bedId,
                        AdmissionStatus.ACTIVE
                );
    }

    // ========================================
    // COUNT
    // ========================================

    @Transactional(readOnly = true)
    public long countAllAdmissions() {
        return admissionRepository.count();
    }

    @Transactional(readOnly = true)
    public long countAdmissionsByStatus(
            AdmissionStatus status
    ) {
        helper.validateAdmissionStatus(status);

        return admissionRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countAdmissionsByPatient(
            Long patientId
    ) {
        helper.validatePatientExists(patientId);

        return admissionRepository.countByPatient_Id(
                patientId
        );
    }

    @Transactional(readOnly = true)
    public long countAdmissionsByDoctor(
            Long doctorId
    ) {
        helper.validateDoctorExists(doctorId);

        return admissionRepository
                .countByAttendingDoctor_Id(doctorId);
    }

    @Transactional(readOnly = true)
    public long countAdmissionsByDoctorAndStatus(
            Long doctorId,
            AdmissionStatus status
    ) {
        helper.validateDoctorExists(doctorId);
        helper.validateAdmissionStatus(status);

        return admissionRepository
                .countByAttendingDoctor_IdAndStatus(
                        doctorId,
                        status
                );
    }

    @Transactional(readOnly = true)
    public long countAdmissionsByRoomAndStatus(
            Long roomId,
            AdmissionStatus status
    ) {
        helper.validateRoomExists(roomId);
        helper.validateAdmissionStatus(status);

        return admissionRepository
                .countByBed_Room_IdAndStatus(
                        roomId,
                        status
                );
    }

    @Transactional(readOnly = true)
    public long countAdmissionsByWardAndStatus(
            Long wardId,
            AdmissionStatus status
    ) {
        helper.validateWardExists(wardId);
        helper.validateAdmissionStatus(status);

        return admissionRepository
                .countByBed_Room_Ward_IdAndStatus(
                        wardId,
                        status
                );
    }

    @Transactional(readOnly = true)
    public long countAdmissionsByDepartmentAndStatus(
            Long departmentId,
            AdmissionStatus status
    ) {
        helper.validateDepartmentExist(departmentId);
        helper.validateAdmissionStatus(status);

        return admissionRepository
                .countByBed_Room_Ward_Department_IdAndStatus(
                        departmentId,
                        status
                );
    }

    // ========================================
    // PRIVATE HELPERS
    // ========================================














    private void validateActiveAdmission(
            Admission admission
    ) {
        if (!admission.isActive()) {
            throw new ConflictException(
                    "Admission is not active"
            );
        }
    }

    private void validateBedCanBeOccupied(Bed bed) {
        if (bed.getStatus() != BedStatus.AVAILABLE
                && bed.getStatus() != BedStatus.RESERVED) {
            throw new ConflictException(
                    "Bed must be available or reserved"
            );
        }
    }

    private void executeAdmissionTransition(
            Runnable transition
    ) {
        try {
            transition.run();
        } catch (IllegalStateException exception) {
            throw new ConflictException(
                    exception.getMessage()
            );
        }
    }

    private void executeBedTransition(
            Runnable transition
    ) {
        try {
            transition.run();
        } catch (IllegalStateException exception) {
            throw new ConflictException(
                    exception.getMessage()
            );
        }
    }



    private String appendNotes(
            String currentNotes,
            String newNotes
    ) {
        String normalizedNewNotes =
                helper.normalizeNullableText(newNotes);

        if (normalizedNewNotes == null) {
            return currentNotes;
        }

        if (currentNotes == null
                || currentNotes.isBlank()) {
            return normalizedNewNotes;
        }

        return currentNotes.trim()
                + System.lineSeparator()
                + normalizedNewNotes;
    }



    //============================
    @Transactional(readOnly = true)
    public Page<AdmissionResponseDTO> getAdmissions(
            AdmissionStatus status,
            Long patientId,
            Long doctorId,
            Long bedId,
            Long roomId,
            Long wardId,
            Long departmentId,
            LocalDateTime admittedFromValue,
            LocalDateTime admittedToValue,
            Pageable pageable
    ) {
        helper.validatePageable(
                pageable,
                ALLOWED_SORT_PROPERTIES
        );

        if (status != null) {
            helper.validateAdmissionStatus(status);
        }

        if (patientId != null) {
            helper.validatePatientExists(patientId);
        }

        if (doctorId != null) {
            helper.validateDoctorExists(doctorId);
        }

        if (bedId != null) {
            helper.validateBedExists(bedId);
        }

        if (roomId != null) {
            helper.validateRoomExists(roomId);
        }

        if (wardId != null) {
            helper.validateWardExists(wardId);
        }

        if (departmentId != null) {
            helper.validateDepartmentExist(departmentId);
        }

        if (admittedFromValue != null
                && admittedToValue != null) {
            helper.validateDateRange(
                    admittedFromValue,
                    admittedToValue
            );
        }

        log.info(
                "Fetching admissions with filters: "
                        + "status={}, patientId={}, doctorId={}, "
                        + "bedId={}, roomId={}, wardId={}, "
                        + "departmentId={}, admittedFrom={}, "
                        + "admittedTo={}",
                status,
                patientId,
                doctorId,
                bedId,
                roomId,
                wardId,
                departmentId,
                admittedFromValue,
                admittedToValue
        );

        Specification<Admission> specification =
                hasStatus(status)
                        .and(belongsToPatient(patientId))
                        .and(belongsToDoctor(doctorId))
                        .and(belongsToBed(bedId))
                        .and(belongsToRoom(roomId))
                        .and(belongsToWard(wardId))
                        .and(belongsToDepartment(
                                departmentId
                        ))
                        .and(admittedFrom(
                                admittedFromValue
                        ))
                        .and(admittedTo(
                                admittedToValue
                        ));

        return admissionRepository
                .findAll(specification, pageable)
                .map(admissionMapper::toResponseDTO);
    }

}