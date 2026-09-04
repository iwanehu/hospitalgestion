package com.hospital.gestion.api.common.helper;

import com.hospital.gestion.api.admin.entity.Admin;
import com.hospital.gestion.api.admin.repository.AdminRepository;
import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.admission.repository.AdmissionRepository;
import com.hospital.gestion.api.appointment.entity.Appointment;
import com.hospital.gestion.api.appointment.repository.AppointmentRepository;
import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.bed.repository.BedRepository;
import com.hospital.gestion.api.common.enums.*;
import com.hospital.gestion.api.common.exception.ConflictException;
import com.hospital.gestion.api.common.exception.ResourceNotFoundException;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.department.repository.DepartmentRepository;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.doctor.repository.DoctorRepository;
import com.hospital.gestion.api.nurse.entity.Nurse;
import com.hospital.gestion.api.nurse.repository.NurseRepository;
import com.hospital.gestion.api.patient.entity.EmergencyContact;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.patient.repository.PatientRepository;
import com.hospital.gestion.api.receptionist.entity.Receptionist;
import com.hospital.gestion.api.receptionist.repository.ReceptionistRepository;
import com.hospital.gestion.api.room.entity.Room;
import com.hospital.gestion.api.room.repository.RoomRepository;
import com.hospital.gestion.api.user.dto.PasswordChangeDTO;
import com.hospital.gestion.api.user.entity.User;
import com.hospital.gestion.api.user.repository.UserRepository;
import com.hospital.gestion.api.ward.entity.Ward;
import com.hospital.gestion.api.ward.repository.WardRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@RequiredArgsConstructor
@Component
public class HospitalEntityHelper {
    private final RoomRepository roomRepository;
    private final WardRepository wardRepository;
    private final DepartmentRepository departmentRepository;
    private final PatientRepository patientRepository;
    private final BedRepository bedRepository;
    private final DoctorRepository doctorRepository;
    private final AdmissionRepository admissionRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final NurseRepository nurseRepository;
    private final ReceptionistRepository receptionistRepository;
    private final AdminRepository adminRepository;



    //FInder


