package com.blogWebsite.BlogWebsite.controller;

import com.blogWebsite.BlogWebsite.dto.CommentDto;
import com.blogWebsite.BlogWebsite.dto.CommentRequest;
import com.blogWebsite.BlogWebsite.dto.PostDto;
import com.blogWebsite.BlogWebsite.entity.Post;
import com.blogWebsite.BlogWebsite.entity.User;
import com.blogWebsite.BlogWebsite.repository.PostRepository;
import com.blogWebsite.BlogWebsite.repository.UserRepository;
import com.blogWebsite.BlogWebsite.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository;
    // Create a new post
    @PostMapping
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto, Principal principal) {
        PostDto created = postService.create(postDto, principal.getName());
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public List<PostDto> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/profile")
    public ResponseEntity<List<PostDto>> getAllUserPosts(@AuthenticationPrincipal UserDetails userDetails) {
        // Fetch the User entity using email/username
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get posts by author (using user entity)
        List<PostDto> posts = postService.getAllPostsByUserId(user.getId());
        return ResponseEntity.ok(posts);
    }


    // Update an existing post
    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updatePost(
            @PathVariable Long id,
            @RequestBody PostDto postDto,
            Principal principal) {
        PostDto updated = postService.update(id, postDto, principal.getName());
        return ResponseEntity.ok(updated);
    }

    // Get post by slug
    @GetMapping("/{slug}")
    public ResponseEntity<PostDto> getPost(@PathVariable String slug) {
        PostDto post = postService.getBySlug(slug);
        return ResponseEntity.ok(post);
    }

    // List posts with pagination and optional tag filter
    @GetMapping("/tag")
    public ResponseEntity<Page<PostDto>> listPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String tag) {
        Page<PostDto> posts = postService.list(page, size, tag);
        return ResponseEntity.ok(posts);
    }

    // Like/unlike a post
    @PostMapping("/{id}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long id, Principal principal) {
        postService.toggleLike(id, principal.getName());
        return ResponseEntity.ok("Post liked/unliked successfully");
    }

    // Post a comment
    @PostMapping("/{id}/comment")
    public ResponseEntity<CommentDto> postComment(
            @PathVariable Long id,
            @RequestBody CommentRequest commentRequest,
            Principal principal
    ) {
        CommentDto dto = postService.postComment(id, commentRequest.getContent(), principal, commentRequest.getParentId());
        return ResponseEntity.ok(dto);
    }

    // Get comments for a post
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getCommentsForPost(id));
    }

    // Delete comment
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId, Principal principal) {
        postService.deleteComment(commentId, principal);
        return ResponseEntity.ok("Comment deleted successfully");
    }
    // Delete a post (only author can delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Principal principal) {
        // Can implement in service as postService.delete(id, principal.getName())
        postService.deletePost(id,principal.getName());
        return ResponseEntity.ok().build();
    }
}
