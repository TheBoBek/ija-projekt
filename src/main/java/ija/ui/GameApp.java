package ija.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import ija.bot.DummyBot;
import ija.command.AttackCommand;
import ija.command.CaptureCommand;
import ija.command.CommandResult;
import ija.command.EndTurnCommand;
import ija.command.GameCommand;
import ija.command.GameCommandHistory;
import ija.command.MoveCommand;
import ija.command.PurchaseCommand;
import ija.command.WaitCommand;
import ija.common.GameEvent;
import ija.common.GameEventType;
import ija.common.Position;
import ija.game.CaptureResult;
import ija.game.CombatResult;
import ija.game.Game;
import ija.game.GameFactory;
import ija.game.GameState;
import ija.game.Tile;
import ija.game.Turn;
import ija.game.Unit;
import ija.game.UnitType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaFX entry point for the project UI.
 */
public class GameApp extends Application {
    private static final double BASE_CELL_SIZE = 72;
    private static final double UNIT_SCALE = 0.8;
    private static final double SOLDIER_EXTRA_SCALE = 0.68;
    private static final double GRID_GAP = 2;
    private static final double GRID_PADDING = 6;
    private static final Path SCENARIO_ONE_PATH = Path.of("maps", "scenario-alpha.json");
    private static final Path SCENARIO_TWO_PATH = Path.of("maps", "scenario-beta.json");

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final DummyBot dummyBot = new DummyBot();
    private final SpriteLibrary sprites = new SpriteLibrary();
    private final Map<Position, StackPane> cellViews = new HashMap<>();
    private final Map<Unit, Integer> facingByUnit = new IdentityHashMap<>();
    private final List<ReplayEntry> replayEntries = new ArrayList<>();
    private final List<Integer> replayTurnEntryIndices = new ArrayList<>();
    private final GameCommandHistory commandHistory = new GameCommandHistory();

    private final Label turnLabel = new Label();
    private final Label fundsLabel = new Label();
    private final Label statusLabel = new Label();
    private final TextArea eventLog = new TextArea();

    private BorderPane rootLayout;
    private GridPane boardGrid;
    private StackPane boardContainer;
    private VBox terminalPanel;

    private Game game;
    private Position selectedUnit;
    private List<Position> reachableTiles = List.of();
    private double currentCellSize = BASE_CELL_SIZE;
    private ContextMenu activeMenu;
    private Position activeMenuPosition;
    private Button endTurnButton;
    private Button botTurnButton;
    private Button botVsBotStartButton;
    private Button botVsBotStopButton;
    private Button replayBackButton;
    private Button replayForwardButton;
    private Button replayLiveButton;
    private Button mapOneButton;
    private Button mapTwoButton;
    private Button terminalToggleButton;
    private boolean replayMode;
    private int replayIndex;
    private Timeline botVsBotTimeline;
    private boolean gameOverAnnounced;
    private boolean terminalVisible = true;

    @Override
    public void start(Stage stage) {
        initializeGame(loadInitialGame(), "Game start.");

        rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(12));
        rootLayout.setTop(buildTopBar());
        rootLayout.setCenter(buildBoard());
        rootLayout.setRight(buildTerminalPanel());

        refreshHud();
        setStatus("Select your unit to start.");
        renderBoard();
        refreshReplayControls();

