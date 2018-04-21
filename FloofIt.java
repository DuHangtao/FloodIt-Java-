import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import javalib.worldimages.*;
import javalib.impworld.*;
import java.awt.Color;

//--------------------- Cell -----------------------

//Represents a single square of the game area
class Cell {

  // In logical coordinates, with the origin at the top-left corner of the
  // screen
  int x;
  int y;
  Color color;
  boolean flooded;
  // the four adjacent cells to this one
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
    this.left = null;
    this.top = null;
    this.right = null;
    this.bottom = null;
  }

  // to draw a single cell
  public WorldImage drawCell() {
    if (this.flooded) {
      return new OverlayImage(new TextImage("X", Color.WHITE),
          new RectangleImage(20, 20, OutlineMode.SOLID, this.color));
    }
    else {
      return new RectangleImage(20, 20, OutlineMode.SOLID, this.color);
    }
  }
}

// --------------------- FloodItWorld -----------------------

class FloodItWorld extends World {
  ArrayList<ArrayList<Cell>> board;
  int size;
  int color;
  Color currentcolor;

  // constructor
  FloodItWorld(int size, int color) {
    this.size = size;
    this.color = color;
    this.generateBoard();
    this.addNeighbors();
  }

  // --------------------- Constants -----------------------
  int score = 0;
  int timer = 0;
  int timeclick = 0;
  static int BOARD_SIZE = 22;
  // an example of all colors
  ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.BLACK, Color.BLUE, Color.GRAY,
      Color.GREEN, Color.ORANGE, Color.PINK, Color.YELLOW, Color.RED, Color.WHITE, Color.CYAN,
      Color.DARK_GRAY, Color.LIGHT_GRAY, Color.MAGENTA));

  // --------------------- To-Draw -----------------------

  // to generate whole board
  public void generateBoard() {
    ArrayList<Cell> arraycells = new ArrayList<Cell>();
    ArrayList<ArrayList<Cell>> result = new ArrayList<ArrayList<Cell>>();
    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        arraycells.add(new Cell(x, y, this.colors.get(new Random().nextInt(color)), false));
      }
      result.add(arraycells);
      arraycells = new ArrayList<Cell>();
    }
    result.get(0).get(0).flooded = true;
    currentcolor = result.get(0).get(0).color;
    this.board = result;
    this.addNeighbors();
  }

  // draw all the cells on the board
  public WorldScene makeScene() {
    WorldScene bg = getEmptyScene();
    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        Cell cell = board.get(x).get(y);
        bg.placeImageXY(cell.drawCell(), cell.x * 20 + 10, cell.y * 20 + 10);
        bg.placeImageXY(new TextImage(
            Integer.toString(timeclick) + "/" + Integer.toString(new Utils().maxsteps(size, color)),
            55, FontStyle.BOLD, Color.BLACK), 220, 500);
        bg.placeImageXY(
            new TextImage("Time: " + Integer.toString(timer / 10), 55, FontStyle.BOLD, Color.BLACK),
            220, 550);
        bg.placeImageXY(new TextImage("Score: " + Integer.toString(timeclick * 10), 55,
            FontStyle.BOLD, Color.BLACK), 220, 600);
        if (timeclick > new Utils().maxsteps(size, color)) {
          bg.placeImageXY(new TextImage("You Lose >_<|||", 50, FontStyle.BOLD, Color.RED), 440 / 2,
              665);
        }
        if (timeclick <= new Utils().maxsteps(size, color) && this.allsamecolor()) {
          bg.placeImageXY(new TextImage("You Win !!!!!!!!!!!!", 50, FontStyle.BOLD, Color.BLUE),
              440 / 2, 665);
        }
      }
    }
    return bg;
  }

  // --------------------- On-Mouse -----------------------

  // to active the mouse event
  public void onMouseClicked(Posn p) {

    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        Cell currentcell = board.get(x).get(y);
        if (currentcell.x * 20 <= p.x && p.x < currentcell.x * 20 + 20 && currentcell.y * 20 <= p.y
            && p.y < currentcell.y * 20 + 20) {
          currentcolor = currentcell.color;
          if (!(currentcell.flooded)) {
            if (!(board.get(0).get(0).color.equals(board.get(p.x / 20).get(p.y / 20).color))) {
              timeclick++;
            }
          }
        }
      }
    }
  }

  // --------------------- On-Tick -----------------------

  // to update the state of the current cell and it neighbors
  public void updateFlood(Color c) {
    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        Cell cur = board.get(x).get(y);
        boolean curFlooded = board.get(x).get(y).flooded;
        if (curFlooded) {
          cur.color = this.currentcolor;
          if (cur.top != null && cur.top.color.equals(c)) {
            cur.top.flooded = true;
          }
          if (cur.bottom != null && cur.bottom.color.equals(c)) {
            cur.bottom.flooded = true;
          }
          if (cur.left != null && cur.left.color.equals(c)) {
            cur.left.flooded = true;
          }
          if (cur.right != null && cur.right.color.equals(c)) {
            cur.right.flooded = true;
          }
        }
      }
    }
  }

  // on-tick function/ to update the world state
  public void onTick() {
    updateFlood(currentcolor);
    if (!this.allsamecolor()) {
      timer++;
    }
  }

  // --------------------- On-KeyEvent -----------------------

  // to change the state of the flood world when press the key
  public void onKeyEvent(String key) {
    if (key.equals("e")) {
      this.generateBoard();
      timer = 0;
      timeclick = 0;
      score = 0;
    }
  }

  // --------------------- Utility Methods -----------------------

  // to update the top, left, right, bottom of the current cell
  public void addNeighbors() {
    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        Cell current = this.board.get(x).get(y);
        if (y == 0) {
          current.top = null;
        }
        else {
          current.top = this.board.get(x).get(y - 1);
        }
        if (y >= size - 1) {
          current.right = null;
        }
        else {
          current.bottom = this.board.get(x).get(y + 1);
        }
        if (x == 0) {
          current.left = null;
        }
        else {
          current.left = this.board.get(x - 1).get(y);
        }
        if (x >= size - 1) {
          current.right = null;
        }
        else {
          current.right = this.board.get(x + 1).get(y);
        }
      }
    }
  }

  // to check if all the cells have the same color
  public boolean allsamecolor() {
    boolean result = true;
    for (int x = 0; x < size; x++) {
      for (int y = 0; y < size; y++) {
        Cell currentcell = board.get(x).get(y);
        if (!currentcell.color.equals(currentcolor)) {
          result = false;
        }
      }
    }
    return result;
  }
}

