package com.blogWebsite.BlogWebsite.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="likes", uniqueConstraints = @UniqueConstraint( columnNames= {"post_id","user_id"}))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Like {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private Post post;
    @ManyToOne
    private User user;
}
