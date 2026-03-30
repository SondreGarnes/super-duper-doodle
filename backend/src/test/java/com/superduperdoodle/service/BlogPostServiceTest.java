package com.superduperdoodle.service;

import com.superduperdoodle.dto.BlogPostRequest;
import com.superduperdoodle.dto.BlogPostResponse;
import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.PostLike;
import com.superduperdoodle.entity.User;
import com.superduperdoodle.repository.BlogPostRepository;
import com.superduperdoodle.repository.CommentRepository;
import com.superduperdoodle.repository.PostLikeRepository;
import com.superduperdoodle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private BlogPostService blogPostService;

    private User author;
    private BlogPost post;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setUsername("alice");
        author.setEmail("alice@example.com");
        author.setPassword("encoded");

        post = new BlogPost();
        post.setTitle("Test Title");
        post.setContent("Test Content");
        post.setAuthor(author);
        ReflectionTestUtils.setField(post, "id", 1L);
    }

    private void stubToResponse(BlogPost p, String currentUsername, long likes, long dislikes, String userVote) {
        when(postLikeRepository.countByPostAndLiked(p, true)).thenReturn(likes);
        when(postLikeRepository.countByPostAndLiked(p, false)).thenReturn(dislikes);
        when(commentRepository.findByPostOrderByCreatedAtAsc(p)).thenReturn(List.of());
        if (currentUsername != null) {
            when(userRepository.findByUsername(currentUsername)).thenReturn(Optional.of(author));
            if (userVote != null) {
                PostLike like = new PostLike();
                like.setLiked("LIKE".equals(userVote));
                when(postLikeRepository.findByPostAndUser(p, author)).thenReturn(Optional.of(like));
            } else {
                when(postLikeRepository.findByPostAndUser(p, author)).thenReturn(Optional.empty());
            }
        }
    }

    // --- createPost ---

    @Test
    void createPost_success_returnsResponse() {
        BlogPostRequest request = new BlogPostRequest("Test Title", "Test Content");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(author));
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(post);
        // The service creates its own BlogPost instance internally, so match on any()
        when(postLikeRepository.countByPostAndLiked(any(BlogPost.class), eq(true))).thenReturn(0L);
        when(postLikeRepository.countByPostAndLiked(any(BlogPost.class), eq(false))).thenReturn(0L);
        when(commentRepository.findByPostOrderByCreatedAtAsc(any(BlogPost.class))).thenReturn(List.of());
        when(postLikeRepository.findByPostAndUser(any(BlogPost.class), eq(author))).thenReturn(Optional.empty());

        BlogPostResponse response = blogPostService.createPost(request, "alice");

        assertThat(response.title()).isEqualTo("Test Title");
        assertThat(response.content()).isEqualTo("Test Content");
        assertThat(response.authorUsername()).isEqualTo("alice");
        verify(blogPostRepository).save(any(BlogPost.class));
    }

    @Test
    void createPost_userNotFound_throws() {
        BlogPostRequest request = new BlogPostRequest("Title", "Content");
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogPostService.createPost(request, "ghost"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    // --- getAllPosts ---

    @Test
    void getAllPosts_returnsMappedList() {
        when(blogPostRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(post));
        stubToResponse(post, "alice", 2, 1, "LIKE");

        List<BlogPostResponse> result = blogPostService.getAllPosts("alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).likeCount()).isEqualTo(2);
        assertThat(result.get(0).dislikeCount()).isEqualTo(1);
        assertThat(result.get(0).userVote()).isEqualTo("LIKE");
    }

    @Test
    void getAllPosts_withNullUsername_returnsNoUserVote() {
        when(blogPostRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(post));
        when(postLikeRepository.countByPostAndLiked(post, true)).thenReturn(0L);
        when(postLikeRepository.countByPostAndLiked(post, false)).thenReturn(0L);
        when(commentRepository.findByPostOrderByCreatedAtAsc(post)).thenReturn(List.of());

        List<BlogPostResponse> result = blogPostService.getAllPosts(null);

        assertThat(result.get(0).userVote()).isNull();
    }

    // --- getPost ---

    @Test
    void getPost_success_returnsResponse() {
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        stubToResponse(post, "alice", 0, 0, null);

        BlogPostResponse response = blogPostService.getPost(1L, "alice");

        assertThat(response.title()).isEqualTo("Test Title");
    }

    @Test
    void getPost_notFound_throws() {
        when(blogPostRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogPostService.getPost(99L, "alice"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Post not found");
    }

    // --- getPostsByUser ---

    @Test
    void getPostsByUser_success_returnsUserPosts() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(author));
        when(blogPostRepository.findByAuthorOrderByCreatedAtDesc(author)).thenReturn(List.of(post));
        stubToResponse(post, "alice", 0, 0, null);

        List<BlogPostResponse> result = blogPostService.getPostsByUser("alice", "alice");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).authorUsername()).isEqualTo("alice");
    }

    @Test
    void getPostsByUser_userNotFound_throws() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogPostService.getPostsByUser("ghost", "alice"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    // --- deletePost ---

    @Test
    void deletePost_byAuthor_succeeds() {
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(author));

        blogPostService.deletePost(1L, "alice");

        verify(blogPostRepository).delete(post);
    }

    @Test
    void deletePost_byAdmin_succeeds() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        blogPostService.deletePost(1L, "admin");

        verify(blogPostRepository).delete(post);
    }

    @Test
    void deletePost_byOtherUser_throwsSecurityException() {
        User other = new User();
        other.setUsername("bob");
        other.setRole("USER");

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> blogPostService.deletePost(1L, "bob"))
            .isInstanceOf(SecurityException.class)
            .hasMessage("Not authorized to delete this post");

        verify(blogPostRepository, never()).delete(any());
    }

    @Test
    void deletePost_postNotFound_throws() {
        when(blogPostRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogPostService.deletePost(99L, "alice"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Post not found");
    }

    // --- toResponse (userVote logic) ---

    @Test
    void toResponse_withDislikeVote_returnsDislike() {
        when(postLikeRepository.countByPostAndLiked(post, true)).thenReturn(0L);
        when(postLikeRepository.countByPostAndLiked(post, false)).thenReturn(1L);
        when(commentRepository.findByPostOrderByCreatedAtAsc(post)).thenReturn(List.of());
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(author));
        PostLike dislike = new PostLike();
        dislike.setLiked(false);
        when(postLikeRepository.findByPostAndUser(post, author)).thenReturn(Optional.of(dislike));

        BlogPostResponse response = blogPostService.toResponse(post, "alice");

        assertThat(response.userVote()).isEqualTo("DISLIKE");
        assertThat(response.dislikeCount()).isEqualTo(1);
    }
}
