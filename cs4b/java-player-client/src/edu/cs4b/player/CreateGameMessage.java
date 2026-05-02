package edu.cs4b.player;
import edu.cs4b.protocol.Message;

public class CreateGameMessage implements Message {
    private String playerId;

    public CreateGameMessage(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return this.playerId;
    }
}