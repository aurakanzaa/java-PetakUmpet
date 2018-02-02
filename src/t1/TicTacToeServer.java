package t1;

/**
 *
 * @author acer
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
public class TicTacToeServer {

    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(8901);
        System.out.println("Petak Umpet server berjalan");
        try {
            while (true) {
                Game game = new Game();
                Game.Player playerX = game.new Player(listener.accept(), 'X');
                Game.Player playerO = game.new Player(listener.accept(), 'O');
                Game.Player playerY = game.new Player(listener.accept(), 'Y');
                playerX.setOpponent(playerO);
                playerO.setOpponent(playerY);
                playerY.setOpponent(playerX);
                game.currentPlayer = playerX;
                playerX.start();
                playerO.start();
                playerY.start();
            }
        } finally {
            listener.close();
        }
    }
}

class Game {
    // a board of 9 squares
    private Player[] board = {
        null, null, null,
        null, null, null,
        null, null, null};

    //current player
    Player currentPlayer;    
    Player playerOpponent;        
    Player playerOpponent2;
    public int c=0;

    // winner
    public boolean hasWinner(int location) {
        return
            (playerOpponent.place==location || playerOpponent2.place==location);
    }

    // no empty squares
    public boolean boardFilledUp() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        return true;
    }
    // thread when player tries a move
    public synchronized boolean legalMove(int location, Player player) {
        if (player == currentPlayer) {
            board[location] = currentPlayer;
            playerOpponent = currentPlayer.opponent;
            playerOpponent2=playerOpponent.opponent;
            currentPlayer=playerOpponent;
            currentPlayer.otherPlayerMoved(location);
            playerOpponent.opponent.otherPlayerMoved(location);                                          
            c++;
            return true;
        }
        return false;
    }
    class Player extends Thread {
        char mark;
        Player opponent;
        int place;
        Socket socket;
        BufferedReader input;
        PrintWriter output;
        // thread handler to initialize stream fields
        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE Menunggu pemain lain terhubung");
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            }
        }
        //Accepts notification of who the opponent is.
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }
        
        public void setPlace(int x) {
            this.place = x;
        }
       
         //Handles the otherPlayerMoved message. 
        public void otherPlayerMoved(int location) {
            if (currentPlayer==opponent.opponent){
                output.println("OPPONENT_MOVED " + location);
            }else{
                output.println("OPPONENT2_MOVED " + location);
            }
            if(c>=3){
                output.println(hasWinner(location) ? "DEFEAT" : boardFilledUp() ? "TIE" : "");
            }
        }
        
        public void run() {
            try {
                // The thread is only started after everyone connects.
                output.println("MESSAGE Semua pemain terhubung");

                // Tell the first player that it is his/her turn.
                if (mark == 'X') {
                    output.println("MESSAGE Guliran anda");
                }                
                
                // Repeatedly get commands from the client and process them.
                while (true) {
                    String command = input.readLine(); 
                    if (command.startsWith("MOVE")) { 
                        int location = Integer.parseInt(command.substring(5));
                            if (legalMove(location, this)) {
                                if(c<=3){
                                    output.println("VALID2_MOVE"+c);
                                    currentPlayer.setPlace(location);
                                } else{
                                    output.println("VALID_MOVE"+c);
                                    output.println(hasWinner(location) ? "VICTORY"
                                    : boardFilledUp() ? "TIE"
                                    : "");                             
                                }
                            } else {
                                output.println("MESSAGE ?");
                            }
                        
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("Player died: " + e);
            } finally {
                try {socket.close();} catch (IOException e) {}
            }
        }
    }
}
