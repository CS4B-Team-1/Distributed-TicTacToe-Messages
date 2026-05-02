package edu.cs4b.player;
import edu.cs4b.protocol.Message;

public class GameDrawMessage implements Message {
    private String gameId;
    private String finalBoard;

    public GameDrawMessage(String gameId, String finalBoard) {
        this.gameId = gameId;
        this.finalBoard = finalBoard;
    }

    public String getGameId() {
        return gameId;
    }

    public String getFinalBoard() {
        return finalBoard;
    }
}