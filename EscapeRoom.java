/*
* Problem 1: Escape Room
* 
* V1.0
* 10/10/2019
* Copyright(c) 2019 PLTW to present. All rights reserved
*/
import java.util.Scanner;

/**
 * Create an escape room game where the player must navigate
 * to the other side of the screen in the fewest steps, while
 * avoiding obstacles and collecting prizes.
 */
public class EscapeRoom
{

      // describe the game with brief welcome message
      // determine the size (length and width) a player must move to stay within the grid markings
      // Allow game commands:
      //    right, left, up, down: if you try to go off grid or bump into wall, score decreases
      //    jump over 1 space: you cannot jump over walls
      //    if you land on a trap, spring a trap to increase score: you must first check if there is a trap, if none exists, penalty
      //    pick up prize: score increases, if there is no prize, penalty
      //    help: display all possible commands
      //    end: reach the far right wall, score increase, game ends, if game ended without reaching far right wall, penalty
      //    replay: shows number of player steps and resets the board, you or another player can play the same board
      // Note that you must adjust the score with any method that returns a score
      // Optional: create a custom image for your player use the file player.png on disk
    
      /**** provided code:
      // set up the game
      boolean play = true;
      while (play)
      {
        // get user input and call game methods to play 
        play = false;
      }
      */

  public static void main(String[] args) 
  {      
    // welcome message
    System.out.println("Welcome to EscapeRoom!");
    System.out.println("Get to the other side of the room, avoiding walls and invisible traps,");
    System.out.println("pick up all the prizes.\n");
  System.out.println("Controls: You can also use the keyboard when the game window is focused:\n  Arrow keys to move, Shift+Arrow to jump, 'p' to pickup, 'r' to replay, 'q' to quit.\n");
    
    GameGUI game = new GameGUI();
    game.createBoard();
  // automatically start/restart the board on first run so the game is initialized
  game.replay();

    // size of move
    int m = 60; 
    // individual player moves
    int px = 0;
    int py = 0; 
    
    int score = 0;

    Scanner in = new Scanner(System.in);
  String[] validCommands = { "right", "left", "up", "down", "r", "l", "u", "d",
  "jump", "jr", "jumpleft", "jl", "jumpup", "ju", "jumpdown", "jd",
  "pickup", "p", "quit", "q", "replay", "help", "?"};
  
    // set up game
    boolean play = true;
    while (play)
    {
      // prompt
      // stop the loop if the GUI window was closed by the user
      if (game.isUserRequestedClose())
      {
        play = false;
        break;
      }
      // if replay was triggered from GUI or console, reset console score to match GUI
      if (game.consumeReplayedFlag())
      {
        score = 0;
      }
      System.out.print("> ");
      String cmd = UserInput.getValidInput(validCommands);

      switch(cmd)
      {
        case "right":
        case "r":
          int deltaR = game.movePlayer(m, 0);
          score += deltaR;
          game.addToScore(deltaR);
          break;
        case "left":
        case "l":
          int deltaL = game.movePlayer(-m, 0);
          score += deltaL;
          game.addToScore(deltaL);
          break;
        case "up":
        case "u":
          int deltaU = game.movePlayer(0, -m);
          score += deltaU;
          game.addToScore(deltaU);
          break;
        case "down":
        case "d":
          int deltaD = game.movePlayer(0, m);
          score += deltaD;
          game.addToScore(deltaD);
          break;

        // jumps: move two spaces (2*m)
        case "jump":
        case "jr":
          int deltaJR = game.movePlayer(2*m, 0);
          score += deltaJR;
          game.addToScore(deltaJR);
          break;
        case "jumpleft":
        case "jl":
          int deltaJL = game.movePlayer(-2*m, 0);
          score += deltaJL;
          game.addToScore(deltaJL);
          break;
        case "jumpup":
        case "ju":
          int deltaJU = game.movePlayer(0, -2*m);
          score += deltaJU;
          game.addToScore(deltaJU);
          break;
        case "jumpdown":
        case "jd":
          int deltaJD = game.movePlayer(0, 2*m);
          score += deltaJD;
          game.addToScore(deltaJD);
          break;

        case "pickup":
        case "p":
          int d = game.pickupPrize();
          score += d;
          game.addToScore(d);
          break;

        case "replay":
          int r = game.replay();
          score += r;
          game.addToScore(r);
          break;

        case "help":
        case "?":
          System.out.println("Commands:");
          System.out.println("  right/r, left/l, up/u, down/d    - move one space");
          System.out.println("  jump/jr, jumpleft/jl, jumpup/ju, jumpdown/jd - jump two spaces");
          System.out.println("  pickup/p                       - pick up prize at current location");
          System.out.println("  replay                         - reset the board (penalty if not at end)");
          System.out.println("  quit/q                         - quit the game");
          break;

        case "quit":
        case "q":
          // close GUI as well
          game.closeWindow();
          play = false;
          break;

        default:
          // should not occur because UserInput validates commands
          System.out.println("Unknown command: " + cmd);
      }

      // display current score and steps after each action
  System.out.println("score=" + score + " steps=" + game.getSteps() + " coins=" + game.getCoinsCollected());
    
      
    }

  

    score += game.endGame();

    System.out.println("score=" + score);
  System.out.println("steps=" + game.getSteps() + " coins=" + game.getCoinsCollected());
  }
}

        