package edu.cs4b.protocol;

public class CreateGameMessage implements Message {
    private final String gameId;
    private final String playerId;

    public CreateGameMessage(String gameId, String playerId) {
        this.gameId = gameId;
        this.playerId = playerId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayerId() {
        return playerId;
    }

}