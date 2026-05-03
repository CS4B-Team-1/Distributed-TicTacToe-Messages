package edu.cs4b.protocol;
import java.util.ArrayList;

public class StartGameMessage implements Message {
    private String playerId;
    private String gameId;
    private String firstTurn;
    private String initialBoard;
    
    public StartGameMessage(String playerId, String gameId, String firstTurn, ArrayList<Integer> initialBoard) {
        this.playerId = playerId;
        this.gameId = gameId;
    }

    public String getPlayerId() {
        return this.playerId;
    }
    
    public String getGameId() {
        return this.gameId;
    }

    public String getFirstTurn() {
        return this.firstTurn;
    }
    
    public String getInitialBoard() {
        return this.initialBoard;
    }
}