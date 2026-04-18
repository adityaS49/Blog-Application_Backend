package com.blogWebsite.BlogWebsite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;
    private List<CommentDto> replies; // For nested replies
}
