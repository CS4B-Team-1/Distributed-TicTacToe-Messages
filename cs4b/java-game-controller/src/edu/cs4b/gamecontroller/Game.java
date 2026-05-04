package edu.cs4b.gamecontroller;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Game {
    final private String gameId;
    private List<String> Players = new CopyOnWriteArrayList<>();
    private List<Integer> Board = new CopyOnWriteArrayList<>();
    AtomicReference<String> currentTurn = new AtomicReference<>("");

    public Game(String gameId, String creator) {
        this.gameId = gameId;
        Players.add(creator);
        currentTurn = new AtomicReference<>(creator);
        for (int i = 0; i < 9; i++) {
            Board.add(0);
        }
    }


}