    public Admin findAdminById(Long id) {
        validateId(id, "Admin");

        return adminRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found with id: " + id
                        )
                );
    }


    public Admin findAdminByIdForUpdate(Long id) {
        validateId(id, "Admin");

        return adminRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admin not found with id: " + id
                        )
                );
    }

    public Receptionist findReceptionistById(
            Long id
    ) {
        validateId(id, "Receptionist");

        return receptionistRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Receptionist not found with id: " + id
                        )
                );
    }


    public Receptionist findReceptionistByIdForUpdate(
            Long id
    ) {
        validateId(id, "Receptionist");

        return receptionistRepository
                .findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Receptionist not found with id: "
                                        + id
                        )
                );
    }


    public Nurse findNurseById(Long id) {
        validateId(id, "Nurse");

        return nurseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Nurse not found with id: " + id
                        )
                );
    }


    public Nurse findNurseByIdForUpdate(Long id) {
        validateId(id, "Nurse");

        return nurseRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Nurse not found with id: " + id
                        )
                );
    }


    public Patient findPatientById(Long id) {
        validateId(id, "Patient");

        return patientRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with id: " + id
                        )
                );
    }


    public User findUserById(Long id) {
        validateId(id,"User");

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );
    }


    public Admission findAdmissionById(Long id) {
        validateId(id, "Admission");

        return admissionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admission not found with id: "+ id
                        )
                );
    }



    public Ward findWardById(Long id) {
        validateId(id, "Ward");

        return wardRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ward not found with id " + id)
                );
    }


    public Room  findRoomById(Long id) {
        validateId(id, "Room");
        return roomRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Room not found with id " + id)
        );
    }

    public Doctor findDoctorById(Long id) {
        validateId(id, "Doctor");

        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + id
                        )
                );
    }

    public User findUserByIdForUpdate(Long id) {
        validateId(id,"User");

        return userRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id: " + id
                        )
                );
    }


    public Admission findAdmissionByIdForUpdate(
            Long id
    ) {
        validateId(id, "Admission");

        return admissionRepository
                .findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Admission not found with id: " + id
                        )
                );
    }

    public Patient findPatientByIdForUpdate(
            Long id
    ) {
        validateId(id, "Patient");

        return patientRepository
                .findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Patient not found with id: " + id
                        )
                );
    }

    public Doctor findDoctorByIdForUpdate(Long id) {
        validateId(id, "Doctor");

        return doctorRepository
                .findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Doctor not found with id: " + id
                        )
                );
    }

    public Bed findBedByIdForUpdate(Long id) {
        validateId(id, "Bed");

        return bedRepository.findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bed not found with id: " + id
                        )
                );
    }

    public Room findRoomByIdForUpdate(Long id) {
        validateId(id, "Room");

        return roomRepository
                .findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with id: " + id
                        )
                );
    }

    public Appointment findAppointmentById(Long id) {
        validateId(id, "Appointment");

        return appointmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with id: " + id
                        )
                );
    }

    public Appointment findAppointmentByIdForUpdate(
            Long id
    ) {
        validateId(id, "Appointment");

        return appointmentRepository
                .findByIdForUpdate(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Appointment not found with id: " + id
                        )
                );
    }


    public Bed findBedById(Long id) {
        validateId(id, "Bed");

        return bedRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bed not found with id: " + id
                        )
                );
    }


    // ==========================
    // EXISTENCE VALIDATIONS
    // ============

    public void validateDepartmentExist(Long departmentId) {
        validateId(departmentId, "Department");
        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department not found with id " + departmentId);
        }

    }

    public void validateWardExists(Long wardId) {
        validateId(wardId, "Ward");

        if (!wardRepository.existsById(wardId)) {
            throw new ResourceNotFoundException("Ward not found with id " + wardId);
        }
    }

    public void validateRoomExists(Long roomId) {
        validateId(roomId, "Room");
        if (!roomRepository.existsById(roomId)) {
            throw new ResourceNotFoundException("Room not found with id " + roomId);
        }
    }


    public void validateRequiredText(String value,String fieldName){
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty"
            );
        }
    }


    public void validateRoomNumber(String number){
        if (number == null ||  number.isBlank()){
            throw new IllegalArgumentException(
                    "Room number cannot be empty"
            );
        }
        if (number.trim().length()>20){
            throw new IllegalArgumentException(
                    "Room number cannot be longer than 20 characters"
            );
        }
    }

    public void validateRoomStatus(RoomStatus status){
        if (status == null){
            throw new IllegalArgumentException(
                    "Room status cannot be null"
            );
        }
    }

    public void validateRoomType(RoomType roomType) {
        if (roomType == null) {
            throw new IllegalArgumentException(
                    "Room type cannot be null"
            );
        }
    }


    public void validateFloor(Integer floor) {
        if (floor == null || floor < -1 || floor > 50) {
            throw new IllegalArgumentException(
                    "Floor must be between -1 and 50"
            );
        }
    }

    public void validateRoomNumberForUpdate(
            Room room,
            String newNumber
    ) {
        String currentNumber = room.getNumber();

        boolean numberChanged =
                currentNumber == null
                        || !newNumber.equalsIgnoreCase(
                        currentNumber.trim()
                );

        if (numberChanged
                && roomRepository.existsByNumberIgnoreCase(
                newNumber
        )) {
            throw new ConflictException(
                    "Room number already exists: "
                            + newNumber
            );
        }
    }

    public void validateCapacityForUpdate(
            Room room,
            Integer newCapacity
    ) {
        if (newCapacity == null || newCapacity < 1) {
            throw new IllegalArgumentException(
                    "Room capacity must be greater than zero"
            );
        }

        int totalBeds = room.getTotalBeds();

        if (newCapacity < totalBeds) {
            throw new ConflictException(
                    "Room capacity cannot be lower than its "
                            + "current number of beds: "
                            + totalBeds
            );
        }
    }

    //=======
    //Field Validation
    //===========
    public void  validateWardName(String name){
        if (name== null || name.isBlank()){
            throw new IllegalArgumentException("Ward name cannot be empty");
        }
    }

    public void validateActiveStatus(Boolean isActive){
        if (isActive==null){
            throw new IllegalArgumentException("Active status cannot be null");
        }
    }

    public void validateId(Long id, String resourceName) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(resourceName + " id must be greater than zero");
        }
    }
    public void validatePageable(Pageable pageable){
        if (pageable==null){
            throw new IllegalArgumentException("Pageable cannot be null");
        }
    }

    public String normalizeNullableText(String value) {
        return value == null ? null : value.trim();
    }


    //=====BED
    public void validateBedNumber(
            String bedNumber
    ) {
        if (bedNumber == null
                || bedNumber.isBlank()) {
            throw new IllegalArgumentException(
                    "Bed number cannot be empty"
            );
        }

        if (bedNumber.trim().length() > 20) {
            throw new IllegalArgumentException(
                    "Bed number cannot exceed 20 characters"
            );
        }
    }

    public void validateBedStatus(
            BedStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Bed status cannot be null"
            );
        }
    }




    public void validatePatientExists(Long patientId) {
        validateId(patientId, "Patient");

        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException(
                    "Patient not found with id: " + patientId
            );
        }
    }

    public void validateBedExists(Long bedId) {
        validateId(bedId, "Bed");

        if (!bedRepository.existsById(bedId)) {
            throw new ResourceNotFoundException(
                    "Bed not found with id: " + bedId
            );
        }
    }



    public void validateDoctorExists(Long doctorId) {
        validateId(doctorId, "Doctor");

        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException(
                    "Doctor not found with id: " + doctorId
            );
        }
    }

    public void validateAdmissionReason(
            String reason
    ) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Admission reason cannot be empty"
            );
        }

        if (reason.trim().length() > 255) {
            throw new IllegalArgumentException(
                    "Admission reason cannot exceed "
                            + "255 characters"
            );
        }
    }


    public void validateAdmissionStatus(
            AdmissionStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Admission status cannot be null"
            );
        }
    }


    public void validateDateRange(
            LocalDateTime start,
            LocalDateTime end
    ) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "Start and end dates are required"
            );
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }
    }


    public void validateAppoinmentStatus(
            AppointmentStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Appointment status cannot be null"
            );
        }
    }


    //======user=========
    public void validateUniqueEmail(String email) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException(
                    "Email is already registered: " + email
            );
        }
    }

    public void validateUniqueDocument(
            String documentId
    ) {
        if (userRepository
                .existsByDocumentIdIgnoreCase(documentId)) {
            throw new ConflictException(
                    "Document ID is already registered: "
                            + documentId
            );
        }
    }



    public void validatePasswordChangeRequest(
            PasswordChangeDTO request
    ) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Password change request cannot be null"
            );
        }

        if (request.oldPassword() == null
                || request.oldPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "Current password cannot be empty"
            );
        }

        validatePassword(request.newPassword());

        if (request.confirmPassword() == null
                || request.confirmPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "Password confirmation cannot be empty"
            );
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Password cannot be empty"
            );
        }
    }

    public void validateName(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty"
            );
        }

        if (value.trim().length() > 100) {
            throw new IllegalArgumentException(
                    fieldName
                            + " cannot exceed 100 characters"
            );
        }
    }

    public void validateSearchText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Search text cannot be empty"
            );
        }
    }

    public void validateRole(Role role) {
        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }
    }


    public void validateLength(
            String value,
            int maximum,
            String fieldName
    ) {
        if (value != null
                && value.trim().length() > maximum) {
            throw new IllegalArgumentException(
                    fieldName
                            + " cannot exceed "
                            + maximum
                            + " characters"
            );
        }
    }


    //=============user//=============
    public void validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException(
                    "Birth date cannot be null"
            );
        }

        if (birthDate.isAfter(LocalDate.now(ZoneId.of("Europe/Madrid")))) {
            throw new IllegalArgumentException(
                    "Birth date cannot be in the future"
            );
        }
    }

    public void validateEmergencyContact(
            String name,
            String phone,
            String relationship
    ) {
        boolean contactProvided =
                hasText(name)
                        || hasText(phone)
                        || hasText(relationship);

        if (!contactProvided) {
            return;
        }

        if (!hasText(name)) {
            throw new IllegalArgumentException(
                    "Emergency contact name is required"
            );
        }

        if (!hasText(phone)) {
            throw new IllegalArgumentException(
                    "Emergency contact phone is required"
            );
        }

        validateLength(name, 150, "Emergency contact name");
        validateLength(phone, 20, "Emergency contact phone");

        if (relationship != null) {
            validateLength(
                    relationship,
                    50,
                    "Emergency contact relationship"
            );
        }
    }



    public boolean hasText(String value) {
        return value != null && !value.isBlank();
    }


    public void validateInsurance(
            Boolean hasInsurance,
            String provider,
            String number
    ) {
        if (!Boolean.TRUE.equals(hasInsurance)) {
            return;
        }

        if (!hasText(provider)) {
            throw new IllegalArgumentException(
                    "Insurance provider is required"
            );
        }

        if (!hasText(number)) {
            throw new IllegalArgumentException(
                    "Insurance number is required"
            );
        }

        validateLength(
                provider,
                100,
                "Insurance provider"
        );

        validateLength(
                number,
                50,
                "Insurance number"
        );
    }


    public void validateDateRangeLocalDate(
            LocalDate start,
            LocalDate end
    ) {
        if (start == null || end == null) {
            throw new IllegalArgumentException(
                    "Start and end dates are required"
            );
        }

        if (start.isAfter(end)) {
            throw new IllegalArgumentException(
                    "Start date cannot be after end date"
            );
        }

    }



    public void validateBloodType(
            BloodType bloodType
    ) {
        if (bloodType == null) {
            throw new IllegalArgumentException(
                    "Blood type cannot be null"
            );
        }
    }



    public void validateBoolean(
            Boolean value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be null"
            );
        }
    }




    public void normalizePatient(
            Patient patient,
            Boolean requestedInsuranceStatus
    ) {
        patient.setAllergies(
                normalizeNullableText(patient.getAllergies())
        );

        patient.setMedicalHistory(
                normalizeNullableText(
                        patient.getMedicalHistory()
                )
        );

        EmergencyContact contact =
                patient.getEmergencyContact();

        if (contact != null) {
            contact.setName(
                    normalizeNullableText(contact.getName())
            );
            contact.setPhone(
                    normalizeNullableText(contact.getPhone())
            );
            contact.setRelationship(
                    normalizeNullableText(
                            contact.getRelationship()
                    )
            );
        }

        boolean hasInsurance =
                Boolean.TRUE.equals(
                        requestedInsuranceStatus
                );

        patient.setHasHealthInsurance(hasInsurance);

        if (hasInsurance) {
            patient.setHealthInsuranceProvider(
                    normalizeNullableText(
                            patient.getHealthInsuranceProvider()
                    )
            );
            patient.setHealthInsuranceNumber(
                    normalizeNullableText(
                            patient.getHealthInsuranceNumber()
                    )
            );
        } else {
            patient.setHealthInsuranceProvider(null);
            patient.setHealthInsuranceNumber(null);
        }
    }



    public String normalizeRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new ConflictException(
                    fieldName + " cannot be empty"
            );
        }

        return value.trim();
    }


    //===================
    public Department findActiveDepartmentById(
            Long id
    ) {
        validateId(id, "Department");

        Department department =
                departmentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Department not found with id: "
                                                + id
                                )
                        );

        if (Boolean.FALSE.equals(
                department.getIsActive()
        )) {
            throw new ConflictException(
                    "Department is inactive with id: " + id
            );
        }

        return department;
    }


    //=========DOCTOR
    public void validateSpecialty(
            Specialty specialty
    ) {
        if (specialty == null) {
            throw new IllegalArgumentException(
                    "Specialty cannot be null"
            );
        }
    }

    public void validateExperience(
            Integer yearsOfExperience
    ) {
        if (yearsOfExperience == null
                || yearsOfExperience < 0) {
            throw new IllegalArgumentException(
                    "Years of experience must be "
                            + "greater than or equal to zero"
            );
        }
    }


    public String normalizeLicense(String license) {
        String normalized =
                normalizeRequiredText(
                        license,
                        "Medical license"
                ).toUpperCase(Locale.ROOT);

        if (normalized.length() > 50) {
            throw new IllegalArgumentException(
                    "Medical license cannot exceed "
                            + "50 characters"
            );
        }

        return normalized;
    }


    //============nurse
    public void validateSpecialty(
            NurseSpecialty specialty
    ) {
        if (specialty == null) {
            throw new IllegalArgumentException(
                    "Nurse specialty cannot be null"
            );
        }
    }

    public void validateShiftType(
            ShiftType shiftType
    ) {
        if (shiftType == null) {
            throw new IllegalArgumentException(
                    "Shift type cannot be null"
            );
        }
    }



    public Department findDepartmentById(
            Long departmentId
    ) {
        validateId(departmentId, "Department");

        return departmentRepository
                .findById(departmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Department not found with id: "
                                        + departmentId
                        )
                );
    }

    public Department resolveDepartment(
            AdminLevel adminLevel,
            Long departmentId
    ) {
        validateAdminLevel(adminLevel);

        if (adminLevel == AdminLevel.DEPARTMENT_ADMIN) {
            if (departmentId == null) {
                throw new IllegalArgumentException(
                        "Department ID is required for a department admin"
                );
            }

            return findDepartmentById(departmentId);
        }

        if (departmentId != null) {
            throw new IllegalArgumentException(
                    "Department ID is only allowed for a department admin"
            );
        }

        return null;
    }


    public void validateAdminLevel(
            AdminLevel adminLevel
    ) {
        if (adminLevel == null) {
            throw new IllegalArgumentException(
                    "Admin level cannot be null"
            );
        }
    }



    public List<AdminPermission> normalizePermissions(
            List<AdminPermission> permissions
    ) {
        if (permissions == null) {
            return new ArrayList<>();
        }

        if (permissions.stream().anyMatch(
                permission -> permission == null
        )) {
            throw new IllegalArgumentException(
                    "Permissions cannot contain null values"
            );
        }

        /*
         * LinkedHashSet elimina duplicados manteniendo
         * el orden recibido.
         */
        return new ArrayList<>(
                new LinkedHashSet<>(permissions)
        );
    }


    public void validatePermission(
            AdminPermission permission
    ) {
        if (permission == null) {
            throw new IllegalArgumentException(
                    "Admin permission cannot be null"
            );
        }
    }



    public void validatePageable(
            Pageable pageable,
            Set<String> allowedSortProperties
    ) {
        validatePageable(pageable);

        if (allowedSortProperties == null
                || allowedSortProperties.isEmpty()) {
            throw new IllegalArgumentException(
                    "Allowed sort properties cannot be empty"
            );
        }

        pageable.getSort().forEach(order -> {
            String property = order.getProperty();

            if (!allowedSortProperties.contains(property)) {
                throw new IllegalArgumentException(
                        "Invalid sort property: " + property
                );
            }
        });
    }





}
