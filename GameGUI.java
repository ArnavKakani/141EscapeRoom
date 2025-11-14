import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

import java.io.File;
import javax.imageio.ImageIO;

import java.util.Random;

/**
 * A Game board on which to place and move players.
 * 
 * @author PLTW
 * @version 1.0
 */
public class GameGUI extends JComponent implements KeyListener
{
  static final long serialVersionUID = 141L; // problem 1.4.1

  private static final int WIDTH = 510;
  private static final int HEIGHT = 360;
  // extra room on the right for a score/points panel
  private static final int SIDEBAR_WIDTH = 200;
  private static final int WINDOW_WIDTH = WIDTH + SIDEBAR_WIDTH;
  // slightly larger sidebar for clearer counters
  // (we'll update SIDEBAR_WIDTH value below)
  private static final int SPACE_SIZE = 60;
  private static final int GRID_W = 8;
  private static final int GRID_H = 5;
  private static final int START_LOC_X = 15;
  private static final int START_LOC_Y = 15;
  
  // initial placement of player
  int x = START_LOC_X; 
  int y = START_LOC_Y;

  // grid image to show in background
  private Image bgImage;

  // player image and info
  private Image player;
  private Point playerLoc;
  private int playerSteps;
  // score shown in GUI (keeps track of points from GUI-driven or console-driven actions)
  private int guiScore = 0;
  // number of prizes picked up
  private int coinsCollected = 0;

  // walls, prizes, traps
  private int totalWalls;
  private Rectangle[] walls; 
  private Image prizeImage;
  private int totalPrizes;
  private Rectangle[] prizes;
  private int totalTraps;
  private Rectangle[] traps;

  // scores, sometimes awarded as (negative) penalties
  private int prizeVal = 10;
  private int trapVal = 5;
  private int endVal = 10;
  private int offGridVal = 5; // penalty only
  private int hitWallVal = 5;  // penalty only

  // game frame
  private JFrame frame;
  // indicates if the user requested the window to close (via key or window)
  private volatile boolean userRequestedClose = false;
  // flag set when replay() is called so external controllers can react
  private volatile boolean replayedFlag = false;

