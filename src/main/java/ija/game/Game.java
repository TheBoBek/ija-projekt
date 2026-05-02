package ija.game;

import ija.common.GameEvent;
import ija.common.Position;
import ija.observer.GameObserver;
import ija.observer.Observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Main game model. Stores map, units, and observers.
 */
public class Game implements Observable {
    private final Tile[][] map;
    private final List<Unit> units = new ArrayList<>();
    private final List<GameObserver> observers = new ArrayList<>();

    // Default constructor creates an empty map.
    public Game() {
        this(new Tile[0][0]);
    }

    Game(Tile[][] map) {
        if (map == null) {
            throw new IllegalArgumentException("Map must not be null");
        }
        this.map = map;
    }

    @Override
    public void addObserver(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public Unit createUnit(String type, String owner, int x, int y) {
        UnitType unitType = UnitType.fromName(type);
        if (unitType == null) {
            throw new IllegalArgumentException("Unknown unit type: " + type);
        }

        Position position = new Position(x, y);
        if (!isInsideMap(position)) {
            throw new IllegalArgumentException("Unit position is outside map: " + position);
        }

        if (getUnitAt(position) != null) {
            throw new IllegalArgumentException("Tile is already occupied: " + position);
        }

        Unit unit = new Unit(unitType, owner, position);
        units.add(unit);
        return unit;
    }

    public Unit getUnitAt(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }
        if (!isInsideMap(position)) {
            throw new IllegalArgumentException("Position is outside map: " + position);
        }

        for (Unit unit : units) {
            if (unit.getPosition().equals(position)) {
                return unit;
            }
        }
        return null;
    }

    public boolean moveUnit(Position from, Position to) {
        if (from == null || to == null) {
            return false;
        }
        if (from.equals(to)) {
            return false;
        }

        if (!isInsideMap(to)) {
            return false;
        }

        Unit unit = getUnitAt(from);
        if (unit == null) {
            return false;
        }
        if (getUnitAt(to) != null) {
            return false;
        }

        List<Position> reachable = getReachableTiles(from);
        if (!reachable.contains(to)) {
            return false;
        }

        // Move is valid, update position and notify observers.
        unit.setPosition(to);
        notifyObservers(new GameEvent("move", unit, from, to));
        return true;
    }

    public int getHeight() {
        return map.length;
    }

    public int getWidth() {
        if (map.length == 0) {
            return 0;
        }
        return map[0].length;
    }

    public Tile getTileAt(Position position) {
        if (!isInsideMap(position)) {
            throw new IllegalArgumentException("Position is outside map: " + position);
        }
        return map[position.row()][position.col()];
    }

    public Tile getTileAt(int x, int y) {
        return getTileAt(new Position(x, y));
    }

    private boolean isInsideMap(Position position) {
        if (position == null) {
            return false;
        }
        if (map.length == 0) {
            return false;
        }

        int row = position.row();
        int col = position.col();
        return row >= 0 && row < map.length && col >= 0 && col < map[0].length;
    }

    public List<Position> getReachableTiles(Position unitPosition) {
        List<Position> reachable = new ArrayList<>();
        if (unitPosition == null) {
            return reachable;
        }
        if (!isInsideMap(unitPosition)) {
            return reachable;
        }

        Unit unit = getUnitAt(unitPosition);
        if (unit == null) {
            return reachable;
        }

        int rows = map.length;
        int columns = map[0].length;

        // Best known path cost to each tile.
        int[][] distance = new int[rows][columns];
        for (int row = 0; row < rows; row++) {
            Arrays.fill(distance[row], Integer.MAX_VALUE);
        }

        // Dijkstra queue sorted by lowest current cost.
        PriorityQueue<PathNode> queue = new PriorityQueue<>(Comparator.comparingInt(node -> node.cost));
        distance[unitPosition.x()][unitPosition.y()] = 0;
        queue.add(new PathNode(unitPosition.x(), unitPosition.y(), 0));

        // 4-direction movement: up, down, left, right.
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();
            if (current.cost != distance[current.x][current.y]) {
                continue;
            }

            for (int i = 0; i < 4; i++) {
                int nextX = current.x + dx[i];
                int nextY = current.y + dy[i];
                Position next = new Position(nextX, nextY);

                if (!isInsideMap(next)) {
                    continue;
                }
                if (isOccupiedByAnotherUnit(next, unit)) {
                    continue;
                }

                int stepCost = map[nextX][nextY].getMoveCost(unit.getType());
                if (stepCost < 0) {
                    // Negative cost means terrain is not passable for this unit.
                    continue;
                }

                int newCost = current.cost + stepCost;
                if (newCost > unit.getType().getMovePoints()) {
                    continue;
                }
                if (newCost < distance[nextX][nextY]) {
                    distance[nextX][nextY] = newCost;
                    queue.add(new PathNode(nextX, nextY, newCost));
                }
            }
        }

        for (int x = 0; x < rows; x++) {
            for (int y = 0; y < columns; y++) {
                if (distance[x][y] == Integer.MAX_VALUE) {
                    continue;
                }
                if (x == unitPosition.x() && y == unitPosition.y()) {
                    continue;
                }
                reachable.add(new Position(x, y));
            }
        }

        reachable.sort(Comparator.comparingInt(Position::x).thenComparingInt(Position::y));
        return reachable;
    }

    private void notifyObservers(GameEvent event) {
        // Snapshot helps avoid issues if observer list changes during update.
        List<GameObserver> snapshot = new ArrayList<>(observers);
        for (GameObserver observer : snapshot) {
            observer.update(event);
        }
    }

    @Override
    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    private boolean isOccupiedByAnotherUnit(Position position, Unit movingUnit) {
        Unit occupant = getUnitAt(position);
        return occupant != null && occupant != movingUnit;
    }

    private static final class PathNode {
        private final int x;
        private final int y;
        private final int cost;

        private PathNode(int x, int y, int cost) {
            this.x = x;
            this.y = y;
            this.cost = cost;
        }
    }
}
