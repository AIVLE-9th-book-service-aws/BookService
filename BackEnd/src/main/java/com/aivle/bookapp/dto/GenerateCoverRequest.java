package com.aivle.bookapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GenerateCoverRequest {

    @Schema(description = "API Key")
    @NotBlank(message = "API Key는 필수입니다.")
    private String apiKey;

    @Schema(description = "품질")
    private String quality = "low";
}
