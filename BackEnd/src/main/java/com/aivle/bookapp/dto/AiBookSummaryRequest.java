package com.aivle.bookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record AiBookSummaryRequest(

        @Schema(description = "API Key")
        @NotBlank(message = "API Key는 필수입니다.")
        String apiKey
) {
}