// a utility class
class Utils {

  // to calculate the maximum steps that allowed to click
  public int maxsteps(int size, int numcolors) {
    return (size * 4) / 2 + (numcolors * 5) / 3;
  }
}

// --------------------- Examples / Testing -----------------------

// Examples

class ExampleFloodIt {

  // examples of cells
  Cell c1;
  Cell c2 = new Cell(0, 0, Color.BLACK, true);
  Cell c3 = new Cell(0, 1, Color.DARK_GRAY, false);
  Cell c4 = new Cell(1, 0, Color.BLUE, false);
  Cell c5 = new Cell(1, 1, Color.BLACK, false);
  Cell c6 = new Cell(0, 0, Color.BLACK, true);
  Cell c7 = new Cell(0, 1, Color.BLACK, false);
  Cell c8 = new Cell(1, 0, Color.BLACK, false);
  Cell c9 = new Cell(1, 1, Color.BLACK, false);

  Cell c10 = new Cell(1, 1, Color.YELLOW, true);

  //
  Utils u1 = new Utils();

  // examples of arraylist of cells
  ArrayList<Cell> l1 = new ArrayList<Cell>(Arrays.asList(c2, c3));
  ArrayList<Cell> l2 = new ArrayList<Cell>(Arrays.asList(c2));
  ArrayList<Cell> l3 = new ArrayList<Cell>(Arrays.asList(c2, c3));
  ArrayList<Cell> l4 = new ArrayList<Cell>(Arrays.asList(c4, c5));
  ArrayList<Cell> l5 = new ArrayList<Cell>(Arrays.asList(c6, c7));
  ArrayList<Cell> l6 = new ArrayList<Cell>(Arrays.asList(c8, c9));

