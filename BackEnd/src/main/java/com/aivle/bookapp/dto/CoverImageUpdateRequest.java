package com.aivle.bookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CoverImageUpdateRequest {

    @Schema(description = "AI 표지 이미지 URL")
    @NotBlank(message = "coverImageUrl은 필수입니다.")
    private String coverImageUrl;
}
