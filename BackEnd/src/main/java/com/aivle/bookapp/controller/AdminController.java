package com.aivle.bookapp.controller;

import com.aivle.bookapp.dto.BookUpdateRequest;
import com.aivle.bookapp.dto.CommentUpdateRequest;
import com.aivle.bookapp.service.BookService;
import com.aivle.bookapp.service.CommentService;
import com.aivle.bookapp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin API", description = "관리자 관련 API")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final JwtUtil jwtUtil;
    private final BookService bookService;
    private final CommentService commentService;

    @Schema(description = "관리자 이름")
    @Value("${admin.username}")
    private String adminUsername;

    @Schema(description = "관리자 비밀번호")
    @Value("${admin.password}")
    private String adminPassword;

    @Operation(summary = "관리자 로그인", description = "이름과 비밀번호를 입력하여 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            return ResponseEntity.status(401).body("관리자 인증 실패");
        }

        String token = jwtUtil.generateAccessToken(username, "ADMIN");
        return ResponseEntity.ok(Map.of("accessToken", token));
    }

    @Operation(summary = "관리자 도서 수정", description = "관리자 권한을 가진 사용자가 도서 정보를 수정합니다.")
    @PatchMapping("/books/{id}")
    public ResponseEntity<?> updateBook(
            @PathVariable Long id,
            @RequestBody BookUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        if (!"ADMIN".equals(jwtUtil.extractRole(token))) {
            return ResponseEntity.status(403).body("권한 없음");
        }

        return ResponseEntity.ok(bookService.adminUpdate(id, request));
    }

    @Operation(summary = "관리자 도서 삭제", description = "관리자 권한을 가진 사용자가 도서를 삭제합니다.")
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        if (!"ADMIN".equals(jwtUtil.extractRole(token))) {
            return ResponseEntity.status(403).body("권한 없음");
        }

        bookService.adminDelete(id);
        return ResponseEntity.ok("삭제 완료");
    }

    @Operation(summary = "관리자 댓글 수정", description = "관리자 권한을 가진 사용자가 댓글을 수정합니다.")
    @PatchMapping("/comments/{id}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id,
            @RequestBody CommentUpdateRequest request,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        if (!"ADMIN".equals(jwtUtil.extractRole(token))) {
            return ResponseEntity.status(403).body("권한 없음");
        }

        return ResponseEntity.ok(commentService.adminCommentUpdate(id, request));
    }

    @Operation(summary = "관리자 댓글 삭제", description = "관리자 권한을 가진 사용자가 댓글을 삭제합니다.")
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        if (!"ADMIN".equals(jwtUtil.extractRole(token))) {
            return ResponseEntity.status(403).body("권한 없음");
        }

        commentService.adminDeleteComment(id);
        return ResponseEntity.ok("댓글 삭제 완료");
    }
}