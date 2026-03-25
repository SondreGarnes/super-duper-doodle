package com.superduperdoodle.service;

import com.superduperdoodle.entity.User;
import com.superduperdoodle.repository.CommentRepository;
import com.superduperdoodle.repository.PostLikeRepository;
import com.superduperdoodle.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    public UserService(UserRepository userRepository,
                       CommentRepository commentRepository,
                       PostLikeRepository postLikeRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
    }

    @Transactional
    public void deleteAccount(String targetUsername, String requesterUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        boolean isSelf = requesterUsername.equals(targetUsername);
        boolean isAdmin = "ADMIN".equalsIgnoreCase(requester.getRole());
        if (!isSelf && !isAdmin) {
            throw new SecurityException("Not authorized");
        }

        User user = userRepository.findByUsername(targetUsername)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        // Delete comments and likes made by this user on other posts first
        commentRepository.deleteByAuthor(user);
        postLikeRepository.deleteByUser(user);
        // Deleting the user cascades to their own posts (and those posts' comments/likes)
        userRepository.delete(user);
    }
}
