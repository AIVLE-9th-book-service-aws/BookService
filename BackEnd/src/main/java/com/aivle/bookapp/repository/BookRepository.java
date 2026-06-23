package com.aivle.bookapp.repository;

import com.aivle.bookapp.domain.Book;
import com.aivle.bookapp.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitle(String title);
    List<Book> findByAuthor(String author);
    List<Book> findByTitleContaining(String keyword);
    List<Book> findByTitleAndAuthor(String title, String author);

    List<Book> findByTitleContainingOrAuthorContaining(String keyword, String keyword1);

    @Modifying
    @Query("UPDATE Book b SET b.likes = b.likes + 1 WHERE b.id = :id")
    void incrementLikes(@Param("id") Long id);

    @Query("""
            select b
            from Book b
            where b.deletedAt is null
            order by b.likes desc, b.title asc
            """)
    Page<Book> findPopularBooks(Pageable pageable);

    @Query("""
            select b
            from Book b
            where b.deletedAt is null
              and (
                    :keyword = ''
                    or lower(b.title) like concat('%', :keyword, '%')
                    or lower(b.author) like concat('%', :keyword, '%')
                  )
              and (:genresEmpty = true or lower(b.genre) in :genres)
              and (
                    :tagsEmpty = true
                    or exists (
                        select 1
                        from BookTag bt
                        where bt.book = b
                          and lower(bt.name) in :tags
                    )
                  )
            order by b.title asc
            """)
    Page<Book> searchBooks(
            @Param("keyword") String keyword,
            @Param("genres") List<String> genres,
            @Param("genresEmpty") boolean genresEmpty,
            @Param("tags") List<String> tags,
            @Param("tagsEmpty") boolean tagsEmpty,
            Pageable pageable
    );

    List<Book> findAllByDeletedAtIsNotNull();

    List<Book> findByMemberIdAndDeletedAtIsNull(Long memeberId);

    Long member(Member member);
}
