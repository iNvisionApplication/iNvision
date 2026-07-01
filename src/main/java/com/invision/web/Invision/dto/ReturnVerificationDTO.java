package com.invision.web.Invision.dto;

import com.invision.web.Invision.enums.Condition;
import com.invision.web.Invision.enums.Location;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReturnVerificationDTO(

        @NotNull(message = "Asset condition must be verified.")
        Condition condition,

        @NotNull(message = "Return location hub must be specified.")
        Location location,

        @Size(max = 500, message = "Manager remarks cannot exceed 500 characters.")
        String managerNotes

) {}
