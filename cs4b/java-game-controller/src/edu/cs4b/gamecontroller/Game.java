package edu.cs4b.gamecontroller;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Game {
    final private String gameId;
    
    private ConcurrentHashMap<String, String> players = new ConcurrentHashMap<>(); 
    private List<Integer> Board = new CopyOnWriteArrayList<>();
    AtomicReference<String> currentTurn = new AtomicReference<>("");

    static final private int MAX_ROW_COL = 3;
    static final private int MAX_PLAYERS = 2;

    public Game(String gameId, String creatorId) {
        this.gameId = gameId;
        this.players.put(creatorId, "X");
        currentTurn = new AtomicReference<>(creatorId);
        for (int i = 0; i < 9; i++) {
            Board.add(0);
        }
    }

    public String getGameId() {
        return this.gameId;
    }

    public ConcurrentHashMap<String, String> getPlayers() {
        return new ConcurrentHashMap<>(this.players);
    }
    
    public void addPlayer(String playerId, String symbol) {
        if (players.keySet().size() < MAX_PLAYERS)
            players.put(playerId, symbol);
    }

    public void updateBoard(int row, int col, String symbol) {
        int symbolVal = 0;
        if (symbol.equals("X")) {
            symbolVal = 1;
        } else if (symbol.equals("O")) {
            symbolVal = -1;
        }

        int index = col + (row * MAX_ROW_COL);
        this.Board.set(index, Integer.valueOf(symbolVal));
    }

    public CopyOnWriteArrayList<Integer> getBoard() {
        return new CopyOnWriteArrayList<>(this.Board);
    }

    public String getCurrentTurn() {
        return this.currentTurn.get();
    }

}