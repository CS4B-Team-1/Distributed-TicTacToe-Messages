package edu.cs4b.player;

import edu.cs4b.client.MessageListener;
import edu.cs4b.client.RouterClient;
import edu.cs4b.protocol.*;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Example player client that demonstrates the full message-passing lifecycle.
 *
 * Channels use a path-like naming convention:
 *   /lobby              — global chat channel
 *   /game/<id>          — a specific game session
 *   /dm/<name>          — direct messages for a player
 *
 * The router supports wildcard subscriptions: subscribing to "/game/*"
 * delivers messages from ALL game channels (e.g., /game/abc, /game/xyz).
 *
 * Usage:
 *   java edu.cs4b.player.PlayerMain --name Alice
 *   java edu.cs4b.player.PlayerMain --name Bob
 *   java edu.cs4b.player.PlayerMain               (random name)
 *
 * Commands:
 *   say <text>           Send a TextMessage to /lobby
 *   move <row> <col>     Send a MoveMessage to your game channel
 *   emoji <emoji> <n>    Send an EmojiMessage to /lobby
 *   join <gameId>        Subscribe to a game channel /game/<gameId>
 *   leave <gameId>       Unsubscribe from a game channel
 *   quit                 Disconnect and exit
 */
public class PlayerMain {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 4000;
    private static final String LOBBY = "/lobby";
    private static final String PLAYERS = "/players";

    public static void main(String[] args) {
        String name = "Player-" + UUID.randomUUID().toString().substring(0, 4);
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        // Parse command-line arguments
        for (int i = 0; i < args.length - 1; i++) {
            switch (args[i]) {
                case "--name" -> name = args[++i];
                case "--host" -> host = args[++i];
                case "--port" -> port = Integer.parseInt(args[++i]);
            }
        }

        // Track which game channel the player is currently in (if any)
        final String[] currentGame = {null};

        RouterClient client = new RouterClient(host, port);

        // thread-safe boolean flag for whether it's the player's turn or not.
        AtomicBoolean isPlayerTurn = new AtomicBoolean(true);

        try {
            client.connect();
            
            // final is used here so that the value of the playerId can be used in the MessageListener.
            final String playerId = name;

            // A single listener that handles all message types on any channel.
            // This is polymorphism in action — different Message subclasses
            // flow through the same callback, and we use instanceof to
            // distinguish them.
            MessageListener listener = (channel, senderId, message) -> {
                String prefix = "[" + channel + "] ";
                if (message instanceof JoinMessage join) {
                    System.out.println(prefix + senderId + " joined: " + join.getPlayerName());
                } else if (message instanceof EmojiMessage emoji) {
                    System.out.println(prefix + senderId + " sent: " + emoji.render());
                } else if (message instanceof TextMessage text) {
                    System.out.println(prefix + senderId + " says: " + text.getText());
                } else if (message instanceof MoveAcceptedMessage moveAccepted) {
                    System.out.println(prefix + senderId + " played: (" + moveAccepted.getRow() + ", " + moveAccepted.getCol() + ")");
                    if (moveAccepted.getGameStatus().equals("Ongoing")) {
                        // TODO: implement players switching turns (need to determine initial turn first on start of game; "Join Game" flow)
                        System.out.println("Next Turn"); // temp dummy message
                        // if (moveAccepted.getNextTurn().equals(playerId)) {
                        //     System.out.println("Your turn!");
                        //     isPlayerTurn.compareAndSet(false, true);
                        // } 
                        // else {
                        //     isPlayerTurn.compareAndSet(true, false);
                        // } 
                        // TODO: probably need different cases for the different end game states?
                    } else {
                        System.out.println("Game Completed!");
                    }
                } else if (message instanceof MoveRejectedMessage moveRejected) {
                    System.out.println(prefix + senderId + " move invalid: (" + moveRejected.getRow() + ", " + moveRejected.getCol() + ")");
                } else {
                    System.out.println(prefix + senderId + " sent: " + message);
                }
            };

            // Subscribe to the lobby channel
            client.subscribe(LOBBY, listener);
            client.subscribe(PLAYERS + "\\" + name);

            // Announce ourselves in the lobby
            client.send(LOBBY, new JoinMessage(name));

            // Interactive command loop
            System.out.println();
            System.out.println("Commands:");
            System.out.println("  say <text>           Send a chat message to /lobby");
            System.out.println("  move <row> <col>     Send a move to your game channel");
            System.out.println("  emoji <emoji> <n>    Send an emoji n times to /lobby");
            System.out.println("  create <gameId>      Create game channel /game/<gameId>");
            System.out.println("  join <gameId>        Join game channel /game/<gameId>");
            System.out.println("  leave <gameId>       Leave game channel /game/<gameId>");
            System.out.println("  quit                 Disconnect and exit");
            System.out.println();

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                try {
                    if (line.startsWith("say ")) {
                        client.send(LOBBY, new TextMessage(line.substring(4)));

                    } else if (line.startsWith("move ")) {
                        if (currentGame[0] == null) {
                            System.out.println("Not in a game. Use: join <gameId>");
                            continue;
                        } else if (!isPlayerTurn.get()) {
                            System.out.println("It is not your turn.");
                            continue;
                        }
                        String[] parts = line.split("\\s+");
                        int row = Integer.parseInt(parts[1]);
                        int col = Integer.parseInt(parts[2]);

                        // grab gameId from currentGame[0]
                        String gameId = currentGame[0].substring(6).trim();
                        // send MakeMoveMessage to GameController
                        client.send("/game/", new MakeMoveMessage(gameId, name, row, col));

                    }else if (line.startsWith("emoji ")) {
                        String[] parts = line.split("\\s+");
                        String emoji = parts[1];
                        int count = Integer.parseInt(parts[2]);
                        client.send(LOBBY, new EmojiMessage(emoji, count));

                    } else if (line.startsWith("join ")) {  // JOIN GAME USERFLOW
                        String gameId = line.substring(5).trim();
                        String gameChannel = "/game/" + gameId;
                        
                        // TODO: player should probably only subscribe if they receive a JoinMessage response in the listener above
                        client.subscribe(gameChannel, listener);

                        //only needed to add one line for Join_Game userflow
                        
                        client.send("/game/" + gameId, new JoinGameMessage(playerId, gameId));

                        currentGame[0] = gameChannel;
                        System.out.println("Joined " + gameChannel);

                    }else if (line.startsWith("create ")){
                        String gameId = line.substring(7).trim();
                        String gameChannel = "/game/" + gameId;

                        // TODO: player should probably only subscribe if they receive a GameCreatedMessage response in the listener above
                        client.subscribe(gameChannel, listener);
                        currentGame[0] = gameChannel;
                        System.out.println("Joined " + gameChannel);

                    } else if (line.startsWith("leave ")) {
                        String gameId = line.substring(6).trim();
                        String gameChannel = "/game/" + gameId;
                        client.unsubscribe(gameChannel);
                        if (gameChannel.equals(currentGame[0])) {
                            currentGame[0] = null;
                        }
                        System.out.println("Left " + gameChannel);

                    } else if (line.equals("quit")) {
                        break;

                    } else {
                        System.out.println("Unknown command. Try: say, move, emoji, join, leave, quit");
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("Invalid arguments. Check your command format.");
                }
            }

            client.unsubscribe(LOBBY);
            client.disconnect();

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
