package com.hospital.gestion.api.patient.mapper;

import com.hospital.gestion.api.patient.dto.PatientRequestDTO;
import com.hospital.gestion.api.patient.dto.PatientResponseDTO;
import com.hospital.gestion.api.patient.dto.PatientUpdateDTO;
import com.hospital.gestion.api.patient.entity.EmergencyContact;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PatientMapper {

    public Patient toEntity(
            PatientRequestDTO request,
            User user
    ) {

        return Patient.builder()
                .user(user)
                .bloodType(request.bloodType())
                .birthDate(request.birthDate())
                .emergencyContact(
                        buildEmergencyContact(request)
                )
                .allergies(request.allergies())
                .hasHealthInsurance(
                        Boolean.TRUE.equals(
                                request.hasHealthInsurance()
                        )
                )
                .healthInsuranceProvider(
                        request.healthInsuranceProvider()
                )
                .healthInsuranceNumber(
                        request.healthInsuranceNumber()
                )
                .medicalHistory(request.medicalHistory())
                .build();
    }

    public PatientResponseDTO toResponseDTO(
            Patient patient
    ) {

        User user = patient.getUser();

        EmergencyContact contact =
                patient.getEmergencyContact();

        return new PatientResponseDTO(
                patient.getId(),

                user.getId(),
                patient.getFullName(),
                user.getEmail(),
                user.getDocumentId(),
                user.getPhone(),

                patient.getBloodType() != null
                        ? patient.getBloodType().name()
                        : null,

                patient.getBirthDate(),

                contact != null
                        ? contact.getName()
                        : null,

                contact != null
                        ? contact.getPhone()
                        : null,

                contact != null
                        ? contact.getRelationship()
                        : null,

                patient.getAllergies(),

                patient.getHasHealthInsurance(),
                patient.getHealthInsuranceProvider(),
                patient.getHealthInsuranceNumber(),

                patient.getMedicalHistory(),

                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }

    public void updateEntity(
            Patient patient,
            PatientUpdateDTO request
    ) {

        patient.setBloodType(request.bloodType());

        patient.setEmergencyContact(
                buildEmergencyContact(request)
        );

        patient.setAllergies(request.allergies());

        if (request.hasHealthInsurance() != null) {
            patient.setHasHealthInsurance(
                    request.hasHealthInsurance()
            );
        }

        patient.setHealthInsuranceProvider(
                request.healthInsuranceProvider()
        );

        patient.setHealthInsuranceNumber(
                request.healthInsuranceNumber()
        );

        patient.setMedicalHistory(
                request.medicalHistory()
        );
    }

    private EmergencyContact buildEmergencyContact(
            PatientRequestDTO request
    ) {

        if (request.emergencyContactName() == null
                && request.emergencyContactPhone() == null
                && request.emergencyContactRelationship() == null) {

            return null;
        }

        return EmergencyContact.builder()
                .name(request.emergencyContactName())
                .phone(request.emergencyContactPhone())
                .relationship(
                        request.emergencyContactRelationship()
                )
                .build();
    }

    private EmergencyContact buildEmergencyContact(
            PatientUpdateDTO request
    ) {

        if (request.emergencyContactName() == null
                && request.emergencyContactPhone() == null
                && request.emergencyContactRelationship() == null) {

            return null;
        }

        return EmergencyContact.builder()
                .name(request.emergencyContactName())
                .phone(request.emergencyContactPhone())
                .relationship(
                        request.emergencyContactRelationship()
                )
                .build();
    }

    public List<PatientResponseDTO> toResponseDTOList(List<Patient> patients) {
       return patients.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}