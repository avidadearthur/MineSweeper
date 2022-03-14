import model.Difficulty;
//import model.MineSweeper;
import model.MineSweeper;
import model.PlayableMinesweeper;
import view.MinesweeperView;

public class App {
    public static void main(String[] args) throws Exception {
        MinesweeperView view = new MinesweeperView();
        PlayableMinesweeper model = new MineSweeper();
        view.setGameModel(model);
        model.startNewGame(Difficulty.EASY);
    }
}
