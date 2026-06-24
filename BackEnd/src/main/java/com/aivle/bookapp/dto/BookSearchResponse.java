package com.aivle.bookapp.dto;

import com.aivle.bookapp.domain.Book;

public record BookSearchResponse(
        Long id,
        String title,
        String author,
        String genre,
        String tag,
        String summary,
        String coverImageUrl,
        int likes
) {
    public static BookSearchResponse from(Book book) {
        return new BookSearchResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getTagText(),
                book.getSummary(),
                book.getCoverImageUrl(),
                book.getLikes()
        );
    }
}
