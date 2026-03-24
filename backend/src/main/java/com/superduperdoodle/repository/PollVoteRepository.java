package com.superduperdoodle.repository;

import com.superduperdoodle.entity.PollVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PollVoteRepository extends JpaRepository<PollVote, Long> {
    long countByOption(String option);
}
