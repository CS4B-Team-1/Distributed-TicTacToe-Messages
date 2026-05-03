package edu.cs4b.protocol;

public class CreateGameMessage implements Message {
    private String playerId;

    public CreateGameMessage(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return this.playerId;
    }
}