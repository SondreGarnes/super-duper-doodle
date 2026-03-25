package com.superduperdoodle.controller;

import com.superduperdoodle.dto.BlogPostResponse;
import com.superduperdoodle.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/posts/{postId}")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/like")
    public ResponseEntity<?> like(@PathVariable Long postId,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        try {
            BlogPostResponse response = likeService.toggleVote(postId, true, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }
    }

    @PostMapping("/dislike")
    public ResponseEntity<?> dislike(@PathVariable Long postId,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        try {
            BlogPostResponse response = likeService.toggleVote(postId, false, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(401).body(Map.of("error", "User not found"));
        }
    }
}
