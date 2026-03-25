package com.superduperdoodle.controller;

import com.superduperdoodle.dto.BlogPostRequest;
import com.superduperdoodle.dto.BlogPostResponse;
import com.superduperdoodle.service.BlogPostService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/posts")
public class BlogPostController {

    private final BlogPostService blogPostService;

    public BlogPostController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @GetMapping
    public List<BlogPostResponse> getAllPosts(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return blogPostService.getAllPosts(username);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable Long id,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String username = userDetails != null ? userDetails.getUsername() : null;
            return ResponseEntity.ok(blogPostService.getPost(id, username));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody BlogPostRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        BlogPostResponse response = blogPostService.createPost(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            blogPostService.deletePost(id, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "Post deleted"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
