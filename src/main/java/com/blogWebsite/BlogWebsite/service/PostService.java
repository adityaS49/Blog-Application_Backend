package com.blogWebsite.BlogWebsite.service;

import com.blogWebsite.BlogWebsite.dto.CommentDto;
import com.blogWebsite.BlogWebsite.dto.PostDto;
import com.blogWebsite.BlogWebsite.entity.*;
import com.blogWebsite.BlogWebsite.repository.CommentRepository;
import com.blogWebsite.BlogWebsite.repository.PostRepository;
import com.blogWebsite.BlogWebsite.repository.TagRepository;
import com.blogWebsite.BlogWebsite.repository.UserRepository;
import com.blogWebsite.BlogWebsite.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final CommentRepository commentRepository;


    public List<PostDto> getAllPosts() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    public List<PostDto> getAllPostsByUserId(Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Post> posts = postRepository.findByAuthorOrderByCreatedAtDesc(author);
        return posts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    /** CREATE POST **/
    public PostDto create(PostDto dto, String username) {
        User author = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .description(dto.getDescription())
                .thumbnailUrl(dto.getThumbnailUrl())
                .author(author)
                .status(dto.getStatus() != null ? dto.getStatus() : PostStatus.DRAFT)
                .slug(SlugUtil.toSlug(dto.getTitle()))
                .tags(getOrCreateTags(dto.getTags()))
                .createdAt(LocalDateTime.now())      // ✅ Add this
                .updatedAt(LocalDateTime.now())      // optional, good practice
                .build();


        postRepository.save(post);
        return mapToDto(post);
    }

    /** UPDATE POST **/
    public PostDto update(Long postId, PostDto dto, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!post.getAuthor().getEmail().equals(username)) {
            throw new RuntimeException("You are not the author of this post");
        }

        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setDescription(dto.getDescription());
        post.setThumbnailUrl(dto.getThumbnailUrl());
        post.setStatus(dto.getStatus());
        post.setSlug(SlugUtil.toSlug(dto.getTitle()));
        post.setTags(getOrCreateTags(dto.getTags()));

        postRepository.save(post);
        return mapToDto(post);
    }

    /** DELETE POST **/
    public void delete(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getAuthor().getEmail().equals(username)) {
            throw new RuntimeException("You are not the author of this post");
        }
        postRepository.delete(post);
    }

    /** GET POST BY SLUG **/
    public PostDto getBySlug(String slug) {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return mapToDto(post);
    }

    /** LIST POSTS WITH OPTIONAL TAG AND PAGINATION **/
    public Page<PostDto> list(int page, int size, String tag) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Post> posts;
        if (tag != null && !tag.isEmpty()) {
            posts = postRepository.findByTag(tag, pageRequest);
        } else {
            posts = postRepository.findByStatus(PostStatus.PUBLISHED, pageRequest);
        }
        return posts.map(this::mapToDto);
    }

    /** LIKE / UNLIKE POST **/
    @Transactional
    public void toggleLike(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean liked = post.getLikes().removeIf(like -> like.getUser().getId().equals(user.getId()));
        if (!liked) {
            post.getLikes().add(new Like(null, post, user));
        }
        post.setLikesCount((long) post.getLikes().size());
        postRepository.save(post);
    }

    /** HELPER: GET OR CREATE TAGS **/
    private Set<Tag> getOrCreateTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames != null) {
            for (String name : tagNames) {
                Tag tag = tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(null, name)));
                tags.add(tag);
            }
        }
        return tags;
    }

    /** HELPER: MAP ENTITY TO DTO **/
    private PostDto mapToDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .description(post.getDescription())
                .thumbnailUrl(post.getThumbnailUrl())
                .slug(post.getSlug())
                .status(post.getStatus())
                .authorEmail(post.getAuthor().getEmail())
                .tags(post.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .views(post.getViews())
                .likesCount(post.getLikesCount())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }


    @Transactional
    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Authorization: Only author or admin can delete
        if (!post.getAuthor().getId().equals(user.getId())
                && !user.getRoles().contains("ADMIN")) {
            throw new RuntimeException("You are not authorized to delete this post");
        }

        postRepository.delete(post);
    }
    @Transactional
    public CommentDto postComment(Long postId, String content, Principal principal, Long parentId) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);

        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parentComment);
        }

        Comment saved = commentRepository.save(comment);
        return mapToDto(saved);
    }

    // Get all comments for a post
    public List<CommentDto> getCommentsForPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        List<Comment> topComments = commentRepository.findByPostAndParentIsNull(post);

        return topComments.stream()
                .map(this::mapToDtoWithReplies)
                .collect(Collectors.toList());
    }
    @Transactional
    public void deleteComment(Long commentId, Principal principal) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        User currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = comment.getPost();

        // Only post author or comment author can delete
        boolean isPostAdmin = post.getAuthor().getId().equals(currentUser.getId());
        boolean isCommentAuthor = comment.getUser().getId().equals(currentUser.getId());

        if (!isPostAdmin && !isCommentAuthor) {
            throw new RuntimeException("You are not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    // Map entity to DTO
    private CommentDto mapToDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setPostId(comment.getPost().getId());
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setContent(comment.getContent());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }

    // Recursive mapping for nested replies
    private CommentDto mapToDtoWithReplies(Comment comment) {
        CommentDto dto = mapToDto(comment);

        List<CommentDto> replies = comment.getPost().getComments().stream()
                .filter(c -> c.getParent() != null && c.getParent().getId().equals(comment.getId()))
                .map(this::mapToDtoWithReplies)
                .collect(Collectors.toList());

        dto.setReplies(replies);
        return dto;
    }
}
