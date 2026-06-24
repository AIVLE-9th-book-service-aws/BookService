package com.aivle.bookapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String name;
    private String message;
    private String accessToken;
    private String refreshToken;

}