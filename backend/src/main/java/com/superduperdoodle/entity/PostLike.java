package com.superduperdoodle.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BlogPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // true = like, false = dislike
    @Column(nullable = false)
    private boolean isLike;

    public Long getId() { return id; }
    public BlogPost getPost() { return post; }
    public void setPost(BlogPost post) { this.post = post; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isLike() { return isLike; }
    public void setLike(boolean like) { isLike = like; }
}
