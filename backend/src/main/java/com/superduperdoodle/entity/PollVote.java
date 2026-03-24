package com.superduperdoodle.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "poll_votes")
public class PollVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String option;

    @Column(nullable = false)
    private LocalDateTime votedAt;

    public PollVote() {}

    public PollVote(String option) {
        this.option = option;
        this.votedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getOption() { return option; }
    public void setOption(String option) { this.option = option; }
    public LocalDateTime getVotedAt() { return votedAt; }
    public void setVotedAt(LocalDateTime votedAt) { this.votedAt = votedAt; }
}
