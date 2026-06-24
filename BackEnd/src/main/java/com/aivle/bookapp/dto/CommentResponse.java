package com.aivle.bookapp.dto;

import com.aivle.bookapp.domain.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        Long bookId,
        String author,
        String text,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getBookId(),
                comment.getAuthor(),
                comment.getText(),
                comment.getCreatedAt()
        );
    }
}
