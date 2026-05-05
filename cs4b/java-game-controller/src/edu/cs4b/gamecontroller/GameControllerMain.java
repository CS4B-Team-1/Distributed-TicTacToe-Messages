package edu.cs4b.gamecontroller;

import edu.cs4b.client.MessageListener;
import edu.cs4b.client.RouterClient;
import edu.cs4b.protocol.*;
import edu.cs4b.gamecontroller.Game;
import java.util.UUID;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
    private static final String PLAYERS = "/players";

    private static ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();

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
                } else if (message instanceof MakeMoveMessage move) {
                    makeMoveMessageReceived(client, channel, move, games);
                } else if (message instanceof TextMessage text) {
                    System.out.println("Text: " + text.getText());
                } else if (message instanceof CreateGameMessage game) {
                    createGame(game);
                }else {
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

    private static void createGame(CreateGameMessage message){
        String playerID = message.getPlayerId();
        //Game("Game-" + UUID.randomUUID().toString().substring(0, 4), playerID);
    }

    private static void makeMoveMessageReceived(RouterClient client, String channel, MakeMoveMessage move, ConcurrentHashMap<String, Game> game) {
        try {
            // check if the move is valid
            if (checkIfMoveValid(move.getGameId(), move.getRow(), move.getColumn())) {
                // if it's valid, update the game board
                updateGameBoard(move.getGameId(), move.getRow(), move.getColumn());
                // check the state of the board
                int boardStatus = checkGameEnd(move.getGameId());
                // if the boardStatus returned is not 0, the game is finished
                String statusStr;
                // TODO: create a better status indicator
                if (boardStatus != 0) {
                    statusStr = "Completed";
                    // Call winnerMessages function with boardStatus int
                    winnerMessages(client, move.getGameId(), channel, games, boardStatus);
                } else {
                    statusStr = "Ongoing";
                }
                // send the MoveAcceptedMessage to the players
                client.send(channel + move.getGameId(), new MoveAcceptedMessage(
                                            move.getGameId(), 
                                            move.getPlayerId(), 
                                            move.getRow(), 
                                            move.getColumn(), 
                                            "board-state-here",
                                            "X or O", 
                                            statusStr));
                // TODO: actually send an updated board state, whose turn is next, and the game status
            } else {
                // TODO: create actual reason for rejection
                client.send(PLAYERS + move.getPlayerId(), new MoveRejectedMessage(
                                            move.getGameId(), 
                                            move.getPlayerId(),
                                            move.getRow(), 
                                            move.getColumn(), 
                                            "Just because lol")); 
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean checkIfMoveValid(String gameId, int row, int column) {
        // TODO do an actual validation using gameId's associated game board to check if a move is already there
        return true;
    }

    private static void updateGameBoard(String gameId, int row, int column) {
        // TODO: update game board associated with gameId
        return;
    }

    // Returns the following:
    // 1 if game is won by a player
    // 0 if game is continuing
    // -1 if game is a draw
    // TODO: for now, randomly generates game end state, need to determine the ACTUAL game end
    private static int checkGameEnd(String gameId) {
        Random random = new Random();
        // returns a number between -1 and 1
        // (0, 1, or 2) - 1 translates to a -1, 0, or 1
        return random.nextInt(3) - 1; 
    }



    // Game End flow:

    // Handle cleanup of the game
    //  - Delete the game
    //  - Unsubscribe players from the game

    // winnerMessages function
    // Checks whether or not the game is finished
    // Sends out game won / over / draw messages to the corresponding players
    //  1 = Game is won by a player
    //  0 = Game continuing
    // -1 = Draw
    private static void winnerMessages(RouterClient client, String gameId, String channel, ConcurrentHashMap<String, Game> games, int winner) {
        // Check if game is finished or not
        // If game isn't finished, exit function
        if (winner == 0) {
            return;
        }
        // Check if game is a draw or not
        if (winner == -1) {
            // If game is a draw, send GameDrawMessage to both players
            // Send final board state later
            // GameDrawMessage(String gameId, String finalBoard)
            try {
                client.send(gameId, new GameDrawMessage(gameId, ""));
            } catch (IOException e) {
                System.out.println("ERROR: Failed to send draw message!");
            }
        } else {
                // If game is not a draw, send win and lose messages
                switch (winner) {
                    // Send GameWonMessage to winning player
                    // Later update winner, winningLine, and finalBoard to not be blank
                    // GameWonMessage(String gameId, String winner, String winningLine, String finalBoard)
                    case 1:
                        try {
                            client.send(client.getClientId(), new GameWonMessage(gameId, "", "", ""));
                        } catch (IOException e) {
                            System.out.println("ERROR: Failed to send winning message!");
                        }
                    break;
                    // Send GameOverMessage to losing player
                    // Later update result, finalBoard to not be blank
                    // GameOverMessage(String gameId, String result, String finalBoard)
                    case -1:
                        try {
                            client.send(client.getClientId(), new GameOverMessage(gameId, "", ""));
                        } catch (IOException e) {
                            System.out.println("ERROR: Failed to send losing message!");
                        }
                    break;
                    // No win condition, exit switch
                    default:
                }
        }
        // Clean up game
        cleanGame(client, gameId, channel, games);
        return;
    }

    // cleanGame function
    // Cleans up the game after win conditions
    // Unsubscribes game controller from the game channel
    // Deletes the game from concurrent hash map
    private static void cleanGame(RouterClient client, String gameId, String channel, ConcurrentHashMap<String, Game> games) {
        // Unsubscribe the game controller from the game
        try {
            client.unsubscribe(channel);
        } catch (IOException e) {
            System.err.println("ERROR: invalid channel!");
        }
        // Remove the game from the concurrent hash map
        games.remove(gameId);
    }
}
