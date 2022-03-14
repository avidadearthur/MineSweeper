package model;

import java.util.ArrayList;
import java.util.Random;

public class MineSweeper extends AbstractMineSweeper {

    public int height;
    public int width;
    public int explosiveCount;
    public AbstractTile[][] world;

    private final int[][] offsetOfTile = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};
    private boolean firstOpened = false;

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

    @Override
    public void startNewGame(Difficulty level) {
        switch (level) {
            case EASY -> {
                this.height = 8;
                this.width = 8;
                this.explosiveCount = 10;
            }
            case MEDIUM -> {
                this.height = 16;
                this.width = 16;
                this.explosiveCount = 40;
            }
            case HARD -> {
                this.height = 16;
                this.width = 30;
                this.explosiveCount = 99;
            }
        }
        this.world = new Tile[height][width];
        setWorld(this.world);
        viewNotifier.notifyNewGame(height, width);
    }

    @Override
    public void startNewGame(int row, int col, int explosionCount) {
        this.height = row;
        this.width = col;
        this.explosiveCount = explosionCount;
        this.world = new Tile[height][width];
        setWorld(this.world);
        viewNotifier.notifyNewGame(row, col);
    }

    @Override
    public void toggleFlag(int x, int y) {
        if (this.world[x][y].isFlagged()) {
            this.world[x][y].unflag();
        } else {
            this.world[x][y].flag();
        }
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

    private ArrayList<Integer> generateExplosiveAddresses() {
        ArrayList<Integer> addresses = new ArrayList<>();
        int bound = this.height * this.width;
        int count = 0;

        while (count < this.explosiveCount) {
            Random rnd = new Random();
            int nextAddress = rnd.nextInt(bound);

            if (!addresses.contains(nextAddress)) {
                addresses.add(nextAddress);
                count++;
            }
        }
        return addresses;
    }

    @Override
    public void setWorld(AbstractTile[][] world) {
        ArrayList<Integer> explosiveAddresses = generateExplosiveAddresses();

        for (int num : explosiveAddresses) {
            int row = num / this.width;
            int column = num % this.width;
            world[row][column] = generateExplosiveTile();
        }

        for (int i = 0; i < this.height; ++i) {
            for (int j = 0; j < this.width; ++j) {
                if (world[i][j] == null) {
                    world[i][j] = generateEmptyTile();
                }
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

    private void openBlank(int x, int y) {
        world[x][y].open();
        viewNotifier.notifyOpened(x, y, 0);
        for (int[] off : offsetOfTile) {
            int newRow = x + off[0];
            int newCol = y + off[1];
            if(verifyBound(newRow, newCol) && countExplosiveNeighbors(newRow, newCol) == 0
                    && !world[newRow][newCol].isOpened()) {
                openBlank(newRow, newCol);
            }
            else if(verifyBound(newRow, newCol) && countExplosiveNeighbors(newRow, newCol) > 0
                    && !world[newRow][newCol].isOpened()){
                viewNotifier.notifyOpened(newRow, newCol, countExplosiveNeighbors(newRow, newCol));
            }
        }
    }

    private void openExplosive(){
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (world[i][j].isExplosive()) {
                    viewNotifier.notifyExploded(i, j);
                }
                else if(!world[i][j].isOpened())
                    viewNotifier.notifyOpened(i, j, countExplosiveNeighbors(i, j));
                world[i][j].open();
            }
        }
    }

    @Override
    public void open(int x, int y) {
        try {
            this.world[x][y].open();

            if (world[x][y].isExplosive()) {
                if (!firstOpened) {
                    setFirstEmptyExplosive();
                    world[x][y] = generateEmptyTile();
                    firstOpened = true;
                    open(x, y);
                } else {
                    openExplosive();
                    viewNotifier.notifyGameLost();
                    firstOpened = false;
                }
            } else if (countExplosiveNeighbors(x, y) == 0) {
                openBlank(x, y);
            } else {
                viewNotifier.notifyOpened(x, y, countExplosiveNeighbors(x, y));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("Failed to open tile. Invalid index");
        }
    }

    @Override
    public void flag(int x, int y) {
        if(!world[x][y].isFlagged()) {
            this.world[x][y].flag();
            viewNotifier.notifyFlagged(x, y);
            for (int[] off : offsetOfTile) {
                int newRow = x + off[0];
                int newCol = y + off[1];

            }
        }
        else
            unflag(x, y);
    }

    @Override
    public void unflag(int x, int y) {
        this.world[x][y].unflag();
    }

    @Override
    public void deactivateFirstTileRule() {
        firstOpened = true;
    }

    @Override
    public AbstractTile generateEmptyTile() {
        return new Tile(false);
    }

    @Override
    public AbstractTile generateExplosiveTile() {
        return new Tile(true);
    }
}
