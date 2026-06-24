package com.aivle.bookapp.controller;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.domain.Member;
import com.aivle.bookapp.dto.*;
import com.aivle.bookapp.service.BookService;
import com.aivle.bookapp.service.MemberService;
import com.aivle.bookapp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Book API", description = "도서 관련 API")
@RestController
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "도서 목록", description = "등록된 전체 도서를 조회합니다.")
    @GetMapping("/books")
    public List<BookResponse> findAll() {
        return bookService.findAll().stream().map(BookResponse::from).toList();
    }

    @Operation(summary = "도서 상세", description = "도서 ID를 이용해 상세 정보를 조회합니다.")
    @GetMapping("/books/{id}")
    public BookResponse findById(@PathVariable Long id) {
        return BookResponse.from(bookService.findById(id));
    }

    @Operation(summary = "신규 도서 등록", description = "신규 도서를 등록합니다.")
    @PostMapping("/books")
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookCreateRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.getEmail(authHeader.substring(7));
        Book saved = bookService.create(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.from(saved));
    }

    @Operation(summary = "도서 검색", description = "제목, 저자, 장르, 태그 조건으로 도서를 검색합니다.")
    @GetMapping("/books/search")
    public Page<BookSearchResponse> searchBooks(
            @ModelAttribute BookSearchRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        return bookService.search(request, pageable);
    }

    @Operation(summary = "인기 도서 조회", description = "인기 도서 5권을 조회합니다.")
    @GetMapping("/books/popular")
    public List<BookSearchResponse> getPopularBooks(@RequestParam(defaultValue = "5") int limit) {
        return bookService.getPopularBooks(limit);
    }

    @Operation(summary = "도서 삭제 목록", description = "삭제된 전체 도서를 조회합니다.")
    @GetMapping("/books/trash")
    public List<BookResponse> findAllDeletedBooks() {
        return bookService.findAllByDeleted().stream().map(BookResponse::from).toList();
    }

    @GetMapping("/books/page")
    public Page<BookResponse> getPage(@RequestParam int page, @RequestParam int size, @RequestParam String sortBy) {
        return bookService.getPage(page, size, sortBy).map(BookResponse::from);
    }

    @GetMapping("/books/count")
    public long getCount() {
        return bookService.count();
    }

    @Operation(summary = "도서 수정", description = "도서 정보를 수정합니다.")
    @PatchMapping("/books/{id}")
    public ResponseEntity<Map<String, Object>> updateBook(
            @PathVariable Long id,
            @RequestBody BookUpdateRequest dto,
            @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.getEmail(authHeader.substring(7));
        Book updatedBook = bookService.update(id, dto, email);
        Map<String, Object> body = Map.of(
                "id", updatedBook.getId(),
                "message", "도서 수정 성공"
        );
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @Operation(summary = "도서 삭제", description = "도서를 휴지통으로 이동합니다.")
    @PatchMapping("/books/trash/{id}")
    public ResponseEntity<Map<String, Object>> moveToTrash(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.getEmail(authHeader.substring(7));
        bookService.moveToTrash(id, email);
        Map<String, Object> body = Map.of("message", "도서 삭제 성공");
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @Operation(summary = "도서 복원", description = "휴지통에 있는 도서를 복원합니다.")
    @PatchMapping("/books/restore/{id}")
    public ResponseEntity<MessageResponse> restore(@PathVariable Long id) {
        bookService.restore(id);
        return ResponseEntity.ok(new MessageResponse("도서 복원 성공"));
    }

    @Operation(summary = "좋아요", description = "도서 좋아요 수를 증가시킵니다.")
    @PatchMapping("/books/{id}/like")
    public ResponseEntity<MessageResponse> likeBook(@PathVariable Long id) {
        bookService.likeBook(id);
        return ResponseEntity.ok(new MessageResponse("좋아요 성공"));
    }

    @Operation(summary = "AI 표지 생성", description = "OpenAI를 이용하여 표지 이미지를 생성합니다.")
    @PostMapping("/books/{id}/cover/generate")
    public ResponseEntity<GenerateCoverResponse> generateCover(@PathVariable Long id, @Valid @RequestBody GenerateCoverRequest request) {
        var updatedBook = bookService.generateCover(id, request);
        return ResponseEntity.ok(new GenerateCoverResponse(updatedBook.getId(), "표지 이미지 생성 성공", updatedBook.getCoverImageUrl()));
    }

    @Operation(summary = "AI 표지 이미지 저장", description = "생성된 AI 표지 이미지를 저장합니다.")
    @PatchMapping("/books/{id}/cover")
    public ResponseEntity<GenerateCoverResponse> saveImgUrl(@PathVariable Long id, @Valid @RequestBody CoverImageUpdateRequest request) {
        var updatedBook = bookService.saveImgUrl(id, request);
        return ResponseEntity.ok(new GenerateCoverResponse(updatedBook.getId(), "표지 이미지 저장 성공", updatedBook.getCoverImageUrl()));
    }

    @Operation(summary = "AI 한줄평 생성", description = "OpenAI를 이용하여 도서 한줄평을 생성합니다.")
    @PostMapping("/books/{id}/summary/generate")
    public ResponseEntity<AiBookSummaryResponse> generateSummary(
            @PathVariable Long id,
            @Valid @RequestBody AiBookSummaryRequest request) {
        return ResponseEntity.ok(bookService.generateSummary(id, request));
    }

    @Operation(summary = "AI 한줄평 저장", description = "OpenAI로 생성한 도서 한줄평을 저장합니다.")
    @PatchMapping("/books/{id}/summary")
    public ResponseEntity<BookResponse> saveSummary(@PathVariable Long id, @Valid @RequestBody SummaryUpdateRequest request) {
        return ResponseEntity.ok(BookResponse.from(bookService.saveSummary(id, request.summary())));
    }

    @Operation(summary = "도서 영구 삭제", description = "도서를 DB에서 완전히 삭제합니다.")
    @DeleteMapping("/books/{id}")
    public ResponseEntity<Map<String, Object>> deleteBook(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.getEmail(authHeader.substring(7));
        bookService.deleteBook(id, email);
        Map<String, Object> body = Map.of("message", "도서 영구 삭제 성공");
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @Operation(summary = "도서 수 통계", description = "도서 수를 통계냅니다.")
    @GetMapping("/books/statistics/count")
    public ResponseEntity<BookCountStatisticsResponse> getBookCountStatistics(
            @RequestParam(required = false) List<String> type) {
        var stats = bookService.getBookCountStatistics(type);
        return ResponseEntity.ok(new BookCountStatisticsResponse(
                (Map<String, Long>) stats.get("genre"),
                (Map<String, Long>) stats.get("tag")
        ));
    }

    @Operation(summary = "좋아요 수 통계", description = "좋아요 수를 통계냅니다.")
    @GetMapping("/books/statistics/likes")
    public ResponseEntity<LikesStatisticsResponse> getLikesCountStatistics(
            @RequestParam(required = false) List<String> type) {
        var stats = bookService.getLikesCountStatistics(type);
        return ResponseEntity.ok(new LikesStatisticsResponse(
                (Map<String, Integer>) stats.get("genre"),
                (Map<String, Integer>) stats.get("tag")
        ));
    }

    @Operation(summary = "내 도서 목록", description = "내가 등록한 도서 목록을 조회합니다.")
    @GetMapping("/books/my")
    public ResponseEntity<List<Book>> getMyBooks(
            @RequestHeader("Authorization") String authHeader) {
        String email = jwtUtil.getEmail(authHeader.substring(7));
        Member member = memberService.findByEmail(email);
        List<Book> books = bookService.myBooks(member.getId());
        return ResponseEntity.ok(books);
    }
}
