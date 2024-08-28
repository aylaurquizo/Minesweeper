import java.util.ArrayList;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;
import java.util.Random;

// represents the constants in the code
interface WorldConstants {
  int CELL_SIZE = 20;

}

// represents a world class to animate an array of cells
class Minesweeper extends World implements WorldConstants {
  int rows;
  int columns;
  int numMines;
  ArrayList<ArrayList<Cell>> grid;
  boolean gameOver;
  Random rand;

  // constructor for use in real game
  Minesweeper(int rows, int columns, int numMines) {
    this.rows = rows;
    this.columns = columns;
    this.numMines = numMines;
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.gameOver = false;
    this.rand = new Random();
    initializeGrid();
  }

  // constructor for use in testing, with a specified Random object
  Minesweeper(int rows, int columns, int numMines, Random rand) {
    this.rows = rows;
    this.columns = columns;
    this.numMines = numMines;
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.gameOver = false;
    this.rand = rand;
    initializeGrid();
  }

  // initialize the grid with cells
  void initializeGrid() {
    for (int i = 0; i < rows; i++) {
      ArrayList<Cell> row = new ArrayList<>();
      for (int j = 0; j < columns; j++) {
        Cell cell = new Cell(false, 0, false, false, new ArrayList<Cell>());
        row.add(cell);
      }
      grid.add(row);
    }

    placeMines(rand);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        Cell cell = grid.get(i).get(j);
        for (int x = -1; x <= 1; x++) {
          for (int y = -1; y <= 1; y++) {
            if (x != 0 || y != 0) {
              if (isValidCell(i + x, j + y)) {
                cell.addNeighbor(grid.get(i + x).get(j + y));
              }
            }
          }
        }
      }
    }

    for (ArrayList<Cell> row : this.grid) {
      for (Cell cell : row) {
        cell.countAdjacentMines();
      }
    }

  }

  // checks to see if the inputed row and col are valid cells on the grid
  boolean isValidCell(int row, int col) {
    return row >= 0 && row < rows && col >= 0 && col < columns;
  }

  // randomly place mines on the grid
  void placeMines(Random random) {
    int minesPlaced = 0;
    while (minesPlaced < numMines) {
      int row = random.nextInt(rows);
      int col = random.nextInt(columns);

      Cell currentCell = this.grid.get(row).get(col);
      if (!currentCell.isMine) {
        currentCell.isMine = true;
        minesPlaced++;
      }
    }
  }

  // draws the words onto the background
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(columns * CELL_SIZE, rows * CELL_SIZE);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        Cell currentCell = this.grid.get(i).get(j);
        scene.placeImageXY(currentCell.draw(), j * CELL_SIZE + CELL_SIZE / 2,
            i * CELL_SIZE + CELL_SIZE / 2);
      }
    }

    if (gameOver) {
      String message = "You Win!";
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < columns; j++) {
          Cell cell = grid.get(i).get(j);
          if (cell.isMine && cell.isRevealed) {
            message = "You Lose";
          }
        }
      }
      TextImage gameOverText = new TextImage(message, 24, Color.RED);
      scene.placeImageXY(gameOverText, columns * CELL_SIZE / 2, rows * CELL_SIZE / 2);
    }

    return scene;
  }

  // reveals a cell if left click and flags a cell if right click
  public void onMouseClicked(Posn pos, String button) {
    if (!gameOver) {
      int row = pos.y / CELL_SIZE;
      int col = pos.x / CELL_SIZE;
      if (button.equals("RightButton")) {
        this.flagCell(row, col);
      }
      else if (button.equals("LeftButton")) {
        this.revealCell(row, col);
      }

    }

  }

  // flags the indicated cell
  public void flagCell(int row, int col) {
    Cell clickedCell = grid.get(row).get(col);
    if(clickedCell.isFlagged) {
      clickedCell.isFlagged = false;
    }
    else {
      clickedCell.isFlagged = true;
    }
  }

  // reveals a cell when clicked
  public void revealCell(int row, int col) {
    Cell clickedCell = grid.get(row).get(col);
    clickedCell.isRevealed = true;
    if (checkGame(clickedCell)) {
      this.gameOver = true;
    }
    else {
      if (clickedCell.adjacentMines == 0) {
        this.flooding(clickedCell.neighbors);
      }
    }
  }

  // checks to see if the game is over
  public boolean checkGame(Cell clickedCell) {
    if (clickedCell.isMine) {
      return true;
    }
    else {
      for (ArrayList<Cell> row : this.grid) {
        for (Cell cell : row) {
          if (cell.isMine && cell.isRevealed) {
            return false;
          }
          if (!cell.isMine && !cell.isRevealed) {
            return false;
          }
        }
      }
      return true;
    }
  }

  // implements the flooding aspect of Minesweeper
  public void flooding(ArrayList<Cell> neighbors) {
    for (Cell cell : neighbors) {
      if ((!cell.isMine) && (!cell.isRevealed)) {
        cell.isRevealed = true;
        if (cell.adjacentMines == 0) {
          this.flooding(cell.neighbors);
        }
      }
    }
  }

}

