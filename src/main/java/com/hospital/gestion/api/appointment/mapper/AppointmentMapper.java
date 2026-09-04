package com.hospital.gestion.api.appointment.mapper;

import com.hospital.gestion.api.appointment.dto.AppointmentRequestDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentResponseDTO;
import com.hospital.gestion.api.appointment.dto.AppointmentUpdateDTO;
import com.hospital.gestion.api.appointment.entity.Appointment;
import com.hospital.gestion.api.doctor.entity.Doctor;
import com.hospital.gestion.api.patient.entity.Patient;
import com.hospital.gestion.api.room.entity.Room;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AppointmentMapper {

    public Appointment toEntity(
            AppointmentRequestDTO request,
            Doctor doctor,
            Patient patient,
            Room room
    ) {

        return Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .room(room)
                .dateTime(request.dateTime())
                .reason(request.reason())
                .notes(request.notes())
                .build();
    }

    public AppointmentResponseDTO toResponseDTO(
            Appointment appointment
    ) {

        Room room =
                appointment.getRoom();

        return new AppointmentResponseDTO(
                appointment.getId(),

                appointment.getDoctor().getId(),
                appointment.getDoctor()
                        .getFullName(),

                appointment.getPatient().getId(),
                appointment.getPatient()
                        .getFullName(),

                room != null
                        ? room.getId()
                        : null,

                room != null
                        ? room.getNumber()
                        : null,

                appointment.getDateTime(),

                appointment.getReason(),
                appointment.getNotes(),

                appointment.getStatus(),

                appointment.getCancellationReason(),

                appointment.getCancelledAt(),
                appointment.getConfirmedAt(),
                appointment.getCompletedAt(),

                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }

    public void updateEntity(
            Appointment appointment,
            AppointmentUpdateDTO request,
            Doctor doctor,
            Room room
    ) {

        appointment.setDoctor(doctor);
        appointment.setRoom(room);

        appointment.setDateTime(
                request.dateTime()
        );

        appointment.setReason(
                request.reason()
        );

        appointment.setNotes(
                request.notes()
        );
    }

    public List<AppointmentResponseDTO> toResponseDTOList(
            List<Appointment> appointmens){
        return appointmens.stream()
                .map(this::toResponseDTO)
                .toList();
    }


}