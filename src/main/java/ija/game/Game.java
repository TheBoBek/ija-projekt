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
    private static final BuildingService BUILDING_SERVICE = new BuildingService();
    private static final CombatService COMBAT_SERVICE = new CombatService(new UnitDamageTable());
    private static final int CITY_INCOME_PER_TURN = 1000;
    private static final int MAX_REPAIR_HP_PER_TURN = 20;
    private static final int REPAIR_COST_PERCENT_PER_HP = 1;
    private static final String PLAYER_ONE_ID = "P1";
    private static final String PLAYER_TWO_ID = "P2";

    private final Tile[][] map;
    private final UnitFactory unitFactory;
    private final List<Unit> units = new ArrayList<>();
    private final List<GameObserver> observers = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private Turn turn;
    private boolean gameOver;
    private String winnerPlayerId;

    // Default constructor creates an empty map.
    public Game() {
        this(new Tile[0][0]);
    }

    Game(Tile[][] map) {
        this(map, new DefaultUnitFactory());
    }

    Game(Tile[][] map, UnitFactory unitFactory) {
        if (map == null) {
            throw new IllegalArgumentException("Map must not be null");
        }
        if (unitFactory == null) {
            throw new IllegalArgumentException("Unit factory must not be null");
        }
        this.map = map;
        this.unitFactory = unitFactory;
        initializeDefaultPlayers();
        this.turn = new Turn("P1", 1, Turn.Phase.ACTION);
        this.gameOver = false;
        this.winnerPlayerId = null;
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

        Unit unit = unitFactory.createUnit(unitType, owner, position);
        if (unit == null) {
            throw new IllegalArgumentException("Unit factory returned null unit");
        }
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
        if (gameOver) {
            return false;
        }
        if (from == null || to == null) {
            return false;
        }
        if (!isInsideMap(from) || !isInsideMap(to)) {
            return false;
        }

        Unit unit = getUnitAt(from);
        if (unit == null) {
            return false;
        }
        if (unit.hasMovedThisTurn() || unit.hasActedThisTurn()) {
            return false;
        }
        if (from.equals(to)) {
            notifyObservers(GameEvent.move(unit, from, to));
            return true;
        }
        if (getUnitAt(to) != null) {
            return false;
        }

        List<Position> reachable = getReachableTiles(from);
        if (!reachable.contains(to)) {
            return false;
        }

        Tile fromTile = getTileAt(from);

        // Move is valid, update position and notify observers.
        unit.setPosition(to);
        unit.markMovedThisTurn();
        resetCaptureProgressIfLeavingBuilding(fromTile);
        notifyObservers(GameEvent.move(unit, from, to));
        return true;
    }

    public boolean waitUnit(Position unitPosition) {
        if (gameOver) {
            return false;
        }
        if (unitPosition == null || !isInsideMap(unitPosition)) {
            return false;
        }

        Unit unit = getUnitAt(unitPosition);
        if (unit == null) {
            return false;
        }
        if (unit.hasActedThisTurn()) {
            return false;
        }

        unit.markActedThisTurn();
        notifyObservers(GameEvent.wait(unit, unitPosition));
        return true;
    }

    public void endTurn() {
        if (gameOver) {
            return;
        }
        String nextPlayer = PLAYER_ONE_ID.equals(turn.getCurrentPlayer()) ? PLAYER_TWO_ID : PLAYER_ONE_ID;
        if (PLAYER_TWO_ID.equals(turn.getCurrentPlayer())) {
            turn.setTurnNumber(turn.getTurnNumber() + 1);
        }
        turn.setCurrentPlayer(nextPlayer);
        turn.setPhase(Turn.Phase.ACTION);
        resetUnitsForOwner(nextPlayer);
        notifyObservers(GameEvent.endTurn());
        applyIncomeForOwner(nextPlayer);
        applyRepairsForOwner(nextPlayer);
        notifyObservers(GameEvent.income());
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

    public Turn getTurn() {
        return new Turn(turn.getCurrentPlayer(), turn.getTurnNumber(), turn.getPhase());
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getWinnerPlayerId() {
        return winnerPlayerId;
    }

    public Player getPlayer(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player id must not be blank");
        }

        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                return new Player(player.getPlayerId(), player.getMoney());
            }
        }

        throw new IllegalArgumentException("Unknown player id: " + playerId);
    }

    public int getIncomeForPlayer(String playerId) {
        requirePlayerId(playerId);
        int income = 0;
        for (Tile[] row : map) {
            for (Tile tile : row) {
                if (tile.isCity() && playerId.equals(tile.getOwner())) {
                    income += CITY_INCOME_PER_TURN;
                }
            }
        }
        return income;
    }

    public boolean canPurchaseUnit(String type, Position factoryPosition) {
        if (gameOver) {
            return false;
        }
        UnitType unitType = UnitType.fromName(type);
        if (unitType == null) {
            return false;
        }
        if (factoryPosition == null || !isInsideMap(factoryPosition)) {
            return false;
        }
        if (turn.getPhase() != Turn.Phase.ACTION) {
            return false;
        }

        Tile tile = getTileAt(factoryPosition);
        String currentPlayer = turn.getCurrentPlayer();
        if (!tile.isFactory()) {
            return false;
        }
        if (!currentPlayer.equals(tile.getOwner())) {
            return false;
        }
        if (getUnitAt(factoryPosition) != null) {
            return false;
        }

        Player player = getMutablePlayer(currentPlayer);
        return player.getMoney() >= unitType.getCost();
    }

    public Unit purchaseUnit(String type, Position factoryPosition) {
        if (gameOver) {
            throw new IllegalArgumentException("Game is over");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Unit type must not be blank");
        }
        if (factoryPosition == null || !isInsideMap(factoryPosition)) {
            throw new IllegalArgumentException("Factory position is outside map: " + factoryPosition);
        }

        UnitType unitType = UnitType.fromName(type);
        if (unitType == null) {
            throw new IllegalArgumentException("Unknown unit type: " + type);
        }
        if (!canPurchaseUnit(type, factoryPosition)) {
            throw new IllegalArgumentException("Purchase is not allowed");
        }

        String currentPlayer = turn.getCurrentPlayer();
        Player player = getMutablePlayer(currentPlayer);
        player.setMoney(player.getMoney() - unitType.getCost());

        Unit created = createUnit(unitType.getDisplayName(), currentPlayer, factoryPosition.row(), factoryPosition.col());
        created.markMovedThisTurn();
        created.markActedThisTurn();
        notifyObservers(GameEvent.purchase(created, factoryPosition));
        return created;
    }

    public GameState snapshotState() {
        int height = getHeight();
        int width = getWidth();
        GameState.TileState[][] tileStates = new GameState.TileState[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Tile tile = map[row][col];
                int capturePoints = tile.isBuilding() ? tile.getCapturePointsRemaining() : 0;
                tileStates[row][col] = new GameState.TileState(
                    tile.getTerrainType(),
                    tile.getOwner(),
                    capturePoints
                );
            }
        }

        List<GameState.UnitState> unitStates = new ArrayList<>();
        for (Unit unit : units) {
            unitStates.add(new GameState.UnitState(
                unit.getType(),
                unit.getOwner(),
                unit.getPosition(),
                unit.getHp(),
                unit.hasMovedThisTurn(),
                unit.hasActedThisTurn()
            ));
        }

        List<GameState.PlayerState> playerStates = new ArrayList<>();
        for (Player player : players) {
            playerStates.add(new GameState.PlayerState(player.getPlayerId(), player.getMoney()));
        }

        GameState.TurnState turnState = new GameState.TurnState(
            turn.getCurrentPlayer(),
            turn.getTurnNumber(),
            turn.getPhase()
        );
        return new GameState(tileStates, unitStates, playerStates, turnState, gameOver, winnerPlayerId);
    }

    public static Game fromState(GameState state) {
        if (state == null) {
            throw new IllegalArgumentException("Game state must not be null");
        }
        if (state.tiles() == null || state.tiles().length == 0 || state.tiles()[0].length == 0) {
            throw new IllegalArgumentException("Game state tiles must not be empty");
        }
        if (state.turn() == null) {
            throw new IllegalArgumentException("Game state turn must not be null");
        }
        if (state.players() == null) {
            throw new IllegalArgumentException("Game state players must not be null");
        }
        if (state.units() == null) {
            throw new IllegalArgumentException("Game state units must not be null");
        }

        Tile[][] restoredTiles = new Tile[state.tiles().length][state.tiles()[0].length];
        for (int row = 0; row < state.tiles().length; row++) {
            if (state.tiles()[row] == null || state.tiles()[row].length != state.tiles()[0].length) {
                throw new IllegalArgumentException("Game state tiles must be rectangular");
            }
            for (int col = 0; col < state.tiles()[row].length; col++) {
                GameState.TileState tileState = state.tiles()[row][col];
                if (tileState == null || tileState.terrainType() == null) {
                    throw new IllegalArgumentException("Game state tile entries must include terrain type");
                }
                restoredTiles[row][col] = new Tile(tileState.terrainType());
            }
        }

        Game restoredGame = new Game(restoredTiles);
        restoredGame.applyStateOwnersAndCapture(state.tiles());
        restoredGame.applyStatePlayers(state.players());
        restoredGame.applyStateUnits(state.units());
        restoredGame.turn = new Turn(
            state.turn().currentPlayer(),
            state.turn().turnNumber(),
            state.turn().phase()
        );
        restoredGame.gameOver = state.gameOver();
        restoredGame.winnerPlayerId = state.winnerPlayerId();
        return restoredGame;
    }

    public boolean canHealUnitOnCurrentTile(Position unitPosition) {
        Unit unit = getRequiredUnit(unitPosition);
        Tile tile = getTileAt(unitPosition);
        return BUILDING_SERVICE.canHeal(unit, tile);
    }

    public int healUnitOnCurrentTile(Position unitPosition) {
        Unit unit = getRequiredUnit(unitPosition);
        Tile tile = getTileAt(unitPosition);
        return BUILDING_SERVICE.healIfEligible(unit, tile);
    }

    public boolean canAttack(Position attackerPosition, Position defenderPosition) {
        if (gameOver) {
            return false;
        }
        if (attackerPosition == null || defenderPosition == null) {
            return false;
        }
        if (!isInsideMap(attackerPosition) || !isInsideMap(defenderPosition)) {
            return false;
        }

        Unit attacker = getUnitAt(attackerPosition);
        Unit defender = getUnitAt(defenderPosition);
        if (attacker == null || defender == null) {
            return false;
        }

        return COMBAT_SERVICE.canAttack(attacker, defender);
    }

    public CombatResult attack(Position attackerPosition, Position defenderPosition) {
        if (gameOver) {
            throw new IllegalArgumentException("Game is over");
        }
        Unit attacker = getRequiredUnit(attackerPosition);
        Unit defender = getRequiredUnit(defenderPosition);

        if (!canAttack(attackerPosition, defenderPosition)) {
            throw new IllegalArgumentException("Attack is not allowed");
        }

        Tile attackerTile = getTileAt(attackerPosition);
        Tile defenderTile = getTileAt(defenderPosition);
        CombatResult result = COMBAT_SERVICE.resolveAttack(attacker, attackerTile, defender, defenderTile);
        removeDestroyedUnits(attacker, defender);
        notifyObservers(GameEvent.attack(attacker, defender, attackerPosition, defenderPosition, result));
        return result;
    }

    public boolean canCaptureBuilding(Position unitPosition) {
        if (gameOver) {
            return false;
        }
        Unit unit = getRequiredUnit(unitPosition);
        Tile tile = getTileAt(unitPosition);
        return BUILDING_SERVICE.canCapture(unit, tile);
    }

    public CaptureResult captureBuilding(Position unitPosition) {
        if (gameOver) {
            throw new IllegalArgumentException("Game is over");
        }
        Unit unit = getRequiredUnit(unitPosition);
        Tile tile = getTileAt(unitPosition);
        CaptureResult result = BUILDING_SERVICE.captureIfEligible(unit, tile);
        if (result.progressApplied()) {
            if (result.capturedHq()) {
                gameOver = true;
                winnerPlayerId = unit.getOwner();
            }
            notifyObservers(GameEvent.capture(unit, unitPosition));
        }
        return result;
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
        if (gameOver) {
            return reachable;
        }
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
                Unit occupant = getUnitAt(next);
                if (isOccupiedByEnemyUnit(occupant, unit)) {
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
                Position candidate = new Position(x, y);
                if (isOccupiedByAnotherUnit(candidate, unit)) {
                    continue;
                }
                reachable.add(candidate);
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

    private boolean isOccupiedByEnemyUnit(Unit occupant, Unit movingUnit) {
        return occupant != null
            && occupant != movingUnit
            && !occupant.getOwner().equals(movingUnit.getOwner());
    }

    private void requirePlayerId(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("Player id must not be blank");
        }
        getMutablePlayer(playerId);
    }

    void setPlayerMoney(String playerId, int money) {
        Player player = getMutablePlayer(playerId);
        player.setMoney(money);
    }

    void setTurnState(String currentPlayer, int turnNumber, Turn.Phase phase) {
        turn = new Turn(currentPlayer, turnNumber, phase);
    }

    private Player getMutablePlayer(String playerId) {
        for (Player player : players) {
            if (player.getPlayerId().equals(playerId)) {
                return player;
            }
        }
        throw new IllegalArgumentException("Unknown player id: " + playerId);
    }

    private int applyIncomeForOwner(String playerId) {
        Player player = getMutablePlayer(playerId);
        int income = getIncomeForPlayer(playerId);
        if (income > 0) {
            player.setMoney(player.getMoney() + income);
        }
        return income;
    }

    private void applyRepairsForOwner(String playerId) {
        Player player = getMutablePlayer(playerId);
        for (Unit unit : units) {
            if (!playerId.equals(unit.getOwner())) {
                continue;
            }

            Position unitPosition = unit.getPosition();
            Tile tile = getTileAt(unitPosition);
            if (!BUILDING_SERVICE.canHeal(unit, tile)) {
                continue;
            }

            int missingHp = 100 - unit.getHp();
            int plannedRepairHp = Math.min(MAX_REPAIR_HP_PER_TURN, missingHp);
            int repairCost = calculateRepairCost(unit.getType(), plannedRepairHp);
            if (repairCost <= 0) {
                continue;
            }
            if (player.getMoney() < repairCost) {
                continue;
            }

            player.setMoney(player.getMoney() - repairCost);
            unit.heal(plannedRepairHp);
        }
    }

    private int calculateRepairCost(UnitType unitType, int repairedHp) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type must not be null");
        }
        if (repairedHp <= 0) {
            return 0;
        }
        return unitType.getCost() * repairedHp * REPAIR_COST_PERCENT_PER_HP / 100;
    }

    private void initializeDefaultPlayers() {
        players.clear();
        players.add(new Player(PLAYER_ONE_ID, 0));
        players.add(new Player(PLAYER_TWO_ID, 0));
    }

    private void resetUnitsForOwner(String owner) {
        for (Unit unit : units) {
            if (owner.equals(unit.getOwner())) {
                unit.resetTurnFlags();
            }
        }
    }

    private void resetCaptureProgressIfLeavingBuilding(Tile tile) {
        if (!BUILDING_SERVICE.isCapturableBuilding(tile)) {
            return;
        }
        if (tile.getCapturePointsRemaining() < Tile.DEFAULT_CAPTURE_POINTS) {
            tile.resetCapturePoints();
        }
    }

    private void removeDestroyedUnits(Unit attacker, Unit defender) {
        if (defender != null && defender.isDestroyed()) {
            units.remove(defender);
        }
        if (attacker != null && attacker.isDestroyed()) {
            units.remove(attacker);
        }
    }

    private Unit getRequiredUnit(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position must not be null");
        }

        Unit unit = getUnitAt(position);
        if (unit == null) {
            throw new IllegalArgumentException("No unit at position: " + position);
        }
        return unit;
    }

    private void applyStateOwnersAndCapture(GameState.TileState[][] tileStates) {
        for (int row = 0; row < tileStates.length; row++) {
            for (int col = 0; col < tileStates[row].length; col++) {
                GameState.TileState tileState = tileStates[row][col];
                Tile tile = map[row][col];

                if (tileState.owner() == null) {
                    tile.clearOwner();
                } else {
                    tile.setOwner(tileState.owner());
                }

                if (tile.isBuilding()) {
                    int capturePoints = tileState.capturePointsRemaining();
                    if (capturePoints < 0 || capturePoints > Tile.DEFAULT_CAPTURE_POINTS) {
                        throw new IllegalArgumentException("Capture points out of range at (" + row + "," + col + ")");
                    }
                    tile.resetCapturePoints();
                    if (capturePoints < Tile.DEFAULT_CAPTURE_POINTS) {
                        tile.reduceCapturePoints(Tile.DEFAULT_CAPTURE_POINTS - capturePoints);
                    }
                }
            }
        }
    }

    private void applyStatePlayers(List<GameState.PlayerState> playerStates) {
        for (GameState.PlayerState playerState : playerStates) {
            if (playerState == null) {
                throw new IllegalArgumentException("Player state entry must not be null");
            }
            Player player = getMutablePlayer(playerState.playerId());
            player.setMoney(playerState.money());
        }
    }

    private void applyStateUnits(List<GameState.UnitState> unitStates) {
        for (GameState.UnitState unitState : unitStates) {
            if (unitState == null) {
                throw new IllegalArgumentException("Unit state entry must not be null");
            }
            Unit unit = createUnit(
                unitState.type().getDisplayName(),
                unitState.owner(),
                unitState.position().row(),
                unitState.position().col()
            );
            if (unitState.hp() < 0 || unitState.hp() > 100) {
                throw new IllegalArgumentException("Unit HP out of range at " + unitState.position());
            }
            if (unitState.hp() < 100) {
                unit.takeDamage(100 - unitState.hp());
            }
            if (unitState.movedThisTurn()) {
                unit.markMovedThisTurn();
            }
            if (unitState.actedThisTurn()) {
                unit.markActedThisTurn();
            }
        }
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