  // examples of arraylist of arraylist of cells
  ArrayList<ArrayList<Cell>> ll1 = new ArrayList<ArrayList<Cell>>(Arrays.asList(l2));
  ArrayList<ArrayList<Cell>> ll2 = new ArrayList<ArrayList<Cell>>(Arrays.asList(l3, l4));
  ArrayList<ArrayList<Cell>> ll3 = new ArrayList<ArrayList<Cell>>(Arrays.asList(l5, l6));

  // examples of FloodItWorld
  FloodItWorld f1;
  FloodItWorld f2;
  FloodItWorld f3;
  FloodItWorld f4;

  // the initial states
  void initialstates() {

    // initialize the examples of cells
    c1 = new Cell(0, 0, Color.BLACK, false);

    // initialize the examples of FloodItWorld
    f1 = new FloodItWorld(22, 9);
    f2 = new FloodItWorld(2, 9);
    f3 = new FloodItWorld(15, 7);
    f4 = new FloodItWorld(1, 5);
  }

  // testing

  // test generateBoard
  void testgenerateBoard(Tester t) {
    this.initialstates();
    f2.generateBoard();
    t.checkExpect(f2.board.size(), 2);
    t.checkExpect(f2.board.get(0).size(), 2);
  }

  // test AddNeighbors
  void testAddNeighbors(Tester t) {
    this.initialstates();
    f1.generateBoard();
    f1.addNeighbors();
    t.checkExpect(f1.board.get(0).get(0).right.x, 1);
    t.checkExpect(f1.board.get(1).get(1).right.x, 2);
    t.checkExpect(f1.board.get(10).get(10).right.y, 10);
    t.checkExpect(f1.board.get(0).get(0).top, null);
    t.checkExpect(f1.board.get(0).get(0).left, null);
    t.checkExpect(f1.board.get(0).get(0).bottom.x, 0);
    t.checkExpect(f1.board.get(0).get(0).bottom.y, 1);
    t.checkExpect(f1.board.get(10).get(10).top.x, 10);
    t.checkExpect(f1.board.get(10).get(10).top.y, 9);
    t.checkExpect(f1.board.get(10).get(10).left.x, 9);
    t.checkExpect(f1.board.get(10).get(10).left.y, 10);
    t.checkExpect(f1.board.get(10).get(10).right.x, 11);
    t.checkExpect(f1.board.get(10).get(10).right.y, 10);
    t.checkExpect(f1.board.get(10).get(10).bottom.x, 10);
    t.checkExpect(f1.board.get(10).get(10).bottom.y, 11);
    t.checkExpect(f1.board.get(21).get(0).top, null);
    t.checkExpect(f1.board.get(21).get(0).right, null);
    t.checkExpect(f1.board.get(0).get(21).left, null);
    t.checkExpect(f1.board.get(0).get(21).bottom, null);
    t.checkExpect(f1.board.get(21).get(21).bottom, null);
    t.checkExpect(f1.board.get(21).get(21).right, null);
  }

  // testing DrawCell
  void testDrawCell(Tester t) {
    this.initialstates();
    t.checkExpect(this.c10.drawCell(), new OverlayImage(new TextImage("X", Color.WHITE),
        new RectangleImage(20, 20, "solid", Color.YELLOW)));
  }

