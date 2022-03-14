import model.Difficulty;
//import model.MineSweeper;
import model.MineSweeper;
import model.PlayableMinesweeper;
import view.MinesweeperView;

public class App {
    public static void main(String[] args) throws Exception {
        MinesweeperView view = new MinesweeperView();
        //Uncomment the lines below once your game model code is ready; don't forget to import your game model
        PlayableMinesweeper model = new MineSweeper();
        model.startNewGame(Difficulty.MEDIUM);
        view.notifyNewGame(model.getWidth(),model.getHeight());
    }
}