// represents a cell within a grid
class Cell implements WorldConstants {
  boolean isMine;
  int adjacentMines;
  boolean isRevealed;
  boolean isFlagged;
  ArrayList<Cell> neighbors;

  // main constructor
  Cell(boolean isMine, int adjacentMines, boolean isRevealed, boolean isFlagged,
      ArrayList<Cell> neighbors) {
    this.isMine = isMine;
    this.adjacentMines = adjacentMines;
    this.isRevealed = isRevealed;
    this.isFlagged = isFlagged;
    this.neighbors = neighbors;
  }

  // empty constructor
  Cell() {
    this(false, 0, false, false, new ArrayList<Cell>());
  }

  // count the number of neighboring mines
  public void countAdjacentMines() {
    for (Cell neighbor : this.neighbors) {
      if (neighbor.isMine) {
        this.adjacentMines++;
      }
    }
  }

  // add a neighbor to this cell
  public void addNeighbor(Cell neighbor) {
    this.neighbors.add(neighbor);
  }

  // draws the state of the cell
  public WorldImage draw() {
    if (isRevealed) {
      if (isMine) {
        return new RectangleImage(CELL_SIZE, 20, OutlineMode.SOLID, Color.RED);
      }
      else if (this.adjacentMines > 0) {
        return new OverlayImage(
            new TextImage(Integer.toString(this.adjacentMines), 18, Color.BLACK), new FrameImage(
                new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID, Color.LIGHT_GRAY)));
      }
      else {
        return new FrameImage(
            new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID, Color.LIGHT_GRAY));
      }
    }

    else if (isFlagged) {
      return new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.SOLID, Color.ORANGE);
    }

    else {
      return new RectangleImage(CELL_SIZE, CELL_SIZE, OutlineMode.OUTLINE, Color.BLACK);
    }
  }
}

// examples and test for the Minesweeper game
class ExamplesMinesweeper {
  Cell basicCell1, basicCell2, cell1, cell2, cell3, cell4, cell5, cell6;
  Cell revCell1, revCell2, revCell3;
  Cell flagCell;
  ArrayList<Cell> cell1Nbr, cell2Nbr, cell3Nbr, cell4Nbr, cell5Nbr, cell6Nbr;
  ArrayList<Cell> cellRow1, cellRow2;
  ArrayList<ArrayList<Cell>> grid;

