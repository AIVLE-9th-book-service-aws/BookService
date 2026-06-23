package com.aivle.bookapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LikesStatisticsResponse(
        Map<String, Integer> genre,
        Map<String, Integer> tag
) {}
