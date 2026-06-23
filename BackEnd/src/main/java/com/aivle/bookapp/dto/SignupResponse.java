package com.aivle.bookapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponse {

    private Long id;
    private String name;
    private String email;
    private String message;

}
