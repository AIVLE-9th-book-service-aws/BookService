package com.aivle.bookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record SummaryUpdateRequest(
        @Schema(description = "AI 한줄평")
        @NotBlank(message = "한줄평은 비워둘 수 없습니다.")
        String summary
) {}
