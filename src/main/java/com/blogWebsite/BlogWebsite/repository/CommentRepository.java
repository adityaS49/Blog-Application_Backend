// src/main/java/com/blogWebsite/BlogWebsite/repository/CommentRepository.java
package com.blogWebsite.BlogWebsite.repository;

import com.blogWebsite.BlogWebsite.entity.Comment;
import com.blogWebsite.BlogWebsite.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Find all comments for a given post
    List<Comment> findByPost(Post post);

    // Optional: find top-level comments (without parent) for a post
    List<Comment> findByPostAndParentIsNull(Post post);
}
