package com.hospital.gestion.api.bed.dto;

import com.hospital.gestion.api.common.validation.ValidBedNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BedUpdateDTO(

        @ValidBedNumber
        @NotBlank(message = "Bed number is required")
        String bedNumber,

        @Size(max = 500, message = "Notes must be less than 500 characters")
        String notes

) {
}