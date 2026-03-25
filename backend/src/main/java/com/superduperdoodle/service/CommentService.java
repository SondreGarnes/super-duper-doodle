package com.superduperdoodle.service;

import com.superduperdoodle.dto.CommentRequest;
import com.superduperdoodle.dto.CommentResponse;
import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.Comment;
import com.superduperdoodle.entity.User;
import com.superduperdoodle.repository.BlogPostRepository;
import com.superduperdoodle.repository.CommentRepository;
import com.superduperdoodle.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository,
                          BlogPostRepository blogPostRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
    }

    public List<CommentResponse> getComments(Long postId) {
        BlogPost post = blogPostRepository.findById(postId)
            .orElseThrow(() -> new NoSuchElementException("Post not found"));
        return commentRepository.findByPostOrderByCreatedAtAsc(post).stream()
            .map(c -> new CommentResponse(c.getId(), c.getContent(), c.getAuthor().getUsername(), c.getCreatedAt()))
            .toList();
    }

    public CommentResponse addComment(Long postId, CommentRequest request, String username) {
        BlogPost post = blogPostRepository.findById(postId)
            .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User author = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Comment comment = new Comment();
        comment.setContent(request.content());
        comment.setPost(post);
        comment.setAuthor(author);
        commentRepository.save(comment);

        return new CommentResponse(comment.getId(), comment.getContent(), username, comment.getCreatedAt());
    }

    public void deleteComment(Long postId, Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to this post");
        }
        User requester = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isAuthor = comment.getAuthor().getUsername().equals(username);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requester.getRole());
        if (!isAuthor && !isAdmin) {
            throw new SecurityException("Not authorized to delete this comment");
        }
        commentRepository.delete(comment);
    }
}
