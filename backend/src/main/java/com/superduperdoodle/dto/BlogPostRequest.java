package com.superduperdoodle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogPostRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank String content
) {}
