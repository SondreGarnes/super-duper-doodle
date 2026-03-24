package com.superduperdoodle.controller;

import com.superduperdoodle.entity.PollVote;
import com.superduperdoodle.repository.PollVoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/poll")
public class PollController {

    @Autowired
    private PollVoteRepository repository;

    @GetMapping("/results")
    public Map<String, Long> getResults() {
        return Map.of(
            "optionA", repository.countByOption("A"),
            "optionB", repository.countByOption("B")
        );
    }

    @PostMapping("/vote")
    public ResponseEntity<?> vote(@RequestBody Map<String, String> body) {
        String option = body.get("option");
        if (!"A".equals(option) && !"B".equals(option)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid option"));
        }
        repository.save(new PollVote(option));
        return ResponseEntity.ok(Map.of("success", true));
    }
}
