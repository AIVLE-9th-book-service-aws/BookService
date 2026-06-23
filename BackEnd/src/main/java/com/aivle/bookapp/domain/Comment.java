package com.aivle.bookapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private String author = "익명";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column
    private String password;

    @Column
    private java.time.LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    @JsonProperty("bookId")
    public Long getBookId() {
        return book == null ? null : book.getId();
    }

    @JsonProperty("memberId")
    public Long getMemberId() {
        return member == null ? null : member.getId();
    }
}