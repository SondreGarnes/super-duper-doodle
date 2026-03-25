package com.superduperdoodle.service;

import com.superduperdoodle.dto.BlogPostRequest;
import com.superduperdoodle.dto.BlogPostResponse;
import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.User;
import com.superduperdoodle.repository.BlogPostRepository;
import com.superduperdoodle.repository.CommentRepository;
import com.superduperdoodle.repository.PostLikeRepository;
import com.superduperdoodle.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    public BlogPostService(BlogPostRepository blogPostRepository,
                           UserRepository userRepository,
                           PostLikeRepository postLikeRepository,
                           CommentRepository commentRepository) {
        this.blogPostRepository = blogPostRepository;
        this.userRepository = userRepository;
        this.postLikeRepository = postLikeRepository;
        this.commentRepository = commentRepository;
    }

    public BlogPostResponse createPost(BlogPostRequest request, String username) {
        User author = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        BlogPost post = new BlogPost();
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setAuthor(author);
        blogPostRepository.save(post);

        return toResponse(post, username);
    }

    public List<BlogPostResponse> getAllPosts(String currentUsername) {
        return blogPostRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(post -> toResponse(post, currentUsername))
            .toList();
    }

    public BlogPostResponse getPost(Long id, String currentUsername) {
        BlogPost post = blogPostRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Post not found"));
        return toResponse(post, currentUsername);
    }

    public List<BlogPostResponse> getPostsByUser(String username, String currentUsername) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return blogPostRepository.findByAuthorOrderByCreatedAtDesc(user).stream()
            .map(post -> toResponse(post, currentUsername))
            .toList();
    }

    public void deletePost(Long id, String username) {
        BlogPost post = blogPostRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Post not found"));
        User requester = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean isAdmin = "ADMIN".equals(requester.getRole());
        if (!isAdmin && !post.getAuthor().getUsername().equals(username)) {
            throw new SecurityException("Not authorized to delete this post");
        }
        blogPostRepository.delete(post);
    }

    public BlogPostResponse toResponse(BlogPost post, String currentUsername) {
        long likes = postLikeRepository.countByPostAndLiked(post, true);
        long dislikes = postLikeRepository.countByPostAndLiked(post, false);
        long comments = commentRepository.findByPostOrderByCreatedAtAsc(post).size();

        String userVote = null;
        if (currentUsername != null) {
            var maybeUser = userRepository.findByUsername(currentUsername);
            if (maybeUser.isPresent()) {
                var maybeLike = postLikeRepository.findByPostAndUser(post, maybeUser.get());
                if (maybeLike.isPresent()) {
                    userVote = maybeLike.get().isLiked() ? "LIKE" : "DISLIKE";
                }
            }
        }

        return new BlogPostResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getAuthor().getUsername(),
            post.getCreatedAt(),
            likes,
            dislikes,
            comments,
            userVote
        );
    }
}
