package com.aivle.bookapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenerateCoverResponse {
    private Long id;
    private String message;
    private String coverImageUrl;
}
