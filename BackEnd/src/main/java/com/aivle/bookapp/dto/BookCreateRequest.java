package com.aivle.bookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BookCreateRequest(
  
        @Schema(description = "도서 제목")
        @NotBlank(message = "제목은 필수입니다.")
        String title,

        @Schema(description = "도서 저자")
        @NotBlank(message = "저자명은 필수입니다.")
        String author,

        @Schema(description = "장르")
        @NotBlank(message = "장르는 필수입니다.")
        String genre,

        @Schema(description = "내용")
        String content,

        @Schema(description = "태그")
        String tag,

        @Schema(description = "AI 표지 이미지 URL")
        String coverImageUrl
) {
}