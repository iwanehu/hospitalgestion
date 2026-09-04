package com.hospital.gestion.api.patient.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class EmergencyContact {

    @Column(name = "emergency_contact_name",length = 150)
    private String name;

    @Column(name = "emergency_contact_phone",length = 20)
    private String phone;

    @Column(name = "emergency_contact_relationship",length = 50)
    private String relationship;
}
