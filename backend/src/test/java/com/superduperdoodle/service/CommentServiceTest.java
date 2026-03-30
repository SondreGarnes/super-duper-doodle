package com.superduperdoodle.service;

import com.superduperdoodle.dto.CommentRequest;
import com.superduperdoodle.dto.CommentResponse;
import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.Comment;
import com.superduperdoodle.entity.User;
import com.superduperdoodle.repository.BlogPostRepository;
import com.superduperdoodle.repository.CommentRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BlogPostRepository blogPostRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User author;
    private BlogPost post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setUsername("alice");
        author.setEmail("alice@example.com");
        author.setPassword("encoded");

        post = new BlogPost();
        post.setTitle("Post Title");
        post.setContent("Post Content");
        post.setAuthor(author);
        ReflectionTestUtils.setField(post, "id", 1L);

        comment = new Comment();
        comment.setContent("Great post!");
        comment.setPost(post);
        comment.setAuthor(author);
    }

    // --- getComments ---

    @Test
    void getComments_success_returnsCommentList() {
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentRepository.findByPostOrderByCreatedAtAsc(post)).thenReturn(List.of(comment));

        List<CommentResponse> result = commentService.getComments(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("Great post!");
        assertThat(result.get(0).authorUsername()).isEqualTo("alice");
    }

    @Test
    void getComments_postNotFound_throws() {
        when(blogPostRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getComments(99L))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Post not found");
    }

    // --- addComment ---

    @Test
    void addComment_success_returnsCommentResponse() {
        CommentRequest request = new CommentRequest("Great post!");
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.addComment(1L, request, "alice");

        assertThat(response.content()).isEqualTo("Great post!");
        assertThat(response.authorUsername()).isEqualTo("alice");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_postNotFound_throws() {
        when(blogPostRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(99L, new CommentRequest("hi"), "alice"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Post not found");
    }

    @Test
    void addComment_userNotFound_throws() {
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.addComment(1L, new CommentRequest("hi"), "ghost"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    // --- deleteComment ---

    @Test
    void deleteComment_byAuthor_succeeds() {
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(author));
        commentService.deleteComment(1L, 10L, "alice");

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_byAdmin_succeeds() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setRole("ADMIN");

        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        commentService.deleteComment(1L, 10L, "admin");

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_byOtherUser_throwsSecurityException() {
        User other = new User();
        other.setUsername("bob");
        other.setRole("USER");

        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> commentService.deleteComment(1L, 10L, "bob"))
            .isInstanceOf(SecurityException.class)
            .hasMessage("Not authorized to delete this comment");

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_commentNotFound_throws() {
        when(commentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.deleteComment(1L, 99L, "alice"))
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("Comment not found");
    }

    @Test
    void deleteComment_wrongPost_throwsIllegalArgumentException() {
        // comment.getPost().getId() == 1L (from setUp), but we pass postId=2L
        when(commentRepository.findById(10L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.deleteComment(2L, 10L, "alice"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Comment does not belong to this post");
    }
}
