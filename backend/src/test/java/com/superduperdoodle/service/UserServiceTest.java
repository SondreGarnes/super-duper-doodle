package com.superduperdoodle.service;

import com.superduperdoodle.entity.User;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostLikeRepository postLikeRepository;

    @InjectMocks
    private UserService userService;

    private User alice;
    private User bob;
    private User admin;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setUsername("alice");
        alice.setRole("USER");

        bob = new User();
        bob.setUsername("bob");
        bob.setRole("USER");

        admin = new User();
        admin.setUsername("admin");
        admin.setRole("ADMIN");
    }

    // --- deleteAccount ---

    @Test
    void deleteAccount_self_deletesOwnAccount() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        userService.deleteAccount("alice", "alice");

        verify(commentRepository).deleteByAuthor(alice);
        verify(postLikeRepository).deleteByUser(alice);
        verify(userRepository).delete(alice);
    }

    @Test
    void deleteAccount_adminDeletesOther_succeeds() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(bob));

        userService.deleteAccount("bob", "admin");

        verify(commentRepository).deleteByAuthor(bob);
        verify(postLikeRepository).deleteByUser(bob);
        verify(userRepository).delete(bob);
    }

    @Test
    void deleteAccount_nonAdminDeletesOther_throwsSecurityException() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(alice));

        assertThatThrownBy(() -> userService.deleteAccount("bob", "alice"))
            .isInstanceOf(SecurityException.class)
            .hasMessage("Not authorized");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteAccount_requesterNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount("alice", "ghost"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void deleteAccount_targetNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteAccount("ghost", "admin"))
            .isInstanceOf(UsernameNotFoundException.class);
    }
}
