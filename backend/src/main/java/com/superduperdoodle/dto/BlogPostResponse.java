package com.superduperdoodle.dto;

import java.time.LocalDateTime;

public record BlogPostResponse(
    Long id,
    String title,
    String content,
    String authorUsername,
    LocalDateTime createdAt,
    long likeCount,
    long dislikeCount,
    long commentCount,
    String userVote  // "LIKE", "DISLIKE", or null
) {}
