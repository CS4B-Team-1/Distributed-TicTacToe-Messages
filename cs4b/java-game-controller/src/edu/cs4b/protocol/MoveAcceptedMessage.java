package edu.cs4b.protocol;

public class MoveAcceptedMessage implements Message {
    private String gameId;
    private String playerId;    // the playerId of the player that made the move
    private int row;
    private int col;
    private String updatedBoard;
    private String nextTurn;    // the playerId of the player whose turn is next
    private String gameStatus;

    public MoveAcceptedMessage(String gameId, String playerId, int row, int col,
                               String updatedBoard, String nextTurn, String gameStatus) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.row = row;
        this.col = col;
        this.updatedBoard = updatedBoard;
        this.nextTurn = nextTurn;
        this.gameStatus = gameStatus;
    }

    public String getGameId() {
        return this.gameId;
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    public String getUpdatedBoard() {
        return this.updatedBoard;
    }

    public String getNextTurn() {
        return this.nextTurn;
    }

    public String getGameStatus() {
        return this.gameStatus;
    }
}