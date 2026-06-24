package com.aivle.bookapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "제목은 필수입니다.")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "저자는 필수입니다.")
    @Column(nullable = false)
    private String author;

    @NotBlank(message = "장르는 필수입니다.")
    @Column(nullable = false)
    private String genre;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(nullable = false)
    private Integer likes = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private java.time.LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private java.time.LocalDateTime updatedAt;

    @Column
    private java.time.LocalDateTime deletedAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookTag> tags = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = true)
    private Member member;

    public void replaceTags(List<String> tagNames) {
        tags.clear();

        if (tagNames == null) {
            return;
        }

        tagNames.stream()
                .filter(tagName -> tagName != null && !tagName.isBlank())
                .map(String::trim)
                .distinct()
                .map(tagName -> new BookTag(this, tagName))
                .forEach(tags::add);
    }

    public String getTagText() {
        return tags.stream()
                .map(BookTag::getName)
                .filter(tagName -> tagName != null && !tagName.isBlank())
                .collect(Collectors.joining(","));
    }
}