package edu.cs4b.protocol;

public class MoveRejectedMessage implements Message {
    private String gameId;
    private String playerId;
    private String attemptedPosition;
    private String reason;

    public MoveRejectedMessage(String gameId, String playerId, String attemptedPosition, String reason) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.attemptedPosition = attemptedPosition;
        this.reason = reason;
    }

    public String getGameId() {
        return this.gameId;
    }

    public String getPlayerId() {
        return this.playerId;
    }

    public String getAttemptedPosition() {
        return this.attemptedPosition;
    }

    public String getReason() {
        return this.reason;
    }
}