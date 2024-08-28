public class Main {
  public static void main(String[] args) {
    // Create an instance of Minesweeper with a grid size and number of mines
    Minesweeper game = new Minesweeper(10, 10, 10);  // 10x10 grid with 10 mines

    // Start the game
    game.bigBang(game.columns * WorldConstants.CELL_SIZE, game.rows * WorldConstants.CELL_SIZE, 0.1);
  }
}