package com.blogWebsite.BlogWebsite.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long parentId; // optional
}
