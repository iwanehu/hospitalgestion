package com.hospital.gestion.api.doctor.mapper;

import com.hospital.gestion.api.department.entity.Department;
import com.hospital.gestion.api.doctor.dto.DoctorRequestDTO;
import com.hospital.gestion.api.doctor.dto.DoctorResponseDTO;
import com.hospital.gestion.api.doctor.dto.DoctorUpdateDTO;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoctorMapper {

    public Doctor toEntity(
            DoctorRequestDTO request,
            User user,
            Department department
    ) {

        return Doctor.builder()
                .user(user)
                .department(department)
                .specialty(request.specialty())
                .medicalLicenseNumber(
                        request.medicalLicenseNumber()
                )
                .yearsOfExperience(
                        request.yearsOfExperience()
                )
                .biography(request.biography())
                .build();
    }

    public DoctorResponseDTO toResponseDTO(
            Doctor doctor
    ) {

        User user = doctor.getUser();

        Department department =
                doctor.getDepartment();

        return new DoctorResponseDTO(
                doctor.getId(),

                user.getId(),
                doctor.getFullName(),
                user.getEmail(),

                department.getId(),
                department.getDepartmentType(),

                doctor.getSpecialty(),
                doctor.getMedicalLicenseNumber(),
                doctor.getYearsOfExperience(),
                doctor.getBiography(),

                doctor.getCreatedAt(),
                doctor.getUpdatedAt()
        );
    }

    public void updateEntity(
            Doctor doctor,
            DoctorUpdateDTO request,
            Department department
    ) {

        doctor.setDepartment(department);
        doctor.setSpecialty(request.specialty());
        doctor.setYearsOfExperience(
                request.yearsOfExperience()
        );
        doctor.setBiography(request.biography());
    }

    public List<DoctorResponseDTO> toResponseDTOList(
            List<Doctor> doctors
    ) {
        return doctors.stream()
                .map(this::toResponseDTO)
                .toList();
    }
}