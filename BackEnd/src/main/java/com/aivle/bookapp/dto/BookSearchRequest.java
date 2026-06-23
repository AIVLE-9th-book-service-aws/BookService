package com.aivle.bookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record BookSearchRequest(

        @Schema(description = "검색어")
        String keyword,

        @Schema(description = "장르")
        List<String> genres,

        @Schema(description = "태그")
        List<String> tags
) {
}
