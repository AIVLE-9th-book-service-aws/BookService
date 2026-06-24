package com.aivle.bookapp.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException(String email) {
        super("존재하지 않는 이메일입니다: " + email);
    }
}
