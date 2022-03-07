package model;

import test.TestableTile;
import java.util.Random;
import java.lang.reflect.Array;

public class MineSweeper extends AbstractMineSweeper{

    public int height;
    public int width;
    public int explosiveCount;
    public Tile[][] world;

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
    }

    @Override
    public void startNewGame(int row, int col, int explosionCount) {
        this.height = row;
        this.width = col;
        this.world = new Tile[height][width];
        setWorld(this.world);
    }

    @Override
    public void toggleFlag(int x, int y) {

    }

    @Override
    public AbstractTile getTile(int x, int y) {
        return world[y][x];
    }

    private int[] generateExplosiveAddresses() {
        return null;
    }

    @Override
    public void setWorld(AbstractTile[][] world) {
        int[] explosiveAddresses = generateExplosiveAddresses();

        for (int i=0; i<explosiveAddresses.length; ++i) {

            }
        }
    }

    @Override
    public void open(int x, int y) {

    }

    @Override
    public void flag(int x, int y) {

    }

    @Override
    public void unflag(int x, int y) {

    }

    @Override
    public void deactivateFirstTileRule() {

    }

    @Override
    public AbstractTile generateEmptyTile() {
        // Tile newTile = new Tile(false);
        return new Tile(false);
    }

    @Override
    public AbstractTile generateExplosiveTile() {
        return new Tile(true);
    }
}
