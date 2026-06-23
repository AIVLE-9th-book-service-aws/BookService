package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.dto.CoverImageUpdateRequest;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCoverServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @InjectMocks
    private BookCoverImageService bookCoverImageService;

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("어린왕자");
        book.setAuthor("생텍쥐페리");
        book.setGenre("소설");
        book.replaceTags(List.of("모험", "우정"));
        book.setContent("사막에서 만난 어린 왕자 이야기");
        book.setLikes(0);
    }

    // ── saveImgUrl ──────────────────────────────────────────────

    @Test
    @DisplayName("표지 URL 저장 - 정상적으로 저장되는지 확인")
    void saveImgUrl_success() {
        String url = "https://example.com/cover.jpg";
        CoverImageUpdateRequest request = new CoverImageUpdateRequest();
        ReflectionTestUtils.setField(request, "coverImageUrl", url);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookService.saveImgUrl(1L, request);

        assertThat(result.getCoverImageUrl()).isEqualTo(url);
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("표지 URL 저장 - 없는 도서 ID면 예외")
    void saveImgUrl_bookNotFound() {
        CoverImageUpdateRequest request = new CoverImageUpdateRequest();
        ReflectionTestUtils.setField(request, "coverImageUrl", "https://example.com/cover.jpg");

        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.saveImgUrl(999L, request))
                .isInstanceOf(BookNotFoundException.class);

        verify(bookRepository, never()).save(any());
    }

    // ── saveCoverImageUrl ────────────────────────────────────────

    @Test
    @DisplayName("base64 이미지 저장 - 정상적으로 저장되는지 확인")
    void saveCoverImageUrl_success() {
        String dataUrl = "data:image/jpeg;base64,/9j/testdata";

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookCoverImageService.saveCoverImageUrl(1L, dataUrl);

        assertThat(result.getCoverImageUrl()).isEqualTo(dataUrl);
        assertThat(result.getCoverImageUrl()).startsWith("data:image/jpeg;base64,");
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("base64 이미지 저장 - 없는 도서 ID면 예외")
    void saveCoverImageUrl_bookNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookCoverImageService.saveCoverImageUrl(999L, "data:image/jpeg;base64,test"))
                .isInstanceOf(BookNotFoundException.class);

        verify(bookRepository, never()).save(any());
    }
}
