package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.domain.BookTag;
import com.aivle.bookapp.domain.Member;
import com.aivle.bookapp.dto.*;
import com.aivle.bookapp.dto.AiBookSummaryRequest;
import com.aivle.bookapp.dto.AiBookSummaryResponse;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.exception.MemberNotFoundException;
import com.aivle.bookapp.exception.OpenAiException;
import com.aivle.bookapp.repository.BookRepository;
import com.aivle.bookapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final int DEFAULT_POPULAR_LIMIT = 5;
    private static final int MAX_POPULAR_LIMIT = 50;

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BookCoverImageService bookCoverImageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 도서 상세 조회
    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    // 도서 목록 조회
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    // 도서 삭제 목록 조회
    @Transactional(readOnly = true)
    public List<Book> findAllByDeleted() {
        return bookRepository.findAllByDeletedAtIsNotNull();
    }

    // 도서 등록 + like 0 기본값 추가
    @Transactional
    public Book create(BookCreateRequest request, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException(email));

        Book book = new Book();
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setGenre(request.genre());
        book.setContent(request.content());
        book.replaceTags(normalizeTagNames(request.tag()));
        book.setCoverImageUrl(request.coverImageUrl());
        book.setLikes(0);
        book.setMember(member); // 등록자 저장

        return bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public Page<Book> getPage(int page, int size, String sortBy) {
        Sort sort = Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return bookRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public long count() {
        return bookRepository.count();
    }

    // 도서 수정
    @Transactional
    public Book update(Long id, BookUpdateRequest dto, String email) {
        Book existing = findById(id);
        checkOwner(existing, email); // 본인 확인

        if (existing.getDeletedAt() != null ) {
            throw new IllegalArgumentException("휴지통에 있는 도서는 수정할 수 없습니다.");
        }

        if (dto.title() != null) {
            if (dto.title().isBlank()) {
                throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
            }
            existing.setTitle(dto.title());
        }
        if (dto.author() != null) {
            if (dto.author().isBlank()) {
                throw new IllegalArgumentException("저자명은 비워둘 수 없습니다.");
            }
            existing.setAuthor(dto.author());
        }
        if (dto.genre() != null) {
            if (dto.genre().isBlank()) {
                throw new IllegalArgumentException("장르는 비워둘 수 없습니다.");
            }
            existing.setGenre(dto.genre());
        }
        if (dto.content() != null) {
            existing.setContent(dto.content());
        }
        if (dto.tag() != null) {
            existing.replaceTags(normalizeTagNames(dto.tag()));
        }
        if (dto.coverImageUrl() != null) {
            existing.setCoverImageUrl(dto.coverImageUrl());
        }
        if (dto.summary() != null) {
            existing.setSummary(dto.summary());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        return bookRepository.save(existing);
    }

    // 도서 휴지통 이동
    @Transactional
    public Book moveToTrash(Long id, String email) {
        Book existing = findById(id);
        checkOwner(existing, email); // 본인 확인
        if (existing.getDeletedAt() != null) {
            throw new IllegalArgumentException("이미 휴지통에 있는 도서입니다.");
        }
        existing.setDeletedAt(LocalDateTime.now());
        existing.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(existing);
    }

    // 도서 휴지통 복원
    @Transactional
    public Book restore(Long id) {
        Book existing = findById(id);
        existing.setDeletedAt(null);
        existing.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(existing);
    }

    // AI 표지 이미지 저장
    @Transactional
    public Book saveImgUrl(Long id, CoverImageUpdateRequest request) {
        Book existing = findById(id);
        existing.setCoverImageUrl(request.getCoverImageUrl());
        return bookRepository.save(existing);
    }

    // 이미지 저장
    @Transactional
    public Book saveCoverImage(Long id, String coverImageUrl) {
        Book existing = findById(id);
        existing.setCoverImageUrl(coverImageUrl);
        return bookRepository.save(existing);
    }

    // 좋아요 +1
    @Transactional
    public void likeBook(Long id) {
        Book existing = findById(id);
        if (existing.getDeletedAt() != null) {
            throw new IllegalArgumentException("삭제된 도서에는 좋아요를 누를 수 없습니다.");
        }
        bookRepository.incrementLikes(id);
    }

    // 한줄평 저장
    @Transactional
    public Book saveSummary(Long id, String summary) {
        Book existing = findById(id);
        existing.setSummary(summary);
        return bookRepository.save(existing);
    }

    // 도서 영구 삭제
    @Transactional
    public void deleteBook(Long id, String email) {
        Book existing = findById(id);
        checkOwner(existing, email); // 본인 확인
        bookRepository.deleteById(id);
    }

    // 본인 도서 확인
    private void checkOwner(Book book, String email) {
        if (book.getMember() == null) return; // 기존 데이터 null이면 통과
        if (!book.getMember().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 도서만 수정/삭제할 수 있습니다.");
        }
    }

    // 삭제되지 않은 도서만 가져오기
    private List<Book> getActiveBooks() {
        return bookRepository.findAll()
                .stream()
                .filter(book -> book.getDeletedAt() == null)
                .toList();
    }

    // 도서 수 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getBookCountStatistics(List<String> type) {
        if (type == null || type.isEmpty()) {
            return Map.of(
                    "genre", getBookCountByGenre(),
                    "tag", getBookCountByTag()
            );
        }

        Map<String, Object> result = new HashMap<>();

        for (String t : type) {
            switch (t) {
                case "genre" -> result.put("genre", getBookCountByGenre());
                case "tag" -> result.put("tag", getBookCountByTag());
                default -> throw new IllegalArgumentException("type은 genre 또는 tag만 가능합니다.");
            }
        }

        return result;
    }

    // 장르별 도서 수 합계
    private Map<String, Long> getBookCountByGenre() {
        Map<String, Long> result = new HashMap<>();

        for (Book book : getActiveBooks()) {
            String genre = book.getGenre() != null ? book.getGenre() : "기타";
            result.put(genre, result.getOrDefault(genre, 0L) + 1);
        }

        return result;
    }

    // 태그별 도서 수 합계
    private Map<String, Long> getBookCountByTag() {
        Map<String, Long> result = new HashMap<>();

        for (Book book : getActiveBooks()) {
            for (BookTag tag : book.getTags()) {
                String tagName = tag.getName();
                if (tagName == null || tagName.isBlank()) continue;
                result.put(tagName, result.getOrDefault(tagName, 0L) + 1);
            }
        }

        return result;
    }

    // 좋아요 수 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getLikesCountStatistics(List<String> type) {
        if (type == null || type.isEmpty()) {
            return Map.of(
                    "genre", getLikesCountByGenre(),
                    "tag", getLikesCountByTag()
            );
        }

        Map<String, Object> result = new HashMap<>();

        for (String t : type) {
            switch (t) {
                case "genre" -> result.put("genre", getLikesCountByGenre());
                case "tag" -> result.put("tag", getLikesCountByTag());
                default -> throw new IllegalArgumentException("type은 genre 또는 tag만 가능합니다.");
            }
        }

        return result;
    }

    // 장르별 좋아요 수 합계
    private Map<String, Integer> getLikesCountByGenre() {
        Map<String, Integer> result = new HashMap<>();

        for (Book book : getActiveBooks()) {
            String genre = book.getGenre() != null ? book.getGenre() : "기타";
            int likes = book.getLikes() != null ? book.getLikes() : 0;

            result.put(genre, result.getOrDefault(genre, 0) + likes);
        }

        return result;
    }

    // 태그별 좋아요 수 합계
    private Map<String, Integer> getLikesCountByTag() {
        Map<String, Integer> result = new HashMap<>();

        for (Book book : getActiveBooks()) {
            int likes = book.getLikes() != null ? book.getLikes() : 0;
            for (BookTag tag : book.getTags()) {
                String tagName = tag.getName();
                if (tagName == null || tagName.isBlank()) continue;
                result.put(tagName, result.getOrDefault(tagName, 0) + likes);
            }
        }

        return result;
    }

    // AI 표지 이미지 생성 (OpenAI 호출 → 압축 → DB 저장)
    public Book generateCover(Long id, GenerateCoverRequest request) {
        Book book = findById(id);
        String prompt = buildPrompt(book);
        String b64Json = callOpenAi(prompt, request.getApiKey(), request.getQuality());
        String dataUrl = compressImage(b64Json);
        return bookCoverImageService.saveCoverImageUrl(id, dataUrl);
    }


    private String buildPrompt(Book book) {
        return String.format("""
                A high-quality, professional book cover design for a book titled "%s" by %s.
                Genre: %s. Tags: %s.
                Visual concept based on the story: %s
                ---
                Visual direction:
                - Genre-specific mood: %s
                - Composition: cinematic, full-bleed illustration with strong focal point
                - Color palette: rich and thematic, matching the genre and emotional tone
                - Lighting: dramatic and atmospheric
                ---
                Text layout:
                - Title "%s" displayed prominently at the top in elegant, genre-appropriate font
                - Author name "%s" at the bottom in smaller clean serif font
                - Text should be clearly legible and well-integrated into the design
                ---
                Style: professional publishing quality, award-winning book cover art
                """,
                book.getTitle(), book.getAuthor(), book.getGenre(), book.getTagText(),
                book.getContent(), getGenreMood(book.getGenre()),
                book.getTitle(), book.getAuthor()
        ).trim();
    }

    private String getGenreMood(String genre) {
        Map<String, String> moods = new HashMap<>();
        moods.put("소설",   "literary fiction, emotional depth, human drama, warm and cinematic tones");
        moods.put("고전",   "timeless elegance, vintage aesthetic, aged paper texture, classical art style");
        moods.put("역사",   "historical epic, aged maps, sepia tones, dramatic lighting, period setting");
        moods.put("IT",    "digital technology, circuit patterns, futuristic interface, clean modern design");
        moods.put("동화",   "whimsical illustration, soft pastel colors, magical fairy tale world, children friendly");
        moods.put("자기계발", "motivational, bright and energetic, clean minimalist design, uplifting mood");
        moods.put("과학",   "scientific discovery, cosmos, molecular structures, clean and precise illustration");
        moods.put("경제",   "professional, financial charts, bold typography, sleek corporate design");
        moods.put("철학",   "deep and contemplative, abstract symbolism, dark and moody, thought-provoking");
        moods.put("예술",   "creative expression, vibrant colors, artistic brushstrokes, gallery-worthy aesthetic");
        return moods.getOrDefault(genre, "artistic, expressive, visually compelling");
    }

    @SuppressWarnings("unchecked")
    private String callOpenAi(String prompt, String apiKey, String quality) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-image-2");
            body.put("prompt", prompt);
            body.put("n", 1);
            body.put("size", "1024x1536");
            body.put("quality", quality != null ? quality : "low");
            body.put("output_format", "png");

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(30))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/images/generations"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(120))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) throw OpenAiException.from(response.statusCode(), response.body());

            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");

            if (data == null || data.isEmpty()) throw OpenAiException.missingData();

            String b64Json = (String) data.get(0).get("b64_json");
            if (b64Json == null) throw OpenAiException.missingImage();
            return b64Json;

        } catch (OpenAiException e) {
            throw e;
        } catch (HttpTimeoutException e) {
            throw OpenAiException.timeout();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw OpenAiException.interrupted();
        } catch (Exception e) {
            throw OpenAiException.callFailed(e);
        }
    }

    private String compressImage(String b64Json) {
        try {
            byte[] pngBytes = Base64.getDecoder().decode(b64Json);
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(pngBytes));

            int maxWidth = 400;
            double scale = Math.min(1.0, (double) maxWidth / original.getWidth());
            int w = (int) (original.getWidth() * scale);
            int h = (int) (original.getHeight() * scale);

            BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.drawImage(original.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);
            g.dispose();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(0.5f);
            ImageOutputStream ios = ImageIO.createImageOutputStream(out);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(resized, null, null), params);
            writer.dispose();

            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            return "data:image/png;base64," + b64Json;
        }
    }

    // 도서 검색
    @Transactional(readOnly = true)
    public Page<BookSearchResponse> search(BookSearchRequest request, Pageable pageable) {
        List<String> genres = normalizeSearchValues(request.genres());
        List<String> tags = normalizeSearchValues(request.tags());

        return bookRepository.searchBooks(
                normalizeSearchValue(request.keyword()),
                emptyListGuard(genres), genres.isEmpty(),
                emptyListGuard(tags), tags.isEmpty(),
                pageable
        ).map(BookSearchResponse::from);
    }

    // 인기 도서 조회
    @Transactional(readOnly = true)
    public List<BookSearchResponse> getPopularBooks(int limit) {
        Pageable pageable = PageRequest.of(0, normalizeLimit(limit));
        return bookRepository.findPopularBooks(pageable)
                .stream()
                .map(BookSearchResponse::from)
                .toList();
    }

    private int normalizeLimit(int limit) {
        // 인기 도서 조회 개수를 기본값과 최대 허용값 사이로 제한
        if (limit <= 0) return DEFAULT_POPULAR_LIMIT;
        return Math.min(limit, MAX_POPULAR_LIMIT);
    }

    private String normalizeSearchValue(String value) {
        // 검색 조건 비교를 위해 null, 앞뒤 공백, 대소문자 차이를 정리
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private List<String> normalizeSearchValues(List<String> values) {
        // 다중 쿼리 파라미터와 콤마로 묶인 값을 모두 동일한 리스트 조건으로 정리
        if (values == null) return List.of();

        return values.stream()
                .flatMap(value -> List.of(value.split(",")).stream())
                .map(this::normalizeSearchValue)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
    }

    private List<String> emptyListGuard(List<String> values) {
        // JPQL의 IN 조건에 빈 리스트가 들어가지 않도록 더미 값을 넣어 쿼리 오류를 방지
        return values.isEmpty() ? List.of("__empty__") : values;
    }

    private List<String> normalizeTagNames(String value) {
        if (value == null) return List.of();

        return Arrays.stream(value.split(","))  // [,/] → ,
                .map(String::trim)
                .filter(tagName -> !tagName.isBlank())
                .distinct()
                .toList();
    }

    public AiBookSummaryResponse generateSummary(Long id, AiBookSummaryRequest request) {
        Book book = findById(id);
        String summary = callOpenAiApi(book.getContent(), request.apiKey());
        return new AiBookSummaryResponse(summary);
    }

    // 관리자 - 도서 수정 (소유자 체크 없음)
    @Transactional
    public Book adminUpdate(Long id, BookUpdateRequest dto) {
        Book existing = findById(id);

        if (dto.title() != null) {
            if (dto.title().isBlank()) throw new IllegalArgumentException("제목은 비워둘 수 없습니다.");
            existing.setTitle(dto.title());
        }
        if (dto.author() != null) {
            if (dto.author().isBlank()) throw new IllegalArgumentException("저자명은 비워둘 수 없습니다.");
            existing.setAuthor(dto.author());
        }
        if (dto.genre() != null) {
            if (dto.genre().isBlank()) throw new IllegalArgumentException("장르는 비워둘 수 없습니다.");
            existing.setGenre(dto.genre());
        }
        if (dto.content() != null) existing.setContent(dto.content());
        if (dto.tag() != null) existing.replaceTags(normalizeTagNames(dto.tag()));
        if (dto.coverImageUrl() != null) existing.setCoverImageUrl(dto.coverImageUrl());
        if (dto.summary() != null) existing.setSummary(dto.summary());
        existing.setUpdatedAt(LocalDateTime.now());

        return bookRepository.save(existing);
    }

    // 관리자 - 도서 삭제 (소유자 체크 없음)
    @Transactional
    public void adminDelete(Long id) {
        findById(id); // 존재 여부만 확인
        bookRepository.deleteById(id);
    }

    private String callOpenAiApi(String content, String apiKey) {
        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey.trim());
            conn.setDoOutput(true);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "system", "content", "주어진 도서 내용을 바탕으로 한 문장의 한줄평을 작성하세요."),
                            Map.of("role", "user", "content", content))
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(objectMapper.writeValueAsBytes(body));
            }
            int responseCode = conn.getResponseCode();
            if (responseCode == 401) throw new OpenAiException(401, "API Key가 올바르지 않습니다.");
            if (responseCode == 429) throw new OpenAiException(429, "요청 한도 초과. 잠시 후 다시 시도해주세요.");
            if (responseCode != 200) throw new OpenAiException(responseCode, "OpenAI 오류: " + responseCode);
            JsonNode root = objectMapper.readTree(conn.getInputStream());
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (OpenAiException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenAiException(500, "OpenAI API 호출 실패: " + e.getMessage());
        }
    }

    @Transactional
    public List<Book> myBooks(Long memberId){
        return bookRepository.findByMemberIdAndDeletedAtIsNull(memberId);
    }
}
