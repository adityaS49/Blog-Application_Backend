package com.blogWebsite.BlogWebsite.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Post post;
    @ManyToOne
    private User user;
    @ManyToOne
    private Comment parent;
    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime createdAt = LocalDateTime.now();
}