  // testing DrawCell
  void testOnMouse(Tester t) {
    this.initialstates();
    f1.generateBoard();
    f1.board.get(0).get(0).color = Color.BLACK;
    f1.board.get(0).get(1).color = Color.BLACK;
    f1.board.get(0).get(2).color = Color.BLUE;
    f1.board.get(5).get(5).color = Color.WHITE;
    f1.board.get(10).get(10).color = Color.WHITE;
    f1.onMouseClicked(new Posn(10, 10));
    t.checkExpect(f1.timeclick, 0);
    f1.onMouseClicked(new Posn(10, 10));
    t.checkExpect(f1.timeclick, 0);
    f1.onMouseClicked(new Posn(10, 30));
    t.checkExpect(f1.timeclick, 0);
    f1.onMouseClicked(new Posn(10, 50));
    f1.onTick();
    t.checkExpect(f1.timeclick, 1);
    f1.onMouseClicked(new Posn(15, 50));
    f1.onTick();
    t.checkExpect(f1.timeclick, 1);
    f1.onMouseClicked(new Posn(110, 110));
    f1.onTick();
    t.checkExpect(f1.timeclick, 2);
    f1.onMouseClicked(new Posn(110, 110));
    f1.onTick();
    t.checkExpect(f1.timeclick, 2);
    f1.onMouseClicked(new Posn(110, 110));
    f1.onTick();
    t.checkExpect(f1.timeclick, 2);
    f1.onMouseClicked(new Posn(210, 210));
    f1.onTick();
    t.checkExpect(f1.timeclick, 2);
    f1.onMouseClicked(new Posn(210, 210));
    f1.onTick();
    t.checkExpect(f1.timeclick, 2);
  
  }

  // testing UpdateFlood
  void testUpdateFlood(Tester t) {
    this.initialstates();
    f2.board = ll3;
    f2.addNeighbors();
    f2.currentcolor = Color.BLACK;
    f2.updateFlood(Color.BLACK);
    t.checkExpect(f2.board.get(0).get(0).right.flooded, true);
    t.checkExpect(f2.board.get(0).get(0).bottom.flooded, true);
    t.checkExpect(f2.board.get(1).get(1).flooded, true);

    f2.board = ll2;
    f2.addNeighbors();
    f2.currentcolor = Color.BLACK;
    f2.updateFlood(Color.BLACK);
    t.checkExpect(f2.board.get(0).get(0).right.flooded, false);
    t.checkExpect(f2.board.get(0).get(0).bottom.flooded, false);
    t.checkExpect(f2.board.get(0).get(1).right.flooded, false);

  }

  // testing OnKey
  void testOnKey(Tester t) {
    this.initialstates();
    ArrayList<ArrayList<Cell>> cells = f2.board;
    f2.generateBoard();
    f2.timer++;
    f2.timer++;
    f2.onKeyEvent("r");
    t.checkExpect(f2.timer, 0);
    t.checkExpect(f2.timeclick, 0);
    t.checkExpect(f2.score, 0);
    t.checkExpect(f2.score, 0);
    t.checkFail(f2.board, cells);

    ArrayList<ArrayList<Cell>> cells2 = f1.board;
    f1.generateBoard();
    f1.timer++;
    f1.timer++;
    f1.onKeyEvent("r");
    t.checkExpect(f1.timer, 0);
    t.checkExpect(f1.timeclick, 0);
    t.checkExpect(f1.score, 0);
    t.checkExpect(f1.score, 0);
    t.checkFail(f1.board, cells2);
  }

  // testing AllSame
  void testAllSame(Tester t) {
    this.initialstates();
    f2.generateBoard();
    t.checkExpect(f2.allsamecolor(), false);
    f4.generateBoard();
    t.checkExpect(f4.allsamecolor(), true);
    f4.generateBoard();
    f4.board = ll3;
    f4.currentcolor = Color.BLACK;
    t.checkExpect(f4.allsamecolor(), true);
  }

  // testing OnTick
  void testOnTick(Tester t) {
    this.initialstates();
    f2.generateBoard();
    t.checkExpect(this.f2.timer, 0);
    f2.onTick();
    t.checkExpect(this.f2.timer, 1);
    f2.onTick();
    t.checkExpect(this.f2.timer, 2);
    f2.onTick();
    t.checkExpect(this.f2.timer, 3);
    f2.board = ll3;
    f2.currentcolor = Color.BLACK;
    f2.onTick();
    t.checkExpect(this.f2.timer, 3);
  }

  // testing maxsteps
  boolean testmaxsteps(Tester t) {
    return t.checkExpect(this.u1.maxsteps(2, 3), 9) && t.checkExpect(this.u1.maxsteps(1, 1), 3)
        && t.checkExpect(this.u1.maxsteps(0, 0), 0);
  }

  // run the bigbang animation
  void testGame(Tester t) {
    this.initialstates();
    f1.bigBang(440, 700, 0.1);
  }
}
