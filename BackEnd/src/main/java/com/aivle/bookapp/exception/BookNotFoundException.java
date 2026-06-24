package com.aivle.bookapp.exception;

public class BookNotFoundException extends RuntimeException{

    public BookNotFoundException(Long id){
        super(id+"번의 책이 존재하지 않습니다.");
    }
}
