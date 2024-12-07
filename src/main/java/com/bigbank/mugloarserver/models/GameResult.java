package com.bigbank.mugloarserver.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * GameResult model
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class GameResult {
    @Id
    @Column(updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String gameId;

    private int finalScore;
    private int livesLeft;
    private boolean achievedGoal;
    private LocalDateTime finishedAt;
}
