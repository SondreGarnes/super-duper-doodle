package com.superduperdoodle.repository;

import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByAuthorOrderByCreatedAtDesc(User author);
    List<BlogPost> findAllByOrderByCreatedAtDesc();
}
