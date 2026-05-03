package edu.cs4b.gamecontroller;

import edu.cs4b.client.MessageListener;
import edu.cs4b.client.RouterClient;
import edu.cs4b.protocol.JoinMessage;
import edu.cs4b.protocol.MoveMessage;
import edu.cs4b.protocol.TextMessage;

import java.io.IOException;

/**
 * GameController client
 *
 * The GameController connects to the router and subscribes to /game/* ,
 * allowing it to receive messages from any game channel
 *
 * For now, this only prints received messages so we can test routing
 * TODO: validate moves and send messages back to the correct channels
 **/

public class GameControllerMain {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4000;
    private static final String ALL_GAME_CHANNELS = "/game/*";

    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        // Parse command-line arguments
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--host" -> host = args[++i];
                case "--port" -> port = Integer.parseInt(args[++i]);
            }
        }

        RouterClient client = new RouterClient(host, port);
        
        try {
            client.connect();

            MessageListener listener = (channel, senderId, message) -> {
                System.out.println();
                System.out.println("[GameController] message received");
                System.out.println("Channel: " + channel);
                System.out.println("Sender: " + senderId);
                System.out.println("Message type: " + message.getClass().getSimpleName());

                if (message instanceof JoinMessage join) {
                    System.out.println("Player joined: " + join.getPlayerName());
                } else if (message instanceof MoveMessage move) {
                    System.out.println("Move: row " + move.getRow() + ", col " + move.getCol());
                } else if (message instanceof TextMessage text) {
                    System.out.println("Text: " + text.getText());
                } else {
                    System.out.println("Message: " + message);
                }

                System.out.println();
            };

            // Subscribe to all game channels
            client.subscribe(ALL_GAME_CHANNELS, listener);

            System.out.println("GameController connected.");
            System.out.println("Subscribed to " + ALL_GAME_CHANNELS);
            System.out.println("Waiting for game messages...");

            // Keep the controller running so it can keep listening for messages
            while (true) {
                Thread.sleep(1000);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("GameController stopped.");
        }
    }
}
