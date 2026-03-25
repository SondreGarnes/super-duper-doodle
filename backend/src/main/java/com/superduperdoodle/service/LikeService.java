package com.superduperdoodle.service;

import com.superduperdoodle.dto.BlogPostResponse;
import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.PostLike;
import com.superduperdoodle.entity.User;
import com.superduperdoodle.repository.BlogPostRepository;
import com.superduperdoodle.repository.PostLikeRepository;
import com.superduperdoodle.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class LikeService {

    private final PostLikeRepository postLikeRepository;
    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;
    private final BlogPostService blogPostService;

    public LikeService(PostLikeRepository postLikeRepository,
                       BlogPostRepository blogPostRepository,
                       UserRepository userRepository,
                       BlogPostService blogPostService) {
        this.postLikeRepository = postLikeRepository;
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
        this.blogPostService = blogPostService;
    }

    public BlogPostResponse toggleVote(Long postId, boolean isLike, String username) {
        BlogPost post = blogPostRepository.findById(postId)
            .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);

        if (existing.isPresent()) {
            PostLike like = existing.get();
            if (like.isLiked() == isLike) {
                // Same vote — toggle off
                postLikeRepository.delete(like);
            } else {
                // Different vote — switch
                like.setLiked(isLike);
                postLikeRepository.save(like);
            }
        } else {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            like.setLiked(isLike);
            postLikeRepository.save(like);
        }

        return blogPostService.toResponse(post, username);
    }
}
