package com.hospital.gestion.api.admission.mapper;

import com.hospital.gestion.api.admission.dto.AdmissionResponseDTO;
import com.hospital.gestion.api.admission.entity.Admission;
import com.hospital.gestion.api.bed.entity.Bed;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.room.entity.Room;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdmissionMapper {

    public AdmissionResponseDTO toResponseDTO(
            Admission admission
    ) {

        Patient patient =
                admission.getPatient();

        Doctor doctor =
                admission.getAttendingDoctor();

        Bed bed =
                admission.getBed();

        Room room =
                bed.getRoom();

        return new AdmissionResponseDTO(
                admission.getId(),

                // Patient
                patient.getId(),
                patient.getFullName(),

                // Doctor
                doctor.getId(),
                doctor.getFullName(),

                // Bed
                bed.getId(),
                bed.getBedNumber(),

                // Room
                room.getId(),
                room.getNumber(),

                // Ward
                room.getWard().getId(),
                room.getWard().getName(),

                // Department
                room.getWard()
                        .getDepartment()
                        .getId(),

                room.getWard()
                        .getDepartment()
                        .getDepartmentType()
                        .name(),

                // Admission
                admission.getStatus().name(),
                admission.getAdmissionReason(),

                admission.getAdmittedAt(),
                admission.getDischargedAt(),

                admission.getNotes(),

                admission.getCreatedAt(),
                admission.getUpdatedAt()
        );
    }


    public List<AdmissionResponseDTO> toResponseDTOList(List<Admission> admissions) {
        return admissions.stream()
                .map(this::toResponseDTO)
                .toList();
    }



}