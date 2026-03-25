package com.superduperdoodle.repository;

import com.superduperdoodle.entity.BlogPost;
import com.superduperdoodle.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostOrderByCreatedAtAsc(BlogPost post);
}
