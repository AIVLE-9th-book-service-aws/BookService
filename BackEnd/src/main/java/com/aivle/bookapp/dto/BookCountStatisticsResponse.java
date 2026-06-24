package com.aivle.bookapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookCountStatisticsResponse(
        Map<String, Long> genre,
        Map<String, Long> tag
) {}
