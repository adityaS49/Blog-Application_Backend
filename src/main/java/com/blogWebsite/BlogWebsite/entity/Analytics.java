package com.blogWebsite.BlogWebsite.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name="analytics")
@Data
public class Analytics {
    @Id
    private Long postId;
    private long viewsCount;
    private LocalDateTime lastViewedAt;
}

