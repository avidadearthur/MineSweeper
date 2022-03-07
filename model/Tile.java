package model;

public class Tile extends AbstractTile{

    public boolean opened;
    public boolean explosive;
    public boolean flagged;

    public Tile(boolean explosive) {
        super();
        this.opened = false;
        this.flagged = false;
        this.explosive = explosive;
    }

    @Override
    public boolean open() {
        return false;
    }

    @Override
    public void flag() {

    }

    @Override
    public void unflag() {

    }

    @Override
    public boolean isFlagged() {
        return flagged;
    }

    @Override
    public boolean isExplosive() {
        return explosive;
    }

    @Override
    public boolean isOpened() {
        return opened;
    }
}
