package com.aivle.bookapp.dto;

import com.aivle.bookapp.domain.Book;
import java.time.LocalDateTime;

public record BookResponse(
        Long id,
        String title,
        String author,
        String genre,
        String content,
        String summary,
        String coverImageUrl,
        int likes,
        String tag,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime deletedAt
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getContent(),
                book.getSummary(),
                book.getCoverImageUrl(),
                book.getLikes(),
                book.getTagText(),
                book.getCreatedAt(),
                book.getUpdatedAt(),
                book.getDeletedAt()
        );
    }
}