        Scene scene = new Scene(rootLayout, 1200, 880);
        stage.setTitle("IJA Advance Wars");
        stage.setScene(scene);
        stage.setOnShown(event -> updateCellSize());
        stage.setOnCloseRequest(event -> stopBotVsBot(false));
        stage.show();
    }

    private Node buildTopBar() {
        endTurnButton = new Button("End Turn");
        endTurnButton.setOnAction(event -> {
            if (replayMode) {
                setStatus("Replay mode is active. Switch to Live mode to keep playing.");
                return;
            }
            if (game.isGameOver()) {
                announceGameOverIfNeeded();
                return;
            }
            CommandResult<Void> endTurnResult = executeCommand(new EndTurnCommand());
            if (!endTurnResult.stateChanged()) {
                setStatus("Turn cannot be ended right now.");
                return;
            }
            clearSelection();
            refreshHud();
            setStatus("Turn ended.");
            renderBoard();
        });

        Button saveLogButton = new Button("Save Log");
        saveLogButton.setOnAction(event -> saveReplayLogToFile());

        Button loadLogButton = new Button("Load Log");
        loadLogButton.setOnAction(event -> loadReplayLogFromFile());

        botTurnButton = new Button("Bot Turn");
        botTurnButton.setOnAction(event -> executeDummyBotTurn());

        botVsBotStartButton = new Button("Bot vs Bot Start");
        botVsBotStartButton.setOnAction(event -> startBotVsBot());

        botVsBotStopButton = new Button("Bot vs Bot Stop");
        botVsBotStopButton.setOnAction(event -> stopBotVsBot(true));

        mapOneButton = new Button("Scenario 1");
        mapOneButton.setOnAction(event -> loadBundledScenario(SCENARIO_ONE_PATH, "Scenario 1"));

        mapTwoButton = new Button("Scenario 2");
        mapTwoButton.setOnAction(event -> loadBundledScenario(SCENARIO_TWO_PATH, "Scenario 2"));

        replayBackButton = new Button("Replay Turn <");
        replayBackButton.setOnAction(event -> stepReplayBackward());

        replayForwardButton = new Button("Replay Turn >");
        replayForwardButton.setOnAction(event -> stepReplayForward());

        replayLiveButton = new Button("Live Mode");
        replayLiveButton.setOnAction(event -> switchReplayToLive());

        terminalToggleButton = new Button("Hide Terminal");
        terminalToggleButton.setOnAction(event -> toggleTerminalVisibility());

        HBox bar = new HBox(
            12,
            turnLabel,
            fundsLabel,
            endTurnButton,
            saveLogButton,
            loadLogButton,
            botTurnButton,
            botVsBotStartButton,
            botVsBotStopButton,
            mapOneButton,
            mapTwoButton,
            replayBackButton,
            replayForwardButton,
            replayLiveButton,
            terminalToggleButton,
            statusLabel
        );
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(4, 0, 10, 0));
        return bar;
    }

    private Node buildBoard() {
        boardGrid = new GridPane();
        boardGrid.setHgap(GRID_GAP);
        boardGrid.setVgap(GRID_GAP);
        boardGrid.setPadding(new Insets(GRID_PADDING));
        boardGrid.setStyle("-fx-background-color: #1e1e1e;");
        boardGrid.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position position = new Position(row, col);
                StackPane cell = createCell(position);
                boardGrid.add(cell, col, row);
                cellViews.put(position, cell);
            }
        }

        boardContainer = new StackPane(boardGrid);
        boardContainer.setStyle("-fx-background-color: #202225;");
        boardContainer.setMinSize(0, 0);
        boardContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        boardContainer.widthProperty().addListener((obs, oldValue, newValue) -> updateCellSize());
        boardContainer.heightProperty().addListener((obs, oldValue, newValue) -> updateCellSize());
        return boardContainer;
    }

    private Node buildTerminalPanel() {
        eventLog.setEditable(false);
        eventLog.setPrefRowCount(22);
        eventLog.setWrapText(true);
        eventLog.setFocusTraversable(false);
        eventLog.setStyle(
            "-fx-font-family: 'Menlo'; "
                + "-fx-font-size: 12px; "
                + "-fx-control-inner-background: #4a5058; "
                + "-fx-text-fill: #f3f4f6; "
                + "-fx-highlight-fill: #6b7280; "
                + "-fx-highlight-text-fill: #ffffff;"
        );
        VBox.setVgrow(eventLog, Priority.ALWAYS);

        Button closeTerminalButton = new Button("Close");
        closeTerminalButton.setOnAction(event -> setTerminalVisible(false));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(8, spacer, closeTerminalButton);
        header.setAlignment(Pos.CENTER_LEFT);

        terminalPanel = new VBox(8, header, eventLog);
        terminalPanel.setPadding(new Insets(8));
        terminalPanel.setPrefWidth(360);
        terminalPanel.setMinWidth(300);
        terminalPanel.setMaxWidth(460);
        terminalPanel.setStyle("-fx-background-color: #3a4048;");
        return terminalPanel;
    }

    private Game loadInitialGame() {
        try {
            return GameFactory.createGameFromJson(SCENARIO_ONE_PATH);
        } catch (IllegalArgumentException ignored) {
            try {
                return GameFactory.createGameFromJson(SCENARIO_TWO_PATH);
            } catch (IllegalArgumentException ignoredAgain) {
                return createFallbackDemoGame();
            }
        }
    }

    private void loadBundledScenario(Path scenarioPath, String label) {
        if (scenarioPath == null) {
            return;
        }
        stopBotVsBot(false);
        closeActiveMenu();
        clearSelection();
        replayMode = false;

        try {
            Game loaded = GameFactory.createGameFromJson(scenarioPath);
            initializeGame(loaded, "Game start (" + label + ").");
            setStatus(label + " loaded.");
            renderBoard();
            updateCellSize();
        } catch (IllegalArgumentException ex) {
            setStatus("Loading " + label + " failed: " + ex.getMessage());
        }
    }

    private void initializeGame(Game loadedGame, String firstReplayEntryMessage) {
        if (loadedGame == null) {
            throw new IllegalArgumentException("Loaded game must not be null");
        }
        replayMode = false;
        closeActiveMenu();
        clearSelection();
        game = loadedGame;
        game.addObserver(this::onGameEvent);
        commandHistory.clear();
        gameOverAnnounced = false;
        facingByUnit.clear();
        seedDefaultFacingForAllUnits();
        resetReplayProtocol(firstReplayEntryMessage);
        if (boardGrid != null) {
            rebuildBoardGridCells();
        }
        refreshHud();
        refreshReplayControls();
    }

    private void rebuildBoardGridCells() {
        boardGrid.getChildren().clear();
        cellViews.clear();
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position position = new Position(row, col);
                StackPane cell = createCell(position);
                boardGrid.add(cell, col, row);
                cellViews.put(position, cell);
            }
        }
    }

    private void seedDefaultFacingForAllUnits() {
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Unit unit = game.getUnitAt(new Position(row, col));
                if (unit == null) {
                    continue;
                }
                facingByUnit.put(unit, defaultFacingForOwner(unit.getOwner()));
            }
        }
    }

    private void announceGameOverIfNeeded() {
        if (game == null || !game.isGameOver() || gameOverAnnounced) {
            return;
        }
        stopBotVsBot(false);
        closeActiveMenu();
        clearSelection();
        String winner = game.getWinnerPlayerId();
        String message = winner == null
            ? "Game over."
            : "Game over: " + winner + " captured an HQ.";
        setStatus(message);
        appendLog(message);
        gameOverAnnounced = true;
        refreshReplayControls();
    }

    private StackPane createCell(Position position) {
        StackPane cell = new StackPane();
        cell.setMinSize(currentCellSize, currentCellSize);
        cell.setPrefSize(currentCellSize, currentCellSize);
        cell.setMaxSize(currentCellSize, currentCellSize);
        cell.setOnMouseClicked(mouseEvent -> onCellClicked(position, mouseEvent.getScreenX(), mouseEvent.getScreenY()));
        return cell;
    }

    private void onCellClicked(Position clicked, double screenX, double screenY) {
        if (replayMode) {
            setStatus("Replay mode is active. Use replay buttons or switch to Live mode.");
            return;
        }
        if (isBotVsBotRunning()) {
            setStatus("Bot vs Bot is running. Stop it to interact manually.");
            return;
        }
        if (game.isGameOver()) {
            announceGameOverIfNeeded();
            return;
        }
        if (isActiveMenuForPosition(clicked)) {
            return;
        }
        if (isAnyMenuActive()) {
            closeActiveMenu();
        }

        Unit clickedUnit = game.getUnitAt(clicked);
        String currentPlayer = game.getTurn().getCurrentPlayer();

        if (selectedUnit == null) {
            if (isSelectable(clickedUnit, currentPlayer)) {
                selectUnit(clicked);
            } else if (isActionSelectable(clickedUnit, currentPlayer)) {
                selectUnitForActionOnly(clicked, screenX, screenY);
            } else if (clickedUnit == null && canOpenFactoryMenu(clicked, currentPlayer)) {
                showFactoryMenu(clicked, screenX, screenY);
            } else {
                setStatus("Select one of " + currentPlayer + " units that has not acted.");
            }
            return;
        }

        if (clicked.equals(selectedUnit)) {
            showActionMenu(selectedUnit, screenX, screenY);
            return;
        }

        if (isSelectable(clickedUnit, currentPlayer)) {
            selectUnit(clicked);
            return;
        }

        if (isActionSelectable(clickedUnit, currentPlayer)) {
            selectUnitForActionOnly(clicked, screenX, screenY);
            return;
        }

        if (reachableTiles.contains(clicked)) {
            Position from = selectedUnit;
            boolean moved = executeCommand(new MoveCommand(from, clicked)).value();
            if (moved) {
                selectedUnit = clicked;
                reachableTiles = List.of();
                setStatus("Choose action: Attack, Capture or Wait.");
                renderBoard();
                showActionMenu(clicked, screenX, screenY);
            } else {
                setStatus("Move is not valid.");
            }
            return;
        }

        setStatus("Target tile is not reachable.");
    }

    private void selectUnit(Position unitPosition) {
        selectedUnit = unitPosition;
        reachableTiles = game.getReachableTiles(unitPosition);
        setStatus("Unit selected. Click destination or click unit again for actions.");
        renderBoard();
    }

    private void selectUnitForActionOnly(Position unitPosition, double screenX, double screenY) {
        selectedUnit = unitPosition;
        reachableTiles = List.of();
        setStatus("Unit has already moved. Choose action: Attack, Capture or Wait.");
        renderBoard();
        showActionMenu(unitPosition, screenX, screenY);
    }

    private void showActionMenu(Position actionPosition, double screenX, double screenY) {
        Unit actingUnit = game.getUnitAt(actionPosition);
        if (actingUnit == null) {
            clearSelection();
            return;
        }

        ContextMenu menu = new ContextMenu();
        List<Position> attackTargets = findAttackTargets(actionPosition);
        boolean hasOptionalAction = false;

        if (!attackTargets.isEmpty()) {
            hasOptionalAction = true;
            if (attackTargets.size() == 1) {
                Position target = attackTargets.get(0);
                MenuItem attackItem = new MenuItem("Attack " + describeUnitAt(target) + " " + formatPosition(target));
                attackItem.setOnAction(event -> performAttack(actionPosition, target));
                menu.getItems().add(attackItem);
            } else {
                Menu attackMenu = new Menu("Attack");
                for (Position target : attackTargets) {
                    MenuItem targetItem = new MenuItem(describeUnitAt(target) + " " + formatPosition(target));
                    targetItem.setOnAction(event -> performAttack(actionPosition, target));
                    attackMenu.getItems().add(targetItem);
                }
                menu.getItems().add(attackMenu);
            }
        }

        if (game.canCaptureBuilding(actionPosition)) {
            hasOptionalAction = true;
            MenuItem captureItem = new MenuItem("Capture");
            captureItem.setOnAction(event -> performCapture(actionPosition));
            menu.getItems().add(captureItem);
        }

        if (hasOptionalAction) {
            menu.getItems().add(new SeparatorMenuItem());
        }

        MenuItem waitItem = new MenuItem("Wait");
        waitItem.setOnAction(event -> performWait(actionPosition));
        menu.getItems().add(waitItem);

        showManagedMenu(menu, actionPosition, screenX, screenY);
    }

    private void performAttack(Position attacker, Position target) {
        try {
            CombatResult result = executeCommand(new AttackCommand(attacker, target)).value();
            setStatus("Attack resolved. Defender HP: " + result.defenderHpAfter());
            clearSelection();
            renderBoard();
        } catch (IllegalArgumentException ex) {
            setStatus("Attack failed: " + ex.getMessage());
        }
    }

    private void performCapture(Position position) {
        try {
            CaptureResult result = executeCommand(new CaptureCommand(position)).value();
            if (!result.progressApplied()) {
                setStatus("Capture is not available.");
                return;
            }
            setStatus("Capture progress: " + result.capturePointsBefore() + " -> " + result.capturePointsAfter());
            clearSelection();
            renderBoard();
            announceGameOverIfNeeded();
        } catch (IllegalArgumentException ex) {
            setStatus("Capture failed: " + ex.getMessage());
        }
    }

    private void performWait(Position position) {
        if (executeCommand(new WaitCommand(position)).value()) {
            setStatus("Unit waits.");
            clearSelection();
            renderBoard();
        } else {
            setStatus("Wait is not available for this unit.");
        }
    }

    private boolean canOpenFactoryMenu(Position position, String currentPlayer) {
        Tile tile = game.getTileAt(position);
        return tile.isFactory() && currentPlayer.equals(tile.getOwner());
    }

    private void showFactoryMenu(Position factoryPosition, double screenX, double screenY) {
        ContextMenu menu = new ContextMenu();
        for (UnitType unitType : UnitType.values()) {
            String label = "Buy " + unitType.getDisplayName() + " ($" + unitType.getCost() + ")";
            MenuItem item = new MenuItem(label);
            item.setDisable(!game.canPurchaseUnit(unitType.getDisplayName(), factoryPosition));
            item.setOnAction(event -> performPurchase(factoryPosition, unitType));
            menu.getItems().add(item);
        }
        showManagedMenu(menu, factoryPosition, screenX, screenY);
    }

    private void performPurchase(Position factoryPosition, UnitType unitType) {
        try {
            Unit created = executeCommand(new PurchaseCommand(unitType.getDisplayName(), factoryPosition)).value();
            int defaultFacing = "P2".equals(created.getOwner()) ? -1 : 1;
            facingByUnit.put(created, defaultFacing);
            refreshHud();
            setStatus("Bought " + created.getType().getDisplayName() + " at " + formatPosition(factoryPosition) + ".");
            renderBoard();
        } catch (IllegalArgumentException ex) {
            setStatus("Purchase failed: " + ex.getMessage());
        }
    }

    private List<Position> findAttackTargets(Position attacker) {
        List<Position> targets = new ArrayList<>();
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position candidate = new Position(row, col);
                if (candidate.equals(attacker)) {
                    continue;
                }
                if (game.canAttack(attacker, candidate)) {
                    targets.add(candidate);
                }
            }
        }
        targets.sort(Comparator.comparingInt(Position::row).thenComparingInt(Position::col));
        return targets;
    }

    private void renderBoard() {
        List<Position> attackTargets = selectedUnit == null ? List.of() : findAttackTargets(selectedUnit);

        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position position = new Position(row, col);
                StackPane cell = cellViews.get(position);
                if (cell == null) {
                    continue;
                }

                Tile tile = game.getTileAt(position);
                Unit unit = game.getUnitAt(position);
                cell.getChildren().setAll(buildTileNode(tile));

                if (unit != null) {
                    cell.getChildren().add(buildUnitNode(unit));
                    if (unit.hasActedThisTurn()) {
                        cell.getChildren().add(new Rectangle(currentCellSize, currentCellSize, Color.color(0, 0, 0, 0.28)));
                    }
                }

                if (reachableTiles.contains(position)) {
                    cell.getChildren().add(new Rectangle(currentCellSize, currentCellSize, Color.color(0.2, 0.55, 1.0, 0.35)));
                }
                if (attackTargets.contains(position)) {
                    cell.getChildren().add(new Rectangle(currentCellSize, currentCellSize, Color.color(1.0, 0.2, 0.2, 0.2)));
                }
                if (position.equals(selectedUnit)) {
                    Rectangle border = new Rectangle(currentCellSize - 2, currentCellSize - 2);
                    border.setFill(Color.TRANSPARENT);
                    border.setStroke(Color.GOLD);
                    border.setStrokeWidth(3);
                    cell.getChildren().add(border);
                }
            }
        }
    }

    private Node buildTileNode(Tile tile) {
        Image sprite = sprites.terrain(tile);
        if (sprite != null) {
            ImageView imageView = new ImageView(sprite);
            imageView.setFitWidth(currentCellSize);
            imageView.setFitHeight(currentCellSize);
            imageView.setPreserveRatio(false);
            return imageView;
        }

        Color fallback = switch (tile.getTerrainType()) {
            case PLAIN -> Color.web("#5fa24a");
            case FOREST -> Color.web("#2f6f35");
            case MOUNTAIN -> Color.web("#8a7f70");
            case WATER -> Color.web("#3c72cc");
            case CITY -> Color.web("#6f6f6f");
            case FACTORY -> Color.web("#7a5d44");
            case HQ -> Color.web("#5a3e86");
        };
        return new Rectangle(currentCellSize, currentCellSize, fallback);
    }

    private Node buildUnitNode(Unit unit) {
        Image sprite = sprites.unit(unit);
        double unitSize = currentCellSize * UNIT_SCALE;
        if (unit.getType() == ija.game.UnitType.INFANTRY) {
            unitSize *= SOLDIER_EXTRA_SCALE;
        }
        int facing = "P2".equals(unit.getOwner()) ? -1 : facingByUnit.getOrDefault(unit, 1);
        if (sprite != null) {
            ImageView imageView = new ImageView(sprite);
            imageView.setFitWidth(unitSize);
            imageView.setFitHeight(unitSize);
            imageView.setPreserveRatio(true);
            imageView.setScaleX(facing);
            return imageView;
        }

        Rectangle fallback = new Rectangle(unitSize, unitSize, Color.color(0.9, 0.2, 0.2, 0.9));
        if ("P2".equals(unit.getOwner())) {
            fallback.setFill(Color.color(0.2, 0.45, 0.95, 0.9));
        }
        fallback.setScaleX(facing);
        return fallback;
    }

    private boolean isSelectable(Unit unit, String currentPlayer) {
        if (unit == null) {
            return false;
        }
        if (!currentPlayer.equals(unit.getOwner())) {
            return false;
        }
        return !unit.hasMovedThisTurn() && !unit.hasActedThisTurn();
    }

    private boolean isActionSelectable(Unit unit, String currentPlayer) {
        if (unit == null) {
            return false;
        }
        if (!currentPlayer.equals(unit.getOwner())) {
            return false;
        }
        return unit.hasMovedThisTurn() && !unit.hasActedThisTurn();
    }

    private void onGameEvent(GameEvent event) {
        if (event.type() == GameEventType.MOVE) {
            String message = "Move: " + event.actor().getOwner() + " " + event.actor().getType().getDisplayName()
                + " " + formatPosition(event.from()) + " -> " + formatPosition(event.to());
            updateFacingFromAction(event.actor(), event.from(), event.to());
            appendLog(message);
            recordAction(message);
        } else if (event.type() == GameEventType.ATTACK) {
            CombatResult combat = event.combatResult();
            String message = "Attack: " + describeUnit(event.actor()) + " -> " + describeUnit(event.target())
                + " | dmg " + combat.damageToDefender() + "/" + combat.damageToAttacker();
            updateFacingFromAction(event.actor(), event.from(), event.to());
            appendLog(message);
            recordAction(message);
        } else if (event.type() == GameEventType.WAIT) {
            String message = "Wait: " + describeUnit(event.actor()) + " at " + formatPosition(event.from());
            appendLog(message);
            recordAction(message);
        } else if (event.type() == GameEventType.CAPTURE) {
            String message = "Capture: " + describeUnit(event.actor()) + " at " + formatPosition(event.from());
            appendLog(message);
            recordAction(message);
            announceGameOverIfNeeded();
        } else if (event.type() == GameEventType.PURCHASE) {
            String message = "Purchase: " + describeUnit(event.actor()) + " at " + formatPosition(event.from());
            appendLog(message);
            recordAction(message);
        } else if (event.type() == GameEventType.END_TURN) {
            Turn turn = game.getTurn();
            appendLog("Turn switched to " + turn.getCurrentPlayer());
        } else if (event.type() == GameEventType.INCOME) {
            Turn turn = game.getTurn();
            int awardedIncome = game.getIncomeForPlayer(turn.getCurrentPlayer());
            String message = "Turn start: " + turn.getCurrentPlayer() + " | Income +" + awardedIncome;
            appendLog(message);
            recordAction(message);
        }
    }

    private void updateFacingFromAction(Unit unit, Position from, Position to) {
        if (unit == null || from == null || to == null) {
            return;
        }
        if ("P2".equals(unit.getOwner())) {
            facingByUnit.put(unit, -1);
            return;
        }
        if (to.col() < from.col()) {
            facingByUnit.put(unit, -1);
        } else if (to.col() > from.col()) {
            facingByUnit.put(unit, 1);
        }
    }

    private void updateCellSize() {
        if (boardContainer == null || boardGrid == null) {
            return;
        }
        double availableWidth = boardContainer.getWidth();
        double availableHeight = boardContainer.getHeight();
        if (availableWidth <= 0 || availableHeight <= 0 || game.getWidth() <= 0 || game.getHeight() <= 0) {
            return;
        }

        double horizontalOverhead = (game.getWidth() - 1) * GRID_GAP + 2 * GRID_PADDING;
        double verticalOverhead = (game.getHeight() - 1) * GRID_GAP + 2 * GRID_PADDING;
        double widthPerCell = (availableWidth - horizontalOverhead) / game.getWidth();
        double heightPerCell = (availableHeight - verticalOverhead) / game.getHeight();
        if (widthPerCell <= 0 || heightPerCell <= 0) {
            return;
        }

        double nextSize = Math.max(24, Math.floor(Math.min(widthPerCell, heightPerCell)));
        if (Math.abs(nextSize - currentCellSize) < 0.5) {
            return;
        }

        currentCellSize = nextSize;
        for (StackPane cell : cellViews.values()) {
            cell.setMinSize(currentCellSize, currentCellSize);
            cell.setPrefSize(currentCellSize, currentCellSize);
            cell.setMaxSize(currentCellSize, currentCellSize);
        }
        renderBoard();
    }

    private void refreshTurnLabel() {
        Turn turn = game.getTurn();
        turnLabel.setText("Turn " + turn.getTurnNumber() + " | Current player: " + turn.getCurrentPlayer());
    }

    private void refreshFundsLabel() {
        Turn turn = game.getTurn();
        int money = game.getPlayer(turn.getCurrentPlayer()).getMoney();
        fundsLabel.setText("Funds: $" + money);
    }

    private void refreshHud() {
        refreshTurnLabel();
        refreshFundsLabel();
    }

    private void resetReplayProtocol(String firstEntryMessage) {
        replayEntries.clear();
        replayEntries.add(captureReplayEntry(firstEntryMessage));
        replayIndex = 0;
        rebuildReplayTurnAnchors();
    }

    private void executeDummyBotTurn() {
        if (replayMode) {
            setStatus("Replay mode is active. Switch to Live mode to run bots.");
            return;
        }
        if (game.isGameOver()) {
            announceGameOverIfNeeded();
            return;
        }
        closeActiveMenu();
        clearSelection();

        String botPlayer = game.getTurn().getCurrentPlayer();
        dummyBot.playTurn(game);
        refreshHud();
        renderBoard();

        setStatus("Dummy Bot played full turn for " + botPlayer + ".");
        announceGameOverIfNeeded();
    }

    private void startBotVsBot() {
        if (replayMode) {
            setStatus("Replay mode is active. Switch to Live mode first.");
            return;
        }
        if (game.isGameOver()) {
            announceGameOverIfNeeded();
            return;
        }
        if (botVsBotTimeline == null) {
            botVsBotTimeline = new Timeline(new KeyFrame(Duration.millis(500), event -> executeDummyBotTurn()));
            botVsBotTimeline.setCycleCount(Timeline.INDEFINITE);
        }
        if (botVsBotTimeline.getStatus() != Timeline.Status.RUNNING) {
            botVsBotTimeline.play();
            setStatus("Bot vs Bot started.");
            refreshReplayControls();
        }
    }

    private void stopBotVsBot(boolean updateStatus) {
        if (botVsBotTimeline != null && botVsBotTimeline.getStatus() == Timeline.Status.RUNNING) {
            botVsBotTimeline.stop();
            if (updateStatus) {
                setStatus("Bot vs Bot stopped.");
            }
            refreshReplayControls();
        }
    }

    private boolean isBotVsBotRunning() {
        return botVsBotTimeline != null && botVsBotTimeline.getStatus() == Timeline.Status.RUNNING;
    }

    private void recordAction(String actionDescription) {
        if (replayMode) {
            return;
        }
        replayEntries.add(captureReplayEntry(actionDescription));
        replayIndex = replayEntries.size() - 1;
        rebuildReplayTurnAnchors();
        refreshReplayControls();
    }

    private ReplayEntry captureReplayEntry(String actionDescription) {
        return new ReplayEntry(actionDescription, game.snapshotState(), captureFacingSnapshot());
    }

    private List<ReplayFacing> captureFacingSnapshot() {
        List<ReplayFacing> facingSnapshot = new ArrayList<>();
        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position position = new Position(row, col);
                Unit unit = game.getUnitAt(position);
                if (unit == null) {
                    continue;
                }
                int facing = facingByUnit.getOrDefault(unit, defaultFacingForOwner(unit.getOwner()));
                facingSnapshot.add(new ReplayFacing(row, col, facing));
            }
        }
        return facingSnapshot;
    }

    private int defaultFacingForOwner(String owner) {
        return "P2".equals(owner) ? -1 : 1;
    }

    private void saveReplayLogToFile() {
        if (replayEntries.isEmpty()) {
            setStatus("Nothing to save yet.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Replay Log");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));
        chooser.setInitialFileName("game-replay-log.json");

        File selectedFile = chooser.showSaveDialog(endTurnButton.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            ReplayLogFile logFile = new ReplayLogFile(new ArrayList<>(replayEntries));
            Files.writeString(selectedFile.toPath(), gson.toJson(logFile));
            setStatus("Replay log saved to " + selectedFile.getName());
        } catch (IOException ex) {
            setStatus("Saving replay log failed: " + ex.getMessage());
        }
    }

    private void loadReplayLogFromFile() {
        stopBotVsBot(false);
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load Replay Log");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON files", "*.json"));

        File selectedFile = chooser.showOpenDialog(endTurnButton.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            String json = Files.readString(selectedFile.toPath());
            ReplayLogFile loaded = gson.fromJson(json, ReplayLogFile.class);
            if (loaded == null || loaded.entries() == null || loaded.entries().isEmpty()) {
                setStatus("Replay log is empty.");
                return;
            }

            replayEntries.clear();
            replayEntries.addAll(loaded.entries());
            rebuildReplayTurnAnchors();
            enterReplayModeAt(0);
            setStatus("Replay loaded: " + selectedFile.getName());
        } catch (IOException | JsonParseException ex) {
            setStatus("Loading replay log failed: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            setStatus("Replay log is invalid: " + ex.getMessage());
        }
    }

    private void enterReplayModeAt(int index) {
        if (replayEntries.isEmpty()) {
            setStatus("No replay entries available.");
            return;
        }
        stopBotVsBot(false);
        replayMode = true;
        closeActiveMenu();
        clearSelection();
        replayIndex = Math.max(0, Math.min(index, replayEntries.size() - 1));
        applyReplayEntry(replayIndex);
        refreshReplayControls();
    }

    private void stepReplayBackward() {
        if (!replayMode || replayEntries.isEmpty()) {
            return;
        }
        int turnPosition = currentReplayTurnPosition();
        if (turnPosition <= 0) {
            return;
        }
        replayIndex = replayTurnEntryIndices.get(turnPosition - 1);
        applyReplayEntry(replayIndex);
        refreshReplayControls();
    }

    private void stepReplayForward() {
        if (!replayMode || replayEntries.isEmpty()) {
            return;
        }
        int turnPosition = currentReplayTurnPosition();
        if (turnPosition < 0 || turnPosition >= replayTurnEntryIndices.size() - 1) {
            return;
        }
        replayIndex = replayTurnEntryIndices.get(turnPosition + 1);
        applyReplayEntry(replayIndex);
        refreshReplayControls();
    }

    private void applyReplayEntry(int index) {
        ReplayEntry entry = replayEntries.get(index);
        if (entry == null || entry.state() == null) {
            throw new IllegalArgumentException("Replay entry at index " + index + " is missing game state");
        }

        game = Game.fromState(entry.state());
        game.addObserver(this::onGameEvent);
        facingByUnit.clear();
        restoreFacingSnapshot(entry.facings());
        refreshHud();
        renderBoard();
        int turnPosition = currentReplayTurnPosition();
        int replayTurnNumber = turnPosition < 0 ? 1 : turnPosition + 1;
        int replayTurnCount = Math.max(1, replayTurnEntryIndices.size());
        setStatus(
            "Replay turn " + replayTurnNumber + "/" + replayTurnCount
                + " (entry " + (index + 1) + "/" + replayEntries.size() + "): "
                + entry.action()
        );
    }

    private void restoreFacingSnapshot(List<ReplayFacing> facingSnapshot) {
        Map<Position, Integer> facingByPosition = new HashMap<>();
        if (facingSnapshot != null) {
            for (ReplayFacing facing : facingSnapshot) {
                if (facing == null) {
                    continue;
                }
                facingByPosition.put(new Position(facing.row(), facing.col()), facing.facing());
            }
        }

        for (int row = 0; row < game.getHeight(); row++) {
            for (int col = 0; col < game.getWidth(); col++) {
                Position position = new Position(row, col);
                Unit unit = game.getUnitAt(position);
                if (unit == null) {
                    continue;
                }
                int facing = facingByPosition.getOrDefault(position, defaultFacingForOwner(unit.getOwner()));
                facingByUnit.put(unit, facing);
            }
        }
    }

    private void switchReplayToLive() {
        if (!replayMode) {
            return;
        }
        replayMode = false;
        gameOverAnnounced = false;
        commandHistory.clear();
        closeActiveMenu();
        clearSelection();
        resetReplayProtocol("Replay exit to live mode.");
        refreshReplayControls();
        setStatus("Switched to live mode. Previous protocol cleared.");
    }

    private void refreshReplayControls() {
        boolean gameOver = game != null && game.isGameOver();
        int turnPosition = currentReplayTurnPosition();
        boolean canReplayBackward = replayMode && turnPosition > 0;
        boolean canReplayForward = replayMode && turnPosition >= 0 && turnPosition < replayTurnEntryIndices.size() - 1;
        if (endTurnButton != null) {
            endTurnButton.setDisable(replayMode || gameOver);
        }
        if (botTurnButton != null) {
            botTurnButton.setDisable(replayMode || isBotVsBotRunning() || gameOver);
        }
        if (botVsBotStartButton != null) {
            botVsBotStartButton.setDisable(replayMode || isBotVsBotRunning() || gameOver);
        }
        if (botVsBotStopButton != null) {
            botVsBotStopButton.setDisable(replayMode || !isBotVsBotRunning());
        }
        if (mapOneButton != null) {
            mapOneButton.setDisable(isBotVsBotRunning());
        }
        if (mapTwoButton != null) {
            mapTwoButton.setDisable(isBotVsBotRunning());
        }
        if (replayBackButton != null) {
            replayBackButton.setDisable(!canReplayBackward);
        }
        if (replayForwardButton != null) {
            replayForwardButton.setDisable(!canReplayForward);
        }
        if (replayLiveButton != null) {
            replayLiveButton.setDisable(!replayMode);
        }
    }

    private void rebuildReplayTurnAnchors() {
        replayTurnEntryIndices.clear();
        String previousTurnKey = null;
        for (int index = 0; index < replayEntries.size(); index++) {
            ReplayEntry entry = replayEntries.get(index);
            String turnKey = replayTurnKey(entry, index);
            if (!turnKey.equals(previousTurnKey)) {
                replayTurnEntryIndices.add(index);
                previousTurnKey = turnKey;
            }
        }
    }

    private String replayTurnKey(ReplayEntry entry, int index) {
        if (entry == null || entry.state() == null || entry.state().turn() == null) {
            // Fallback for malformed/legacy logs: each entry acts as an anchor.
            return "ENTRY_" + index;
        }
        GameState.TurnState turn = entry.state().turn();
        return turn.turnNumber() + "|" + turn.currentPlayer();
    }

    private int currentReplayTurnPosition() {
        if (replayTurnEntryIndices.isEmpty()) {
            return -1;
        }
        int position = 0;
        while (position + 1 < replayTurnEntryIndices.size()
            && replayTurnEntryIndices.get(position + 1) <= replayIndex) {
            position++;
        }
        return position;
    }

    private <T> CommandResult<T> executeCommand(GameCommand<T> command) {
        return commandHistory.execute(game, command);
    }

    private boolean isAnyMenuActive() {
        return activeMenu != null && activeMenu.isShowing();
    }

    private boolean isActiveMenuForPosition(Position position) {
        if (!isAnyMenuActive() || activeMenuPosition == null || position == null) {
            return false;
        }
        return activeMenuPosition.equals(position);
    }

    private void closeActiveMenu() {
        if (activeMenu != null) {
            activeMenu.hide();
        }
        activeMenu = null;
        activeMenuPosition = null;
    }

    private void showManagedMenu(ContextMenu menu, Position position, double screenX, double screenY) {
        closeActiveMenu();
        menu.setOnHidden(event -> {
            if (activeMenu == menu) {
                activeMenu = null;
                activeMenuPosition = null;
            }
        });
        activeMenu = menu;
        activeMenuPosition = position;
        menu.show(cellViews.get(position), screenX, screenY);
    }

    private void clearSelection() {
        selectedUnit = null;
        reachableTiles = List.of();
    }

    private void setStatus(String message) {
        statusLabel.setText("Status: " + message);
    }

    private void appendLog(String message) {
        eventLog.appendText(message + System.lineSeparator());
    }

    private void toggleTerminalVisibility() {
        setTerminalVisible(!terminalVisible);
    }

    private void setTerminalVisible(boolean visible) {
        terminalVisible = visible;
        if (rootLayout != null) {
            rootLayout.setRight(visible ? terminalPanel : null);
        }
        if (terminalToggleButton != null) {
            terminalToggleButton.setText(visible ? "Hide Terminal" : "Show Terminal");
        }
        updateCellSize();
    }

    private String describeUnitAt(Position position) {
        return describeUnit(game.getUnitAt(position));
    }

    private String describeUnit(Unit unit) {
        if (unit == null) {
            return "Unknown";
        }
        return unit.getOwner() + " " + unit.getType().getDisplayName();
    }

    private String formatPosition(Position position) {
        return "(" + position.row() + "," + position.col() + ")";
    }

    private record ReplayFacing(
        int row,
        int col,
        int facing
    ) {
    }

    private record ReplayEntry(
        String action,
        GameState state,
        List<ReplayFacing> facings
    ) {
    }

    private record ReplayLogFile(
        List<ReplayEntry> entries
    ) {
    }

    private Game createFallbackDemoGame() {
        Game demo = GameFactory.createGame(new String[]{
            "H P P P P P P P P P P H",
            "P P P P P P P P P P P P",
            "P T F P P P P P P F T P",
            "P P P P M P P M P P P P",
            "P P P P P P P P P P P P",
            "P C P W P P P P W P C P",
            "P P P P P P P P P P P P",
            "P P P P M P P M P P P P",
            "P T F P P P P P P F T P",
            "P P P P P P P P P P P P",
            "P P P P P P P P P P P P",
            "H P P P P P P P P P P H"
        });

        demo.getTileAt(0, 0).setOwner("P1");
        demo.getTileAt(11, 0).setOwner("P1");
        demo.getTileAt(0, 11).setOwner("P2");
        demo.getTileAt(11, 11).setOwner("P2");
        demo.getTileAt(2, 1).setOwner("P1");
        demo.getTileAt(8, 1).setOwner("P1");
        demo.getTileAt(2, 10).setOwner("P2");
        demo.getTileAt(8, 10).setOwner("P2");
        demo.getTileAt(5, 1).setOwner("P1");
        demo.getTileAt(5, 10).setOwner("P2");

        // Left side: red infantry, default orientation (not flipped).
        for (int row = 1; row <= 10; row++) {
            Unit redInfantry = demo.createUnit("Infantry", "P1", row, 0);
            facingByUnit.put(redInfantry, 1);
        }

        // Right side: blue infantry, tanks, and artillery, initially flipped horizontally.
        for (int row = 1; row <= 4; row++) {
            Unit blueInfantry = demo.createUnit("Infantry", "P2", row, 11);
            facingByUnit.put(blueInfantry, -1);
        }
        for (int row = 5; row <= 7; row++) {
            Unit blueTank = demo.createUnit("Tank", "P2", row, 11);
            facingByUnit.put(blueTank, -1);
        }
        for (int row = 8; row <= 10; row++) {
            Unit blueArtillery = demo.createUnit("Artillery", "P2", row, 11);
            facingByUnit.put(blueArtillery, -1);
        }
        return demo;
    }
}
