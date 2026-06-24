package com.aivle.bookapp.controller;

import com.aivle.bookapp.dto.CommentCreateRequest;
import com.aivle.bookapp.dto.CommentResponse;
import com.aivle.bookapp.dto.CommentUpdateRequest;
import com.aivle.bookapp.service.CommentService;
import com.aivle.bookapp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment API", description = "댓글 관련 API")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    // 댓글 등록
    @Operation(summary = "댓글 등록", description = "특정 도서에 댓글을 등록합니다.")
    @PostMapping("/books/{bookId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long bookId,
            @Valid @RequestBody CommentCreateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String email = extractEmail(authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.createComment(bookId, request, email));
    }

    // 도서 댓글 수정
    @Operation(summary = "댓글 수정", description = "특정 도서의 댓글을 수정합니다.")
    @PatchMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> commentUpdate(
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequest dto,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(commentService.commentUpdate(id, dto, email));
    }

    // 도서 댓글 삭제
    @Operation(summary = "댓글 삭제", description = "특정 도서의 댓글을 삭제합니다.")
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String password) {
        String email = extractEmail(authHeader);
        commentService.deleteComment(id, email, password);
        return ResponseEntity.noContent().build();
    }

    // 댓글 조회
    @Operation(summary = "댓글 조회", description = "특정 도서의 댓글을 조회합니다.")
    @GetMapping("/books/{bookId}/comments")
    public ResponseEntity<List<CommentResponse>> findComments(@PathVariable Long bookId) {
        return ResponseEntity.ok(commentService.findComments(bookId));
    }

    // 토큰에서 이메일 추출 (토큰 없으면 null)
    private String extractEmail(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                return jwtUtil.getEmail(authHeader.substring(7));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
