package model;

import java.time.Duration;
import java.time.LocalTime;

public class MineSweeper extends AbstractMineSweeper {

    public int height;
    public int width;
    public int explosiveCount;
    public int flagCount;
    public AbstractTile[][] world;

    private final int[][] offsetOfTile = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};
    private boolean firstOpened = false;

    private boolean won;
    private boolean lost;
    private int tilesLeft;

    private LocalTime startTime;

    public MineSweeper() {
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    private Duration setTimer() {
        return Duration.between(startTime, LocalTime.now());
    }

    @Override
    public void startNewGame(Difficulty level) {
        switch (level) {
            case EASY -> startNewGame(8, 8, 10);
            case MEDIUM -> startNewGame(16, 16, 40);
            case HARD -> startNewGame(16, 30, 99);
            default -> throw new IllegalStateException("Unexpected value: " + level);
        }
    }

    @Override
    public void startNewGame(int row, int col, int explosionCount) {
        this.height = row;
        this.width = col;
        this.explosiveCount = explosionCount;
        this.flagCount = explosionCount;
        this.tilesLeft = this.height * this.width;
        this.world = new Tile[height][width];
        this.lost = false;
        this.won = false;
        setWorld(this.world);
        viewNotifier.notifyNewGame(row, col);
        viewNotifier.notifyFlagCountChanged(flagCount);

        startTime = LocalTime.now();
        Runnable r = () -> {
            while (!lost && !won) {
                viewNotifier.notifyTimeElapsedChanged(setTimer());
            }
        };
        new Thread(r).start();
    }

    @Override
    public void setWorld(AbstractTile[][] world) {
        for (int i = 0; i < height; ++i)
            for (int j = 0; j < width; ++j) {
                world[i][j] = generateEmptyTile();
            }
        for (int i = 0; i < explosiveCount; i++) {
            int x = (int) (Math.random() * (height - 1));
            int y = (int) (Math.random() * (width - 1));
            if (world[x][y].isExplosive()) {
                i--;
            } else {
                world[x][y] = generateExplosiveTile();
            }
        }
        this.world = world;
        height = world.length;
        width = world[0].length;
    }

    private boolean verifyBound(int x, int y) {
        return x >= 0 && x < this.height && y >= 0 && y < this.width;
    }

    private void setFirstEmptyExplosive() {
        boolean changed = false;
        for (int i = 0; i < height && !changed; i++) {
            for (int j = 0; j < width && !changed; j++) {
                if (!world[i][j].isExplosive()) {
                    world[i][j] = generateExplosiveTile();
                    changed = true;
                }
            }
        }
    }

    private int countExplosiveNeighbors(int x, int y) {
        int explosiveNeighbors = 0;
        for (int[] off : offsetOfTile) {
            int newRow = x + off[0];
            int newCol = y + off[1];
            if (verifyBound(newRow, newCol) && world[newRow][newCol].isExplosive()) {
                explosiveNeighbors++;
            }
        }
        return explosiveNeighbors;
    }

    private void determineWon() {
        tilesLeft--;
        //win when all empty tiles opened and flagCount = 0
        /*
        if (tilesLeft == explosiveCount && flagCount == 0) {
            viewNotifier.notifyGameWon();
            won = true;
        }
        */
        //win only when all empty tiles opened
        if (tilesLeft == explosiveCount) {
            viewNotifier.notifyGameWon();
            won = true;
        }
    }

    private void openExplosive() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if(world[i][j].isFlagged())
                    toggleFlag(i, j);
                if (world[i][j].isExplosive()) {
                    viewNotifier.notifyExploded(i, j);
                } else if (!world[i][j].isOpened())
                    viewNotifier.notifyOpened(i, j, countExplosiveNeighbors(i, j));
                world[i][j].open();
            }
        }
    }

    @Override
    public void open(int x, int y) {
        try {
            if (world[x][y].isFlagged())
                toggleFlag(x, y);
            if (!world[x][y].isOpened()) {
                if (world[x][y].isExplosive()) {
                    if (!firstOpened) {
                        setFirstEmptyExplosive();
                        world[x][y] = generateEmptyTile();
                        firstOpened = true;
                        open(x, y);
                    } else {
                        openExplosive();
                        lost = true;
                        viewNotifier.notifyGameLost();
                        firstOpened = false;
                    }
                } else if (countExplosiveNeighbors(x, y) == 0) {
                    world[x][y].open();
                    viewNotifier.notifyOpened(x, y, 0);
                    firstOpened = true;
                    determineWon();
                    for (int[] off : offsetOfTile) {
                        int newRow = x + off[0];
                        int newCol = y + off[1];
                        if (verifyBound(newRow, newCol) && !world[newRow][newCol].isExplosive())
                            open(newRow, newCol);
                    }
                } else {
                    world[x][y].open();
                    viewNotifier.notifyOpened(x, y, countExplosiveNeighbors(x, y));
                    firstOpened = true;
                    determineWon();
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Failed to open tile. Invalid index");
        }
    }

    @Override
    public void deactivateFirstTileRule() {
        firstOpened = true;
    }

    @Override
    public void flag(int x, int y) {
        this.world[x][y].flag();
        flagCount--;
        viewNotifier.notifyFlagged(x, y);
    }

    @Override
    public void unflag(int x, int y) {
        this.world[x][y].unflag();
        flagCount++;
        viewNotifier.notifyUnflagged(x, y);
    }

    @Override
    public void toggleFlag(int x, int y) {
        if (!this.world[x][y].isOpened()) {
            if (this.world[x][y].isFlagged()) {
                unflag(x, y);
            } else {
                flag(x, y);
            }
            //win when all empty tiles opened and flagCount = 0
            //without this part, win only when all empty tiles opened
            /*
            if (tilesLeft == explosiveCount && flagCount == 0) {
                viewNotifier.notifyGameWon();
                won = true;
            }
            */
            viewNotifier.notifyFlagCountChanged(flagCount);
        }
    }

    @Override
    public AbstractTile generateEmptyTile() {
        return new Tile(false);
    }

    @Override
    public AbstractTile generateExplosiveTile() {
        return new Tile(true);
    }

    @Override
    public AbstractTile getTile(int x, int y) {
        try {
            return this.world[x][y];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Failed to open tile. Invalid index.");
            return null;
        }
    }
}
