package com.invision.web.Invision.dto;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
        int status,
        String code,
        String message,
        String path,
        LocalDateTime timestamp
) {
}
