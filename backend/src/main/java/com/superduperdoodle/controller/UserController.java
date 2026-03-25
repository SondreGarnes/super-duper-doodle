package com.superduperdoodle.controller;

import com.superduperdoodle.dto.BlogPostResponse;
import com.superduperdoodle.service.BlogPostService;
import com.superduperdoodle.service.UserService;
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
    private final UserService userService;

    public UserController(BlogPostService blogPostService, UserService userService) {
        this.blogPostService = blogPostService;
        this.userService = userService;
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

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteAccount(@PathVariable String username,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            userService.deleteAccount(username, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "Account deleted"));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete account"));
        }
    }
}
