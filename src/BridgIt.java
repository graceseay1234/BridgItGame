// Started from scratch after part 1

import java.util.ArrayDeque;
import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// represents the BridgIt world state
class BridgitGame extends World {

  // represents the grid of cells
  ArrayList<ArrayList<Cell>> cells;
  int gridSize;
  int cellSize;
  int windowSize; // big-bang window
  int player; // represents whose turn it is

  BridgitGame(int size) {
    this.gridSize = size;
    // grid can't be smaller than 3 by 3
    if (this.gridSize < 3) {
      this.gridSize = 3;
    }
    // there can't be any even number of rows of columns
    if (this.gridSize % 2 == 0) {
      this.gridSize -= 1;
    }

    this.cellSize = 100;
    this.windowSize = this.cellSize * this.gridSize;
    this.player = 1;

    this.setBoard();
  }

  // draws the game board
  public WorldScene makeScene() {
    WorldScene bg = this.getEmptyScene();

    // places each cell in position accordingly
    for (int i = 0; i < this.gridSize; i++) {
      for (int j = 0; j < this.gridSize; j++) {
        Cell cell = this.cells.get(i).get(j);
        bg.placeImageXY(cell.drawCell(), cell.location.x, cell.location.y);
      }
    }
    return bg;
  }

  // changes the color of white cells when the mouse is clicked
  public void onMouseClicked(Posn p) {

    // pinpoints current cell based on the cursors location
    double x = Math.ceil(p.x / this.cellSize);
    double y = Math.ceil(p.y / this.cellSize);
    int row = (int) x;
    int col = (int) y;

    Cell cell = this.cells.get(row).get(col);
    if (cell.color == Color.white) {
      if (player == 1) {
        cell.color = Color.pink;
      }
      else {
        cell.color = Color.magenta;
      }
      this.changeTurn();

      if (this.horizontalPathFound()) {
        this.endOfWorld("Player 1 Wins!");
      }
    }
  }

