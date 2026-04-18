package com.blogWebsite.BlogWebsite.repository;

import com.blogWebsite.BlogWebsite.entity.Post;
import com.blogWebsite.BlogWebsite.entity.PostStatus;
import com.blogWebsite.BlogWebsite.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
    Optional<Post> findBySlug(String slug);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);
    List<Post> findAllByOrderByCreatedAtDesc();
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.name = :tag")
    Page<Post> findByTag(@Param("tags") String tag, Pageable pageable);
}
