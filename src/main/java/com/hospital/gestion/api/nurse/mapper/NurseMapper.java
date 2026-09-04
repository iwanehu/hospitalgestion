package com.hospital.gestion.api.nurse.mapper;

import com.hospital.gestion.api.common.enums.NurseSpecialty;
import com.hospital.gestion.api.common.helper.HospitalEntityHelper;
import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.nurse.dto.NurseRequestDTO;
import com.hospital.gestion.api.nurse.dto.NurseResponseDTO;
import com.hospital.gestion.api.nurse.dto.NurseUpdateDTO;
import com.hospital.gestion.api.nurse.entity.EmergencyContact;
import com.hospital.gestion.api.nurse.entity.Nurse;
import com.hospital.gestion.api.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NurseMapper {

    private final HospitalEntityHelper helper;

    public Nurse toEntity(
            NurseRequestDTO request,
            User user,
            Department department
    ) {
        return Nurse.builder()
                .user(user)
                .department(department)
                .licenseNumber(
                        request.licenseNumber().trim()
                )
                .specialty(
                        request.specialty() != null
                                ? request.specialty()
                                : NurseSpecialty.GENERAL
                )
                .shiftType(request.shiftType())
                .yearsOfExperience(
                        request.yearsOfExperience()
                )
                .hireDate(request.hireDate())
                .biography(
                helper.normalizeNullableText(
                                request.biography()
                        )
                )
                .emergencyContact(
                        buildEmergencyContact(request)
                )
                .maxPatientsPerShift(
                        request.maxPatientsPerShift()
                )
                .isChargeNurse(
                        Boolean.TRUE.equals(
                                request.isChargeNurse()
                        )
                )
                .vacationDaysAvailable(
                        request.vacationDaysAvailable()
                )
                .build();
    }

    public NurseResponseDTO toResponseDTO(
            Nurse nurse
    ) {

        User user = nurse.getUser();

        Department department =
                nurse.getDepartment();

        EmergencyContact contact =
                nurse.getEmergencyContact();

        return new NurseResponseDTO(
                nurse.getId(),

                user.getId(),
                nurse.getFullName(),
                user.getEmail(),

                department.getId(),
                department.getDepartmentType(),

                nurse.getLicenseNumber(),

                nurse.getSpecialty(),
                nurse.getShiftType(),

                nurse.getYearsOfExperience(),
                nurse.getHireDate(),
                nurse.getBiography(),

                contact != null
                        ? contact.getEmergencyContactName()
                        : null,

                contact != null
                        ? contact.getEmergencyContactPhone()
                        : null,

                contact != null
                        ? contact.getEmergencyContactRelationship()
                        : null,

                nurse.getMaxPatientsPerShift(),
                nurse.getIsChargeNurse(),
                nurse.getVacationDaysAvailable(),

                nurse.getCreatedAt(),
                nurse.getUpdatedAt()
        );
    }

    public void updateEntity(
            Nurse nurse,
            NurseUpdateDTO request,
            Department department
    ) {

        if (department != null) {
            nurse.setDepartment(department);
        }

        if (request.specialty() != null) {
            nurse.setSpecialty(request.specialty());
        }

        if (request.shiftType() != null) {
            nurse.setShiftType(request.shiftType());
        }

        if (request.yearsOfExperience() != null) {
            nurse.setYearsOfExperience(
                    request.yearsOfExperience()
            );
        }

        if (request.hireDate() != null) {
            nurse.setHireDate(request.hireDate());
        }

        nurse.setBiography(request.biography());

        nurse.setEmergencyContact(
                buildEmergencyContact(request)
        );

        if (request.maxPatientsPerShift() != null) {
            nurse.setMaxPatientsPerShift(
                    request.maxPatientsPerShift()
            );
        }

        if (request.isChargeNurse() != null) {
            nurse.setIsChargeNurse(
                    request.isChargeNurse()
            );
        }

        if (request.vacationDaysAvailable() != null) {
            nurse.setVacationDaysAvailable(
                    request.vacationDaysAvailable()
            );
        }
    }

    private EmergencyContact buildEmergencyContact(
            NurseRequestDTO request
    ) {

        if (request.emergencyContactName() == null
                && request.emergencyContactPhone() == null
                && request.emergencyContactRelationship() == null) {

            return null;
        }

        return EmergencyContact.builder()
                .emergencyContactName(
                        request.emergencyContactName()
                )
                .emergencyContactPhone(
                        request.emergencyContactPhone()
                )
                .emergencyContactRelationship(
                        request.emergencyContactRelationship()
                )
                .build();
    }

    private EmergencyContact buildEmergencyContact(
            NurseUpdateDTO request
    ) {

        if (request.emergencyContactName() == null
                && request.emergencyContactPhone() == null
                && request.emergencyContactRelationship() == null) {

            return null;
        }

        return EmergencyContact.builder()
                .emergencyContactName(
                        request.emergencyContactName()
                )
                .emergencyContactPhone(
                        request.emergencyContactPhone()
                )
                .emergencyContactRelationship(
                        request.emergencyContactRelationship()
                )
                .build();
    }

    public List<NurseResponseDTO> toResponseDTOList(List<Nurse> nurses) {
        return nurses.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}