  // resets board when "r" key is pressed
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.setBoard();
    }
  }

  // ends game if somebody wins
  public WorldEnd worldEnds() {
    if (this.horizontalPathFound()) {
      return new WorldEnd(true, this.makeScene());
    }
    if (this.verticalPathFound()) {
      return new WorldEnd(true, this.makeScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  // EFFECT: sets game board to its initial state
  void setBoard() {
    this.cells = new ArrayList<ArrayList<Cell>>();

    for (int i = 0; i < this.gridSize; i++) {
      this.cells.add(new ArrayList<Cell>());

      // creates row of cells
      for (int j = 0; j < this.gridSize; j++) {
        if (this.player == 1) {
          Cell pinkCell = new Cell(Color.PINK, this.cellSize);
          this.cells.get(i).add(pinkCell);
        }
        if (this.player == 2) {
          Cell magentaCell = new Cell(Color.magenta, this.cellSize);
          this.cells.get(i).add(magentaCell);
        }

        // sets cell's coordinates
        Cell cell = this.cells.get(i).get(j);
        int x = this.cellSize * i + (this.cellSize / 2);
        int y = this.cellSize * j + (this.cellSize / 2);
        cell.location = new Posn(x, y);
      }
      this.changeTurn();
    }

    // changes the necessary cells to white
    for (int i = 0; i < this.gridSize; i++) {
      for (int j = 0; j < this.gridSize; j++) {
        if ((i % 2 == 0 && j % 2 == 0) || (i % 2 != 0 && j % 2 != 0)) {
          Cell cell = this.cells.get(i).get(j);
          cell.color = Color.white;
        }
      }

      this.player = 1;
    }

    // assigns valid neighbors to each cell on the board
    this.findNeighbors();
  }

  // assigns neighbors to each cell
  void findNeighbors() {
    for (int i = 0; i < this.gridSize; i++) {
      for (int j = 0; j < this.gridSize; j++) {
        Cell c = this.cells.get(i).get(j);

        // invalid neighbors for cells along the edges remain null
        if (i != 0) {
          c.top = this.cells.get(i - 1).get(j);
        }
        if (i != this.gridSize - 1) {
          c.bottom = this.cells.get(i + 1).get(j);
        }
        if (j != 0) {
          c.left = this.cells.get(i).get(j - 1);
        }
        if (j != this.gridSize - 1) {
          c.right = this.cells.get(i).get(j + 1);
        }
      }
    }
  }

  // changes players' turn
  void changeTurn() {
    if (this.player == 1) {
      this.player = 2;
    }
    else {
      this.player = 1;
    }
  }

  // is the cursor within the given cell
  boolean isCursorWithinCell(Posn cell, Posn mouse) {
    return Math.abs(cell.x - mouse.x) < this.cellSize / 2
        && Math.abs(cell.y - mouse.y) < this.cellSize / 2;
  }

  // has player 1 made a horizontal path?
  boolean horizontalPathFound() {
    ArrayDeque<Cell> alreadySeen = new ArrayDeque<Cell>();
    ArrayDeque<Cell> worklist = new ArrayDeque<Cell>();

    // Initialize the worklist with the from vertex
    for (int i = 0; i < this.gridSize; i++) {
      Cell temp = new Cell(Color.pink, this.cellSize);
      Cell cell = this.cells.get(i).get(0);

      if (temp.validCell(cell)) {
        worklist.add(cell);
      }
    }

    // as long as the worklist isn't empty
    while (!worklist.isEmpty()) {
      Cell next = worklist.remove();
      if (next.location.x > this.windowSize - this.cellSize) {
        return true;
      }
      else if (alreadySeen.contains(next)) {

      }
      else {
        // add all the neighbors of next to the worklist for further processing
        if (next.validCell(next.right)) {
          worklist.add(next.right);
        }
        if (next.validCell(next.bottom)) {
          worklist.add(next.bottom);
        }
        if (next.validCell(next.top)) {
          worklist.add(next.top);
        }
        if (next.validCell(next.left)) {
          worklist.add(next.left);
        }

        // add next to alreadySeen, since we're done with it
        alreadySeen.add(next);
      }
    }
    // We haven't found the to vertex, and there are no more to try
    return false;
  }

  // has player 2 made a vertical path?
  boolean verticalPathFound() {
    ArrayDeque<Cell> alreadySeen = new ArrayDeque<Cell>();
    ArrayDeque<Cell> worklist = new ArrayDeque<Cell>();

    // Initialize the worklist with the from vertex
    for (int i = 0; i < this.gridSize; i++) {
      Cell temp = new Cell(Color.magenta, this.cellSize);
      Cell cell = this.cells.get(0).get(i);

      if (temp.validCell(cell)) {
        worklist.add(cell);
      }
    }

    // as long as the worklist isn't empty
    while (!worklist.isEmpty()) {
      Cell next = worklist.remove();
      if (next.location.y > this.windowSize - this.cellSize) {
        return true;
      }
      else if (alreadySeen.contains(next)) {

      }
      else {
        // add all the neighbors of next to the worklist for further processing
        if (next.validCell(next.right)) {
          worklist.add(next.right);
        }
        if (next.validCell(next.bottom)) {
          worklist.add(next.bottom);
        }
        if (next.validCell(next.top)) {
          worklist.add(next.top);
        }
        if (next.validCell(next.left)) {
          worklist.add(next.left);
        }

        // add next to alreadySeen, since we're done with it
        alreadySeen.add(next);
      }
    }
    // We haven't found the to vertex, and there are no more to try
    return false;
  }
}

// represents a single cell on the game board
class Cell {
  Color color;
  Cell top;
  Cell bottom;
  Cell left;
  Cell right;
  int size;
  Posn location;

  Cell(Color color, int size) {
    this.color = color;
    this.size = size;
  }

  // draws square cell
  WorldImage drawCell() {
    return new RectangleImage(this.size, this.size, "solid", this.color);
  }

  // can the given cell be used to continue a path?
  boolean validCell(Cell other) {
    return other != null && this.color.equals(other.color);
  }
}

class ExamplesBridgitGame {

  void testGame(Tester t) {
    BridgitGame b = new BridgitGame(7);
    b.bigBang(b.windowSize, b.windowSize);
  }

  void testDrawCell(Tester t) {
    Cell a = new Cell(Color.green, 100);
    Cell b = new Cell(Color.cyan, 100);

    RectangleImage cellA = new RectangleImage(100, 100, "solid", Color.green);
    RectangleImage cellB = new RectangleImage(100, 100, "solid", Color.cyan);

    t.checkExpect(a.drawCell(), cellA);
    t.checkExpect(b.drawCell(), cellB);
  }

  void testValidCell(Tester t) {
    Cell c1 = new Cell(Color.red, 5);
    Cell c2 = new Cell(Color.red, 5);
    Cell c3 = new Cell(Color.green, 5);

    t.checkExpect(c1.validCell(c2), true);
    t.checkExpect(c1.validCell(c3), false);
  }

  void testFindNeighbors(Tester t) {
    BridgitGame b = new BridgitGame(5);

    // any non-white cell not in contact with the edge is surrounded by white
    // cells
    Cell cell = b.cells.get(2).get(3);
    Cell topLeftCorner = b.cells.get(0).get(0);
    Cell topRightConter = b.cells.get(0).get(b.gridSize - 1);

    t.checkExpect(cell.top, b.cells.get(1).get(3));
    t.checkExpect(cell.bottom, b.cells.get(3).get(3));
    t.checkExpect(cell.left, b.cells.get(2).get(2));
    t.checkExpect(cell.right, b.cells.get(2).get(4));

    t.checkExpect(topLeftCorner.top, null);
    t.checkExpect(topLeftCorner.bottom, b.cells.get(1).get(0));
    t.checkExpect(topLeftCorner.left, null);
    t.checkExpect(topLeftCorner.right, b.cells.get(0).get(1));

    t.checkExpect(topRightConter.top, null);
    t.checkExpect(topRightConter.bottom, b.cells.get(1).get(b.gridSize - 1));
    t.checkExpect(topRightConter.left, b.cells.get(0).get(b.gridSize - 2));
    t.checkExpect(topRightConter.right, null);
  }

  void testIsCursorWithinCell(Tester t) {
    BridgitGame b = new BridgitGame(5);

    Cell c1 = b.cells.get(0).get(0);
    Cell c2 = b.cells.get(0).get(1);

    t.checkExpect(b.isCursorWithinCell(c1.location, new Posn(3, 6)), true);
    t.checkExpect(b.isCursorWithinCell(c2.location, new Posn(3, 6)), false);
  }

  void testSetBoard(Tester t) {
    BridgitGame b = new BridgitGame(3);
    BridgitGame b2 = new BridgitGame(3);

    // Cell pink = new Cell(Color.pink, b.cellSize);
    // Cell magenta = new Cell(Color.magenta, b.cellSize);
    // Cell white = new Cell(Color.white, b.cellSize);
    //
    // ArrayList<Cell> r1 = new ArrayList<Cell>(Arrays.asList(white, magenta,
    // white));
    // ArrayList<Cell> r2 = new ArrayList<Cell>(Arrays.asList(pink, white,
    // pink));
    //
    //
    // ArrayList<ArrayList<Cell>> board = new
    // ArrayList<ArrayList<Cell>>(Arrays.asList(r1, r2, r1));

    b.setBoard();
    t.checkExpect(b.cells, b2.cells);
  }

  void testOnKeyEvent(Tester t) {
    BridgitGame b = new BridgitGame(3);
    BridgitGame b2 = new BridgitGame(3);

    b.onMouseClicked(new Posn(1, 1)); // changes color of cell on the top left
                                      // corner
    t.checkFail(b.cells, b2.cells);
    b.onKeyEvent("r");
    t.checkExpect(b.cells, b2.cells);
  }

  void testMouseClick(Tester t) {
    BridgitGame b = new BridgitGame(3);
    Cell c1 = b.cells.get(0).get(0);
    t.checkExpect(c1.color, Color.white);
    b.onMouseClicked(new Posn(1, 1));
    t.checkExpect(c1.color, Color.pink);
  }

  void testMakeScene(Tester t) {
    BridgitGame b1 = new BridgitGame(7);
    BridgitGame b2 = new BridgitGame(7);

    t.checkExpect(b1.makeScene(), b2.makeScene());
  }
}