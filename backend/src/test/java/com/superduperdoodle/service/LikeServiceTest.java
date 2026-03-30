package com.superduperdoodle.service;

import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.PostLike;
import com.superduperdoodle.entity.User;
import com.superduperdoodle.repository.BlogPostRepository;
import com.superduperdoodle.repository.PostLikeRepository;
import com.superduperdoodle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private BlogPostRepository blogPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BlogPostService blogPostService;

    @InjectMocks
    private LikeService likeService;

    private User user;
    private BlogPost post;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword("encoded");

        post = new BlogPost();
        post.setTitle("Title");
        post.setContent("Content");
        post.setAuthor(user);
    }

    // --- toggleVote ---

    @Test
    void toggleVote_newLike_savesNewPostLike() {
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(postLikeRepository.findByPostAndUser(post, user)).thenReturn(Optional.empty());

        likeService.toggleVote(1L, true, "alice");

        verify(postLikeRepository).save(any(PostLike.class));
        verify(blogPostService).toResponse(post, "alice");
    }

    @Test
    void toggleVote_sameLikeAgain_deletesExistingLike() {
        PostLike existing = new PostLike();
        existing.setPost(post);
        existing.setUser(user);
        existing.setLiked(true);

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(postLikeRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(existing));

        likeService.toggleVote(1L, true, "alice");

        verify(postLikeRepository).delete(existing);
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    void toggleVote_differentVote_switchesLike() {
        PostLike existing = new PostLike();
        existing.setPost(post);
        existing.setUser(user);
        existing.setLiked(true); // was a like

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(postLikeRepository.findByPostAndUser(post, user)).thenReturn(Optional.of(existing));

        likeService.toggleVote(1L, false, "alice"); // now dislike

        verify(postLikeRepository).save(existing);
        verify(postLikeRepository, never()).delete(any());
    }

    @Test
    void toggleVote_postNotFound_throws() {
        when(blogPostRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggleVote(99L, true, "alice"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Post not found");
    }

    @Test
    void toggleVote_userNotFound_throws() {
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.toggleVote(1L, true, "ghost"))
            .isInstanceOf(UsernameNotFoundException.class);
    }
}
