package com.aivle.bookapp.service;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.domain.Comment;
import com.aivle.bookapp.domain.Member;
import com.aivle.bookapp.dto.CommentCreateRequest;
import com.aivle.bookapp.dto.CommentResponse;
import com.aivle.bookapp.dto.CommentUpdateRequest;
import com.aivle.bookapp.exception.BookNotFoundException;
import com.aivle.bookapp.exception.CommentNotFoundException;
import com.aivle.bookapp.exception.MemberNotFoundException;
import com.aivle.bookapp.repository.BookRepository;
import com.aivle.bookapp.repository.CommentRepository;
import com.aivle.bookapp.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    // 후기 등록
    @Transactional
    public CommentResponse createComment(Long bookId, CommentCreateRequest request, String email) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        Comment comment = new Comment();
        comment.setBook(book);
        comment.setCreatedAt(java.time.LocalDateTime.now());

        if (email != null) {
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new MemberNotFoundException(email));
            comment.setMember(member);
            comment.setAuthor(member.getName());
            comment.setPassword(null);
        } else {
            comment.setAuthor(request.author() == null || request.author().isBlank() ? "익명" : request.author());
            comment.setPassword(request.password());
        }

        comment.setText(request.text());

        return CommentResponse.from(commentRepository.save(comment));
    }

    // 후기 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> findComments(Long bookId) {
        return commentRepository.findByBook_Id(bookId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(()
                -> new CommentNotFoundException(id));
    }

    // 도서 후기 수정
    @Transactional
    public CommentResponse commentUpdate(Long id, CommentUpdateRequest dto, String email) {
        Comment existing = findById(id);

        if (email != null) {
            if (existing.getMember() == null || !existing.getMember().getEmail().equals(email)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 댓글만 수정할 수 있습니다.");
            }
        } else {
            if (existing.getPassword() == null || !existing.getPassword().equals(dto.password())) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
            }
        }

        if (dto.text() != null) {
            existing.setText(dto.text());
        }

        return CommentResponse.from(commentRepository.save(existing));
    }

    // 도서 후기 삭제
    @Transactional
    public void deleteComment(Long id, String email, String password) {
        Comment existing = findById(id);

        if (email != null) {
            if (existing.getMember() == null || !existing.getMember().getEmail().equals(email)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 댓글만 삭제할 수 있습니다.");
            }
        } else {
            if (existing.getPassword() == null || !existing.getPassword().equals(password)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
            }
        }

        commentRepository.deleteById(id);
    }

    // 관리자 - 댓글 수정 (본인 확인 없음)
    @Transactional
    public CommentResponse adminCommentUpdate(Long id, CommentUpdateRequest dto) {
        Comment existing = findById(id);

        if (dto.text() != null) {
            existing.setText(dto.text());
        }

        return CommentResponse.from(commentRepository.save(existing));
    }

    // 관리자 - 댓글 삭제 (본인 확인 없음)
    @Transactional
    public void adminDeleteComment(Long id) {
        findById(id);
        commentRepository.deleteById(id);
    }
}