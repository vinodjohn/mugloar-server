package com.bigbank.mugloarserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * GameResult model representing a past played game result stored in the H2 database.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "game_result", uniqueConstraints = {@UniqueConstraint(columnNames = {"game_id"})})
public class GameResult {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "game_id", nullable = false)
    private String gameId;

    private int score;
    private int highScore;
    private int lives;
    private double gold;
    private int level;
    private int turn;
    private boolean achievedGoal;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    @ElementCollection
    @CollectionTable(name = "processed_messages", joinColumns = @JoinColumn(name = "game_result_id",
            referencedColumnName = "id"))
    private List<ProcessedMessage> processedMessages = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "shop_items", joinColumns = @JoinColumn(name = "game_result_id",
            referencedColumnName = "id"))
    private List<ShopItem> purchasedItems = new ArrayList<>();
}