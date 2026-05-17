package ija.bot;

import ija.common.Position;
import ija.game.CaptureResult;
import ija.game.Game;
import ija.game.Unit;
import ija.game.UnitType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Bot specification:
 * - buys random affordable units in own free factories
 * - moves units using random reachable tiles
 * - attacks if possible, otherwise captures, otherwise waits
 * - ends turn when done
 */
public final class DummyBot implements Bot {
    private final Random random;

    public DummyBot() {
        this(new Random());
    }

    public DummyBot(long seed) {
        this(new Random(seed));
    }

    private DummyBot(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random must not be null");
        }
        this.random = random;
    }

    @Override
    public void playTurn(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null");
        }
        if (game.isGameOver()) {
            return;
        }

        String currentPlayer = game.getTurn().getCurrentPlayer();
        purchaseAtFactories(game, currentPlayer);

        while (!game.isGameOver()) {
            List<Position> actionable = collectActionableUnits(game, currentPlayer);
            if (actionable.isEmpty()) {
                break;
            }

            Position unitPosition = actionable.get(random.nextInt(actionable.size()));
            playSingleUnit(game, unitPosition);
        }

        if (!game.isGameOver()) {
            game.endTurn();
        }
    }

    private void purchaseAtFactories(Game game, String owner) {
        while (true) {
            List<PurchaseOption> options = collectPurchaseOptions(game, owner);
            if (options.isEmpty()) {
                return;
            }

            PurchaseOption choice = options.get(random.nextInt(options.size()));
            game.purchaseUnit(choice.unitType().getDisplayName(), choice.factoryPosition());
        }
    }

    private List<PurchaseOption> collectPurchaseOptions(Game game, String owner) {
        List<PurchaseOption> options = new ArrayList<>();
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position position = new Position(row, col);
                Unit unit = game.getUnitAt(position);
                if (unit != null) {
                    continue;
                }
                if (!game.getTileAt(position).isFactory()) {
                    continue;
                }
                if (!owner.equals(game.getTileAt(position).getOwner())) {
                    continue;
                }

                for (UnitType type : UnitType.values()) {
                    if (game.canPurchaseUnit(type.getDisplayName(), position)) {
                        options.add(new PurchaseOption(position, type));
                    }
                }
            }
        }
        return options;
    }

    private List<Position> collectActionableUnits(Game game, String owner) {
        List<Position> positions = new ArrayList<>();
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position position = new Position(row, col);
                Unit unit = game.getUnitAt(position);
                if (unit == null) {
                    continue;
                }
                if (!owner.equals(unit.getOwner())) {
                    continue;
                }
                if (unit.hasActedThisTurn()) {
                    continue;
                }
                positions.add(position);
            }
        }
        return positions;
    }

    private void playSingleUnit(Game game, Position startPosition) {
        Unit unit = game.getUnitAt(startPosition);
        if (unit == null || unit.hasActedThisTurn()) {
            return;
        }

        List<Position> movementChoices = new ArrayList<>(game.getReachableTiles(startPosition));
        Collections.shuffle(movementChoices, random);
        movementChoices.add(startPosition);

        for (Position target : movementChoices) {
            Position actionPosition = startPosition;
            if (!target.equals(startPosition)) {
                boolean moved = game.moveUnit(startPosition, target);
                if (!moved) {
                    continue;
                }
                actionPosition = target;
            }

            if (tryAttack(game, actionPosition)) {
                return;
            }
            if (tryCapture(game, actionPosition)) {
                return;
            }
            game.waitUnit(actionPosition);
            return;
        }

        game.waitUnit(startPosition);
    }

    private boolean tryAttack(Game game, Position attackerPosition) {
        List<Position> targets = new ArrayList<>();
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position target = new Position(row, col);
                if (game.canAttack(attackerPosition, target)) {
                    targets.add(target);
                }
            }
        }
        if (targets.isEmpty()) {
            return false;
        }

        Position selectedTarget = targets.get(random.nextInt(targets.size()));
        game.attack(attackerPosition, selectedTarget);
        return true;
    }

    private boolean tryCapture(Game game, Position unitPosition) {
        if (!game.canCaptureBuilding(unitPosition)) {
            return false;
        }
        CaptureResult result = game.captureBuilding(unitPosition);
        if (!result.progressApplied()) {
            return false;
        }
        game.waitUnit(unitPosition);
        return true;
    }

    private record PurchaseOption(
        Position factoryPosition,
        UnitType unitType
    ) {
    }
}
