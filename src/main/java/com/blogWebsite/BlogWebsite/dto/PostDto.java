package com.blogWebsite.BlogWebsite.dto;

import com.blogWebsite.BlogWebsite.entity.PostStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    private Long id;

    private String title;

    private String description;       // SEO / meta description

    private String thumbnailUrl;      // Cover image URL

    private String content;           // Markdown/HTML content

    private String slug;              // SEO-friendly URL

    private PostStatus status;        // DRAFT, PUBLISHED, SCHEDULED

    private String authorEmail;       // Post author's email

    private Set<String> tags;         // Names of tags

    private Long views;               // View count

    private Long likesCount;          // Like count

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
