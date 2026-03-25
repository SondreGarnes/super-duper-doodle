package com.superduperdoodle.repository;

import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.PostLike;
import com.superduperdoodle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(BlogPost post, User user);
    long countByPostAndIsLike(BlogPost post, boolean isLike);
}