  // examples for different ways cells can be drawn
  WorldImage revCell1Image = new RectangleImage(20, 20, OutlineMode.SOLID, Color.RED);
  WorldImage revCell2Image = new OverlayImage(new TextImage(Integer.toString(1), 18, Color.BLACK),
      new FrameImage(new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY)));
  WorldImage revCell3Image = new FrameImage(
      new RectangleImage(20, 20, OutlineMode.SOLID, Color.LIGHT_GRAY));
  WorldImage flagCellImage = new RectangleImage(20, 20, OutlineMode.SOLID, Color.ORANGE);
  WorldImage basicCellImage = new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK);

  // examples for a Game
  Minesweeper StartingGame, StartingGame2, StartingGame3, game, noMines;

  int count;

  void init() {

    this.count = 0;
    // examples of unrevealed, unflagged cells with no mines, no adjacent mines, and
    // no neighbors
    this.basicCell1 = new Cell(false, 0, false, false, new ArrayList<Cell>());
    this.basicCell2 = new Cell(false, 0, false, false, new ArrayList<Cell>());

    // examples for revealed cells
    this.revCell1 = new Cell(true, 0, true, false, new ArrayList<Cell>());
    this.revCell2 = new Cell(false, 1, true, false, new ArrayList<Cell>());
    this.revCell3 = new Cell(false, 0, true, false, new ArrayList<Cell>());

    // example of a flagged cell
    this.flagCell = new Cell(false, 0, false, true, new ArrayList<Cell>());

    // examples of cells that make up a 2x3 grid
    this.cell1 = new Cell(true, 0, false, false, new ArrayList<Cell>());
    this.cell2 = new Cell(false, 0, false, false, new ArrayList<Cell>());
    this.cell3 = new Cell(false, 0, false, false, new ArrayList<Cell>());
    this.cell4 = new Cell(true, 0, false, false, new ArrayList<Cell>());
    this.cell5 = new Cell(false, 0, false, false, new ArrayList<Cell>());
    this.cell6 = new Cell(false, 0, false, false, new ArrayList<Cell>());

    // adds the correct neighbors to cell1
    this.cell1.neighbors.add(cell2);
    this.cell1.neighbors.add(cell4);
    this.cell1.neighbors.add(cell5);

    // adds the correct neighbors to cell2
    this.cell2.neighbors.add(cell1);
    this.cell2.neighbors.add(cell3);
    this.cell2.neighbors.add(cell4);
    this.cell2.neighbors.add(cell5);
    this.cell2.neighbors.add(cell6);

    // adds the correct neighbors to cell2
    this.cell3.neighbors.add(cell2);
    this.cell3.neighbors.add(cell5);
    this.cell3.neighbors.add(cell6);

    // adds the correct neighbors to cell2
    this.cell4.neighbors.add(cell1);
    this.cell4.neighbors.add(cell2);
    this.cell4.neighbors.add(cell5);

    // adds the correct neighbors to cell3
    this.cell5.neighbors.add(cell1);
    this.cell5.neighbors.add(cell2);
    this.cell5.neighbors.add(cell3);
    this.cell5.neighbors.add(cell4);
    this.cell5.neighbors.add(cell6);

    // adds the correct neighbors to cell4
    this.cell6.neighbors.add(cell2);
    this.cell6.neighbors.add(cell3);
    this.cell6.neighbors.add(cell5);

    // adds the correct cells to the 1st row of the 2x3 grid
    this.cellRow1 = new ArrayList<Cell>();
    this.cellRow1.add(cell1);
    this.cellRow1.add(cell2);
    this.cellRow1.add(cell3);

    // adds the correct cells to the 2nd row of the 2x3 grid
    this.cellRow2 = new ArrayList<Cell>();
    this.cellRow2.add(cell4);
    this.cellRow2.add(cell5);
    this.cellRow2.add(cell6);

    // example for a 2x3 grid
    this.grid = new ArrayList<ArrayList<Cell>>();
    this.grid.add(cellRow1);
    this.grid.add(cellRow2);

    // example of Minesweeper game with a 2x3 grid and 3 mines
    this.StartingGame = new Minesweeper(2, 3, 1);
    this.StartingGame2 = new Minesweeper(2, 3, 3);
    this.StartingGame3 = new Minesweeper(1, 1, 0);

    this.game = new Minesweeper(2, 3, 3, new Random());
    this.noMines = new Minesweeper(2, 3, 0, new Random());

  }

  // test for countAdjacentMines()
  boolean testCountAdjacentMines(Tester t) {
    this.init();
    this.cell1.countAdjacentMines();
    this.cell2.countAdjacentMines();
    this.cell6.countAdjacentMines();
    return
    // test for a cell with 0 adjacent mines
    t.checkExpect(this.cell6.adjacentMines, 0)
        // test for a cell with 1 adjacent mine
        && t.checkExpect(this.cell1.adjacentMines, 1)
        // test for a cell with 2 adjacent mines
        && t.checkExpect(this.cell2.adjacentMines, 2);
  }

  // test for addNeighbor(Cell)
  boolean testAddNeighbor(Tester t) {
    this.init();

    // adds a neighbor to this.basicCell1
    this.basicCell1.addNeighbor(this.cell1);
    // adds a neighbor to this this.basicCell2
    this.basicCell2.addNeighbor(this.cell2);
    // adds a neighbor to this.cell6
    this.cell6.addNeighbor(this.cell4);

    return
    // confirming that basic cells (no neighbors) have the neighbor added to them
    t.checkExpect(this.basicCell1.neighbors.contains(this.cell1), true)
        && t.checkExpect(this.basicCell2.neighbors.contains(this.cell2), true)
        // confirming that a cell with neighbors has the neighbor added to it
        && t.checkExpect(this.cell6.neighbors.contains(this.cell4), true);
  }

  // test for draw()
  boolean testDraw(Tester t) {
    this.init();
    return
    // testing draw on a revealed cell that has a mine
    t.checkExpect(this.revCell1.draw(), this.revCell1Image)
        // testing draw on a revealed cell with no mine but has adjacent mines
        && t.checkExpect(this.revCell2.draw(), this.revCell2Image)
        // testing draw on a revealed cell with no mine nor adjacent mines
        && t.checkExpect(this.revCell3.draw(), this.revCell3Image)
        // testing draw on a non-revealed cell that has been flagged
        && t.checkExpect(this.flagCell.draw(), this.flagCellImage)
        // testing draw on a non-revelaed cell that has not been flagged
        && t.checkExpect(this.basicCell1.draw(), this.basicCellImage);
  }

  // test for placeMines(Random)
  void testPlaceMines(Tester t) {
    this.init();

    this.game.placeMines(this.game.rand);
    // tests that cells are mines
    t.checkExpect(this.game.grid.get(this.game.rand.nextInt(this.game.rows))
        .get(this.game.rand.nextInt(this.game.columns)).isMine, true);

    this.noMines.placeMines(this.noMines.rand);
    // tests for no mines should be placed
    t.checkExpect(this.noMines.grid.get(this.noMines.rand.nextInt(this.noMines.rows))
        .get(this.noMines.rand.nextInt(this.noMines.columns)).isMine, false);

  }

  boolean testMakeScene(Tester t) {
    this.init();

    // draws the scene correctly
    WorldScene scene = new WorldScene(60, 40);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 3; j++) {
        Cell currentCell = this.grid.get(i).get(j);
        scene.placeImageXY(currentCell.draw(), j * 20 + 20 / 2, i * 20 + 20 / 2);
      }
    }

    // test to see if scene matches scene above
    return t.checkExpect(this.StartingGame.makeScene(), scene);
  }

  // test for method isValidCell
  boolean testIsValidCell(Tester t) {
    this.init();
    return
    // test for a vaild cell
    t.checkExpect(this.StartingGame.isValidCell(0, 0), true)
        // test for both row and col are invalid
        && t.checkExpect(this.StartingGame.isValidCell(10, 10), false)
        // test for row is invalid but col is valid
        && t.checkExpect(this.StartingGame.isValidCell(10, 0), false)
        // test for row is valid but col is invalid
        && t.checkExpect(this.StartingGame.isValidCell(0, 10), false);
  }

  // tests for checkGame method
  boolean testCheckGame(Tester t) {
    this.init();
    this.StartingGame.grid.get(0).set(0, new Cell(true, 5, true, false, new ArrayList<Cell>()));
    this.StartingGame3.grid.get(0).set(0, new Cell(false, 0, true, false, new ArrayList<Cell>()));

    return
    // test for where the game is not over yet
    t.checkExpect(this.StartingGame2.checkGame(this.cell2), false)
        // test for where the game is over because all non-Mine cells have been revealed
        && t.checkExpect(this.StartingGame3.checkGame(this.cell2), true)
        // test for where the game is over because a mine has been clicked
        && t.checkExpect(this.StartingGame.checkGame(this.cell1), true);
  }

  // tests for flagCell method
  void testFlagCell(Tester t) {
    this.init();
    // check that cell is not flagged yet
    t.checkExpect(this.StartingGame.grid.get(0).get(0).isFlagged, false);

    // call the method
    this.StartingGame.flagCell(0, 0);

    // check that the cell is now flagged
    t.checkExpect(this.StartingGame.grid.get(0).get(0).isFlagged, true);
  }

  // tests for revealCell method
  void testRevealCell(Tester t) {
    this.init();
    // setting StartingGame3 cell (0,0) to an un-revealed mine
    this.StartingGame3.grid.get(0).set(0, new Cell(true, 0, false, false, new ArrayList<Cell>()));
    // setting StartingGame2 cell (1,2) to an un-revealed non-mine
    this.StartingGame2.grid.get(1).set(2, this.cell2);
    // setting StartingGame cell (0,0) to an un-revealed non-mine
    this.StartingGame.grid.get(0).set(0, this.cell2);

    // checks that the cell is not revealed yet
    t.checkExpect(this.StartingGame.grid.get(0).get(0).isRevealed, false);
    t.checkExpect(this.StartingGame3.grid.get(0).get(0).isRevealed, false);
    t.checkExpect(this.StartingGame2.grid.get(1).get(2).isRevealed, false);

    // call the method
    this.StartingGame.revealCell(0, 0);
    this.StartingGame3.revealCell(0, 0);
    this.StartingGame2.revealCell(1, 2);

    // tests to check that the cell is now revealed

    // test where cell is changed to revealed and the game is not over
    t.checkExpect(this.StartingGame.grid.get(0).get(0).isRevealed, true);
    t.checkExpect(this.StartingGame.gameOver, false);

    // test where cell is changed to revealed and the game is now over
    t.checkExpect(this.StartingGame3.grid.get(0).get(0).isRevealed, true);
    t.checkExpect(this.StartingGame3.gameOver, true);

    // test where cell is changed to revealed and the game is not over and the
    // flooding effect is run
    t.checkExpect(this.StartingGame2.grid.get(1).get(2).isRevealed, true);
    for (int i = 0; i < this.StartingGame2.grid.get(1).get(2).neighbors.size(); i++) {
      if (this.StartingGame2.grid.get(1).get(2).neighbors.get(i).isRevealed) {
        count++;
      }
    }
    t.checkExpect(count, 3);
  }

  // tests for onMouseClicked method
  void testOnMouseClicked(Tester t) {
    this.init();
    // setting cell (0,0) on StartingGame's grid to an un-revealed cell
    this.StartingGame.grid.get(0).set(0, this.cell1);
    // setting cell (1,2) on StartingGame2's grid to an un-revealed cell
    this.StartingGame2.grid.get(1).set(2, this.cell2);

    // checking that the cells are both un-flagged and un-revealed
    t.checkExpect(this.cell1.isFlagged, false);
    t.checkExpect(this.cell1.isRevealed, false);
    t.checkExpect(this.cell2.isFlagged, false);
    t.checkExpect(this.cell2.isRevealed, false);

    // calling the methods
    // test for using right click
    this.StartingGame.onMouseClicked(new Posn(2, 4), "RightButton");
    // test for using leftclick
    this.StartingGame2.onMouseClicked(new Posn(50, 25), "LeftButton");

    // checks that right click flags the cell but does not reveal it
    t.checkExpect(this.cell1.isFlagged, true);
    t.checkExpect(this.cell1.isRevealed, false);
    // checks that left click reveals the cell and does not flag it
    t.checkExpect(this.cell2.isFlagged, false);
    t.checkExpect(this.cell2.isRevealed, true);
  }

  // tests for method flooding
  void testFlooding(Tester t) {
    this.init();
    // setting cell (1,2) on StartingGame2 to an un-revealed non-mine
    this.StartingGame2.grid.get(1).set(2, this.cell3);

    // checking to see that the cell and its neighbors are un-revealed
    t.checkExpect(this.cell3.isRevealed, false);
    t.checkExpect(this.cell2.isRevealed, false);
    t.checkExpect(this.cell5.isRevealed, false);
    t.checkExpect(this.cell6.isRevealed, false);

    // calling the method
    this.StartingGame2.flooding(this.cell3.neighbors);

    // checking to see that all neighbors which have no adjacent mines are now
    // revealed
    t.checkExpect(this.cell3.isRevealed, true);
    t.checkExpect(this.cell2.isRevealed, true);
    t.checkExpect(this.cell5.isRevealed, true);
    t.checkExpect(this.cell6.isRevealed, true);

    // setting cell (0,0) on StartingGame to an un-revealed mine
    this.StartingGame.grid.get(0).set(0, this.cell1);
    // checking to see if cell1's neighbor cell4 is revealed
    t.checkExpect(this.cell4.isRevealed, false);
    // calls the method
    this.StartingGame.flooding(this.cell1.neighbors);
    // checks that the cell has not been revealed due to flooding because cell1 is a
    // mine
    t.checkExpect(this.cell4.isRevealed, false);
  }

  void testBigBang(Tester t) {
    Minesweeper world = new Minesweeper(30, 16, 80);
    world.bigBang(world.columns * WorldConstants.CELL_SIZE, world.rows * WorldConstants.CELL_SIZE);
  }

}
