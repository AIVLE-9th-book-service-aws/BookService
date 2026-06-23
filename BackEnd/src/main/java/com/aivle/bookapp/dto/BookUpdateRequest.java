package com.aivle.bookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BookUpdateRequest(

        @Schema(description = "도서 제목")
        String title,

        @Schema(description = "도서 저자")
        String author,

        @Schema(description = "장르")
        String genre,

        @Schema(description = "내용")
        String content,

        @Schema(description = "태그")
        String tag,

        @Schema(description = "AI 표지 이미지 URL")
        String coverImageUrl,

        @Schema(description = "AI 한줄평")
        String summary
) {}