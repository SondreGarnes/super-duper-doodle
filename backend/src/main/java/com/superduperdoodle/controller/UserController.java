package com.superduperdoodle.controller;

import com.superduperdoodle.dto.BlogPostResponse;
import com.superduperdoodle.service.BlogPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final BlogPostService blogPostService;

    public UserController(BlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @GetMapping("/{username}/posts")
    public ResponseEntity<?> getUserPosts(@PathVariable String username,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String currentUsername = userDetails != null ? userDetails.getUsername() : null;
            List<BlogPostResponse> posts = blogPostService.getPostsByUser(username, currentUsername);
            return ResponseEntity.ok(Map.of("username", username, "posts", posts));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
