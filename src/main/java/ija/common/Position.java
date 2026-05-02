package ija.common;

/**
 * Simple coordinate on the map.
 */
public record Position(int x, int y) {
    public int row() {
        return x;
    }

    public int col() {
        return y;
    }
}
