package com.aivle.bookapp.controller;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.exception.GlobalExceptionHandler;
import com.aivle.bookapp.exception.OpenAiException;
import com.aivle.bookapp.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookCoverControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private Book mockBook;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockBook = new Book();
        mockBook.setId(1L);
        mockBook.setTitle("어린왕자");
        mockBook.setAuthor("생텍쥐페리");
        mockBook.setGenre("소설");
        mockBook.replaceTags(List.of("모험", "우정"));
        mockBook.setContent("사막에서 만난 어린 왕자 이야기");
        mockBook.setCoverImageUrl("data:image/jpeg;base64,/9j/testdata");
        mockBook.setLikes(0);
    }

    // ── generateCover ──────────────────────────────────────────

    @Test
    @DisplayName("AI 표지 생성 - 정상 (200)")
    void generateCover_success() throws Exception {
        when(bookService.generateCover(eq(1L), any())).thenReturn(mockBook);

        mockMvc.perform(post("/books/1/cover/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "apiKey": "sk-test123", "quality": "low" }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.message").value("표지 이미지 생성 성공"))
                .andExpect(jsonPath("$.coverImageUrl").exists());
    }

    @Test
    @DisplayName("AI 표지 생성 - apiKey 없으면 400")
    void generateCover_missingApiKey() throws Exception {
        mockMvc.perform(post("/books/1/cover/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "quality": "low" }
                                """))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("API Key는 필수입니다."));
    }

    @Test
    @DisplayName("AI 표지 생성 - 존재하지 않는 도서면 404")
    void generateCover_bookNotFound() throws Exception {
        when(bookService.generateCover(eq(999L), any()))
                .thenThrow(new BookNotFoundException(999L));

        mockMvc.perform(post("/books/999/cover/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "apiKey": "sk-test123", "quality": "low" }
                                """))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book not found"));
    }

    @Test
    @DisplayName("AI 표지 생성 - API Key 오류면 401")
    void generateCover_invalidApiKey() throws Exception {
        when(bookService.generateCover(eq(1L), any()))
                .thenThrow(new OpenAiException(401, "API Key가 올바르지 않습니다."));

        mockMvc.perform(post("/books/1/cover/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "apiKey": "sk-wrong", "quality": "low" }
                                """))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("OpenAI Error"))
                .andExpect(jsonPath("$.message").value("API Key가 올바르지 않습니다."));
    }

    @Test
    @DisplayName("AI 표지 생성 - 요청 한도 초과면 429")
    void generateCover_rateLimitExceeded() throws Exception {
        when(bookService.generateCover(eq(1L), any()))
                .thenThrow(new OpenAiException(429, "요청 한도 초과. 잠시 후 다시 시도해주세요."));

        mockMvc.perform(post("/books/1/cover/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "apiKey": "sk-test123", "quality": "low" }
                                """))
                .andDo(print())
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("OpenAI Error"));
    }

    // ── saveImgUrl ──────────────────────────────────────────────

    @Test
    @DisplayName("표지 URL 저장 - 정상 (200)")
    void saveImgUrl_success() throws Exception {
        when(bookService.saveImgUrl(eq(1L), any())).thenReturn(mockBook);

        mockMvc.perform(patch("/books/1/cover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "coverImageUrl": "https://example.com/cover.jpg" }
                                """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.message").value("표지 이미지 저장 성공"))
                .andExpect(jsonPath("$.coverImageUrl").exists());
    }

    @Test
    @DisplayName("표지 URL 저장 - coverImageUrl 없으면 400")
    void saveImgUrl_missingUrl() throws Exception {
        mockMvc.perform(patch("/books/1/cover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("coverImageUrl은 필수입니다."));
    }

    @Test
    @DisplayName("표지 URL 저장 - 존재하지 않는 도서면 404")
    void saveImgUrl_bookNotFound() throws Exception {
        when(bookService.saveImgUrl(eq(999L), any()))
                .thenThrow(new BookNotFoundException(999L));

        mockMvc.perform(patch("/books/999/cover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "coverImageUrl": "https://example.com/cover.jpg" }
                                """))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Book not found"));
    }
}
