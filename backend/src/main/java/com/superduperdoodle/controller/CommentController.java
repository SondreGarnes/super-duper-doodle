package com.superduperdoodle.controller;

import com.superduperdoodle.dto.CommentRequest;
import com.superduperdoodle.dto.CommentResponse;
import com.superduperdoodle.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        try {
            List<CommentResponse> comments = commentService.getComments(postId);
            return ResponseEntity.ok(comments);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable Long postId,
                                        @Valid @RequestBody CommentRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            CommentResponse comment = commentService.addComment(postId, request, userDetails.getUsername());
            return ResponseEntity.ok(comment);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId,
                                           @PathVariable Long commentId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            commentService.deleteComment(postId, commentId, userDetails.getUsername());
            return ResponseEntity.ok(Map.of("message", "Comment deleted"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