  /**
   * Constructor for the GameGUI class.
   * Creates a frame with a background image and a player that will move around the board.
   */
  public GameGUI()
  {
    
    try {
      bgImage = ImageIO.read(new File("grid.png"));      
    } catch (Exception e) {
      System.err.println("Could not open file grid.png");
    }      
    try {
      prizeImage = ImageIO.read(new File("coin.png"));      
    } catch (Exception e) {
      System.err.println("Could not open file coin.png");
    }
  
    // player image, student can customize this image by changing file on disk
    try {
      player = ImageIO.read(new File("player.png"));      
    } catch (Exception e) {
     System.err.println("Could not open file player.png");
    }
    // save player location
    playerLoc = new Point(x,y);

    // create the game frame
    frame = new JFrame();
    frame.setTitle("EscapeRoom");
  frame.setSize(WINDOW_WIDTH, HEIGHT);
  // don't exit JVM immediately when the window closes; allow the main loop to handle shutdown
  frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.add(this);
    frame.setVisible(true);
    frame.setResizable(false); 

  // enable keyboard input for the game component
  this.setFocusable(true);
  this.requestFocusInWindow();
  this.addKeyListener(this);

    // handle native window close to mark that the user requested close
    frame.addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent e) {
        userRequestedClose = true;
      }
      @Override
      public void windowClosed(java.awt.event.WindowEvent e) {
        userRequestedClose = true;
      }
    });

    // set default config
    totalWalls = 20;
    totalPrizes = 3;
    totalTraps = 5;
  }

  @Override
  public java.awt.Dimension getPreferredSize()
  {
    return new java.awt.Dimension(WINDOW_WIDTH, HEIGHT);
  }

 /**
  * After a GameGUI object is created, this method adds the walls, prizes, and traps to the gameboard.
  * Note that traps and prizes may occupy the same location.
  */
  public void createBoard()
  {
    traps = new Rectangle[totalTraps];
    createTraps();
    
    prizes = new Rectangle[totalPrizes];
    createPrizes();

    walls = new Rectangle[totalWalls];
    createWalls();
  }

  /**
   * Increment/decrement the player location by the amount designated.
   * This method checks for bumping into walls and going off the grid,
   * both of which result in a penalty.
   * <P>
   * precondition: amount to move is not larger than the board, otherwise player may appear to disappear
   * postcondition: increases number of steps even if the player did not actually move (e.g. bumping into a wall)
   * <P>
   * @param incrx amount to move player in x direction
   * @param incry amount to move player in y direction
   * @return penalty score for hitting a wall or potentially going off the grid, 0 otherwise
   */
  public int movePlayer(int incrx, int incry)
  {
      int newX = x + incrx;
      int newY = y + incry;
      
      // increment regardless of whether player really moves
      playerSteps++;

      // check if off grid horizontally and vertically
      if ( (newX < 0 || newX > WIDTH-SPACE_SIZE) || (newY < 0 || newY > HEIGHT-SPACE_SIZE) )
      {
        System.out.println ("OFF THE GRID!");
        return -offGridVal;
      }

      // determine if a wall is in the way
      for (Rectangle r: walls)
      {
        // this rect. location
        int startX =  (int)r.getX();
        int endX  =  (int)r.getX() + (int)r.getWidth();
        int startY =  (int)r.getY();
        int endY = (int) r.getY() + (int)r.getHeight();

        // (Note: the following if statements could be written as huge conditional but who wants to look at that!?)
        // moving RIGHT, check to the right
        if ((incrx > 0) && (x <= startX) && (startX <= newX) && (y >= startY) && (y <= endY))
        {
          System.out.println("A WALL IS IN THE WAY");
          return -hitWallVal;
        }
        // moving LEFT, check to the left
        else if ((incrx < 0) && (x >= startX) && (startX >= newX) && (y >= startY) && (y <= endY))
        {
          System.out.println("A WALL IS IN THE WAY");
          return -hitWallVal;
        }
        // moving DOWN check below
        else if ((incry > 0) && (y <= startY && startY <= newY && x >= startX && x <= endX))
        {
          System.out.println("A WALL IS IN THE WAY");
          return -hitWallVal;
        }
        // moving UP check above
        else if ((incry < 0) && (y >= startY) && (startY >= newY) && (x >= startX) && (x <= endX))
        {
          System.out.println("A WALL IS IN THE WAY");
          return -hitWallVal;
        }     
      }

      // all is well, move player
      x += incrx;
      y += incry;

      // update player location immediately so checks use the current spot
      playerLoc.setLocation(x,y);

      // after moving, automatically activate traps (if any) at the new location
      int delta = 0;
      boolean trapFound = false;
      if (traps != null)
      {
        for (Rectangle t: traps)
        {
          if (t != null && t.getWidth() > 0 && t.contains(x, y))
          {
            t.setSize(0,0);
            System.out.println("TRAP IS SPRUNG!");
            delta += trapVal;
            trapFound = true;
            // do not break; multiple traps could be present but generally one
          }
        }
      }

      // if no trap hid the prize, auto-pickup any prize at this location (no penalty when none)
      if (!trapFound && prizes != null)
      {
        for (Rectangle p: prizes)
        {
          if (p != null && p.getWidth() > 0 && p.contains(x, y))
          {
            p.setSize(0,0);
            coinsCollected++;
            System.out.println("YOU PICKED UP A PRIZE!");
            delta += prizeVal;
            // break so multiple prizes at same tile aren't double-counted
            break;
          }
        }
      }

      repaint();   
      return delta;   
  }

  /**
   * Add delta to GUI-visible score and repaint sidebar.
   */
  public void addToScore(int delta)
  {
    guiScore += delta;
    repaint();
  }

  /**
   * Convenience method to allow external callers (like EscapeRoom) to
   * programmatically move the player using grid steps. This simply
   * delegates to movePlayer using the grid-space size.
   */
  public int movePlayerByCells(int cellsX, int cellsY)
  {
    return movePlayer(cellsX * SPACE_SIZE, cellsY * SPACE_SIZE);
  }

  /**
   * Allow external callers to close the frame (used by quit action).
   */
  public void closeWindow()
  {
    userRequestedClose = true;
    setVisible(false);
    frame.dispose();
  }

  /**
   * Returns true if the user has requested the window be closed (via key or native window).
   */
  public boolean isUserRequestedClose()
  {
    return userRequestedClose;
  }

  /*-------------- KeyListener methods ----------------*/
  @Override
  public void keyPressed(KeyEvent e)
  {
    // arrow keys: single-step moves; with shift pressed -> jump (2 cells)
    int dx = 0;
    int dy = 0;
    int multiplier = e.isShiftDown() ? 2 : 1;

    switch (e.getKeyCode())
    {
      case KeyEvent.VK_RIGHT:
        dx = multiplier * SPACE_SIZE;
        break;
      case KeyEvent.VK_LEFT:
        dx = -multiplier * SPACE_SIZE;
        break;
      case KeyEvent.VK_UP:
        dy = -multiplier * SPACE_SIZE;
        break;
      case KeyEvent.VK_DOWN:
        dy = multiplier * SPACE_SIZE;
        break;
      case KeyEvent.VK_P:
        int deltaP = pickupPrize();
        addToScore(deltaP);
        return;
      case KeyEvent.VK_R:
        replay();
        return;
      case KeyEvent.VK_Q:
        closeWindow();
        return;
      default:
        return;
    }

    // perform move and add any penalty/bonus to GUI score
    int delta = movePlayer(dx, dy);
    addToScore(delta);
  }

  @Override
  public void keyReleased(KeyEvent e) { }

  @Override
  public void keyTyped(KeyEvent e) { }

  /**
   * Check the space adjacent to the player for a trap. The adjacent location is one space away from the player, 
   * designated by newx, newy.
   * <P>
   * precondition: newx and newy must be the amount a player regularly moves, otherwise an existing trap may go undetected
   * <P>
   * @param newx a location indicating the space to the right or left of the player
   * @param newy a location indicating the space above or below the player
   * @return true if the new location has a trap that has not been sprung, false otherwise
   */
  public boolean isTrap(int newx, int newy)
  {
    double px = playerLoc.getX() + newx;
    double py = playerLoc.getY() + newy;


    for (Rectangle r: traps)
    {
      // DEBUG: System.out.println("trapx:" + r.getX() + " trapy:" + r.getY() + "\npx: " + px + " py:" + py);
      // zero size traps have already been sprung, ignore
      if (r.getWidth() > 0)
      {
        // if new location of player has a trap, return true
        if (r.contains(px, py))
        {
          System.out.println("A TRAP IS AHEAD");
          return true;
        }
      }
    }
    // there is no trap where player wants to go
    return false;
  }

  /**
   * Spring the trap. Traps can only be sprung once and attempts to spring
   * a sprung task results in a penalty.
   * <P>
   * precondition: newx and newy must be the amount a player regularly moves, otherwise an existing trap may go unsprung
   * <P>
   * @param newx a location indicating the space to the right or left of the player
   * @param newy a location indicating the space above or below the player
   * @return a positive score if a trap is sprung, otherwise a negative penalty for trying to spring a non-existent trap
   */
  public int springTrap(int newx, int newy)
  {
    double px = playerLoc.getX() + newx;
    double py = playerLoc.getY() + newy;

    // check all traps, some of which may be already sprung
    for (Rectangle r: traps)
    {
      // DEBUG: System.out.println("trapx:" + r.getX() + " trapy:" + r.getY() + "\npx: " + px + " py:" + py);
      if (r.contains(px, py))
      {
        // zero size traps indicate it has been sprung, cannot spring again, so ignore
        if (r.getWidth() > 0)
        {
          r.setSize(0,0);
          System.out.println("TRAP IS SPRUNG!");
          return trapVal;
        }
      }
    }
    // no trap here, penalty
    System.out.println("THERE IS NO TRAP HERE TO SPRING");
    return -trapVal;
  }

  /**
   * Pickup a prize and score points. If no prize is in that location, this results in a penalty.
   * <P>
   * @return positive score if a location had a prize to be picked up, otherwise a negative penalty
   */
  public int pickupPrize()
  {
    double px = playerLoc.getX();
    double py = playerLoc.getY();

    for (Rectangle p: prizes)
    {
      // DEBUG: System.out.println("prizex:" + p.getX() + " prizey:" + p.getY() + "\npx: " + px + " py:" + py);
      // if location has a prize, pick it up
      if (p.getWidth() > 0 && p.contains(px, py))
      {
        System.out.println("YOU PICKED UP A PRIZE!");
        p.setSize(0,0);
        // increment coin counter shown in GUI
        coinsCollected++;
        repaint();
        return prizeVal;
      }
    }
    System.out.println("OOPS, NO PRIZE HERE");
    return -prizeVal;  
  }

  /**
   * Return the number of prizes picked up so far.
   */
  public int getCoinsCollected()
  {
    return coinsCollected;
  }

  /**
   * Return the numbers of steps the player has taken.
   * <P>
   * @return the number of steps
   */
  public int getSteps()
  {
    return playerSteps;
  }

  public int getRemainingPrizes()
  {
    int c = 0;
    if (prizes == null) return 0;
    for (Rectangle p: prizes)
      if (p != null && p.getWidth() > 0 && p.getHeight() > 0) c++;
    return c;
  }

  public int getRemainingTraps()
  {
    int c = 0;
    if (traps == null) return 0;
    for (Rectangle t: traps)
      if (t != null && t.getWidth() > 0 && t.getHeight() > 0) c++;
    return c;
  }

  public int getRemainingWalls()
  {
    int c = 0;
    if (walls == null) return 0;
    for (Rectangle w: walls)
      if (w != null && w.getWidth() > 0 && w.getHeight() > 0) c++;
    return c;
  }
  
  /**
   * Set the designated number of prizes in the game.  This can be used to customize the gameboard configuration.
   * <P>
   * precondition p must be a positive, non-zero integer
   * <P>
   * @param p number of prizes to create
   */
  public void setPrizes(int p) 
  {
    totalPrizes = p;
  }
  
  /**
   * Set the designated number of traps in the game. This can be used to customize the gameboard configuration.
   * <P>
   * precondition t must be a positive, non-zero integer
   * <P>
   * @param t number of traps to create
   */
  public void setTraps(int t) 
  {
    totalTraps = t;
  }
  
  /**
   * Set the designated number of walls in the game. This can be used to customize the gameboard configuration.
   * <P>
   * precondition t must be a positive, non-zero integer
   * <P>
   * @param w number of walls to create
   */
  public void setWalls(int w) 
  {
    totalWalls = w;
  }

  /**
   * Reset the board to replay existing game. The method can be called at any time but results in a penalty if called
   * before the player reaches the far right wall.
   * <P>
   * @return positive score for reaching the far right wall, penalty otherwise
   */
  public int replay()
  {

    int win = playerAtEnd();
  
    // resize prizes and traps to "reactivate" them
    for (Rectangle p: prizes)
      p.setSize(SPACE_SIZE/3, SPACE_SIZE/3);
    for (Rectangle t: traps)
      t.setSize(SPACE_SIZE/3, SPACE_SIZE/3);

    // move player to start of board
    x = START_LOC_X;
    y = START_LOC_Y;
    playerSteps = 0;
    // reset GUI-visible score and coins when replaying
    guiScore = 0;
    coinsCollected = 0;
    replayedFlag = true;
    repaint();
    return win;
  }

  /**
   * Returns true if replay() was called since last checked.
   * External code should call this and then clear/handle the replay event.
   */
  public boolean consumeReplayedFlag()
  {
    boolean v = replayedFlag;
    replayedFlag = false;
    return v;
  }

 /**
  * End the game, checking if the player made it to the far right wall.
  * <P>
  * @return positive score for reaching the far right wall, penalty otherwise
  */
  public int endGame() 
  {
    int win = playerAtEnd();
  
    setVisible(false);
    frame.dispose();
    return win;
  }

  /*------------------- public methods not to be called as part of API -------------------*/

  /** 
   * For internal use and should not be called directly: Users graphics buffer to paint board elements.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;

    // draw grid
    g.drawImage(bgImage, 0, 0, null);
    // draw sidebar background (slightly darker for contrast)
    g2.setPaint(new Color(0xE8E8E8));
    g2.fillRect(WIDTH, 0, SIDEBAR_WIDTH, HEIGHT);
    g2.setPaint(Color.DARK_GRAY);
    g2.drawRect(WIDTH, 0, SIDEBAR_WIDTH-1, HEIGHT-1);

    // add (invisible) traps
    for (Rectangle t : traps)
    {
      // some entries may be null if board wasn't fully initialized; guard against that
      if (t != null && t.getWidth() > 0 && t.getHeight() > 0)
      {
        g2.setPaint(Color.WHITE); 
        g2.fill(t);
      }
    }

    // add prizes
    for (Rectangle p : prizes)
    {
      // picked up prizes are 0 size so don't render
      if (p != null && p.getWidth() > 0 && p.getHeight() > 0) 
      {
        int px = (int)p.getX();
        int py = (int)p.getY();
        g.drawImage(prizeImage, px, py, null);
      }
    }

    // add walls
    for (Rectangle r : walls) 
    {
      if (r != null && r.getWidth() > 0 && r.getHeight() > 0)
      {
        g2.setPaint(Color.BLACK);
        g2.fill(r);
      }
    }
   
    // draw player, saving its location
    g.drawImage(player, x, y, 40,40, null);
    playerLoc.setLocation(x,y);

  // draw sidebar header
  g2.setPaint(Color.BLACK);
  g2.setFont(g2.getFont().deriveFont(16f).deriveFont(java.awt.Font.BOLD));
  g2.drawString("EscapeRoom", WIDTH + 12, 26);

  // big score display
  g2.setFont(g2.getFont().deriveFont(14f));
  g2.setPaint(new Color(0x1A73E8)); // blue score
  g2.drawString("Score:", WIDTH + 12, 56);
  g2.setFont(g2.getFont().deriveFont(18f).deriveFont(java.awt.Font.BOLD));
  g2.drawString(Integer.toString(guiScore), WIDTH + 12, 80);

  // small counters
  g2.setFont(g2.getFont().deriveFont(12f).deriveFont(java.awt.Font.PLAIN));
  g2.setPaint(Color.BLACK);
  g2.drawString("Steps: " + playerSteps, WIDTH + 12, 105);
  g2.drawString("Coins: " + coinsCollected, WIDTH + 12, 125);

  // remaining items
  int remPrizes = getRemainingPrizes();
  int remTraps = getRemainingTraps();
  int remWalls = getRemainingWalls();
  g2.drawString("Prizes left: " + remPrizes, WIDTH + 12, 150);
  g2.drawString("Traps left: " + remTraps, WIDTH + 12, 170);
  g2.drawString("Walls: " + remWalls, WIDTH + 12, 190);

  // controls and legend (smaller, muted)
  g2.setPaint(Color.DARK_GRAY);
  g2.setFont(g2.getFont().deriveFont(11f));
  g2.drawString("Controls:", WIDTH + 12, 215);
  g2.drawString("Arrow: move  Shift+Arrow: jump", WIDTH + 12, 235);
  g2.drawString("P: pickup  R: replay  Q: quit", WIDTH + 12, 250);
  g2.drawString("Prize:+" + prizeVal + "  Trap:+" + trapVal, WIDTH + 12, 270);
  g2.drawString("Wall:-" + hitWallVal + "  Off-grid:-" + offGridVal, WIDTH + 12, 285);
  }

  /*------------------- private methods -------------------*/

  /*
   * Add randomly placed prizes to be picked up.
   * Note:  prizes and traps may occupy the same location, with traps hiding prizes
   */
  private void createPrizes()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
     for (int numPrizes = 0; numPrizes < totalPrizes; numPrizes++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      Rectangle r;
      r = new Rectangle((w*s + 15),(h*s + 15), 15, 15);
      prizes[numPrizes] = r;
     }
  }

  /*
   * Add randomly placed traps to the board. They will be painted white and appear invisible.
   * Note:  prizes and traps may occupy the same location, with traps hiding prizes
   */
  private void createTraps()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
     for (int numTraps = 0; numTraps < totalTraps; numTraps++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      Rectangle r;
      r = new Rectangle((w*s + 15),(h*s + 15), 15, 15);
      traps[numTraps] = r;
     }
  }

  /*
   * Add walls to the board in random locations 
   */
  private void createWalls()
  {
     int s = SPACE_SIZE; 

     Random rand = new Random();
     for (int numWalls = 0; numWalls < totalWalls; numWalls++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      Rectangle r;
       if (rand.nextInt(2) == 0) 
       {
         // vertical wall
         r = new Rectangle((w*s + s - 5),h*s, 8,s);
       }
       else
       {
         /// horizontal
         r = new Rectangle(w*s,(h*s + s - 5), s, 8);
       }
       walls[numWalls] = r;
     }
  }

  /**
   * Checks if player as at the far right of the board 
   * @return positive score for reaching the far right wall, penalty otherwise
   */
  private int playerAtEnd() 
  {
    int score;

    double px = playerLoc.getX();
    if (px > (WIDTH - 2*SPACE_SIZE))
    {
      System.out.println("YOU MADE IT!");
      score = endVal;
    }
    else
    {
      System.out.println("OOPS, YOU QUIT TOO SOON!");
      score = -endVal;
    }
    return score;
  
  }
}