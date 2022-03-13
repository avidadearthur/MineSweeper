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
        if (!this.opened) {
            opened = true;
            return true;
        }
        return false;
    }

    @Override
    public void flag() {
        this.flagged = true;
    }

    @Override
    public void unflag() {
        this.flagged = false;
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
