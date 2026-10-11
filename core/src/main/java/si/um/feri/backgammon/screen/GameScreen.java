package si.um.feri.backgammon.screen;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import si.um.feri.backgammon.BackgammonGame;
import si.um.feri.backgammon.assets.RegionNames;
import si.um.feri.backgammon.audio.GameAudio;
import si.um.feri.backgammon.model.Board;
import si.um.feri.backgammon.model.Destination;
import si.um.feri.backgammon.model.DiceRoll;
import si.um.feri.backgammon.model.DiceRoller;
import si.um.feri.backgammon.model.Game;
import si.um.feri.backgammon.model.Move;
import si.um.feri.backgammon.model.Side;
import si.um.feri.backgammon.persistence.Settings;
import si.um.feri.backgammon.view.BoardLayout;
import si.um.feri.backgammon.view.BoardView;
import si.um.feri.backgammon.view.DiceTray;
import si.um.feri.backgammon.view.PlayerPanel;
import si.um.feri.backgammon.view.Ui;

/**
 * A local two-player game. Interaction: roll, tap a checker (checkers on the bar are selected automatically), then
 * tap one of the highlighted destinations. Single-die moves are green, moves combining dice are blue, hits are red.
 */
public class GameScreen extends BaseScreen implements BoardView.Listener {
    private static final float TURN_END_DELAY = 0.6f;
    private static final float NO_MOVES_DELAY = 1.8f;
    private static final float ROLL_BUTTON_WIDTH = 280f;

    private final Settings settings;
    private final GameAudio audio;
    private final Map<Side, String> names = new EnumMap<>(Side.class);
    private final Map<Side, PlayerPanel> panels = new EnumMap<>(Side.class);
    private Game model;

    private BoardView boardView;
    private DiceTray diceTray;
    private TextButton rollButton;
    private Table toast;
    private Dialog leaveDialog;

    /** The selected checker's location (point index or {@link Move#BAR}), or null. */
    private Integer selected;
    /** True while a move animates or the turn is being handed over; board input is ignored. */
    private boolean busy;

    public GameScreen(BackgammonGame game) {
        this(game, null);
    }

    /** Starts from the given game state (e.g. a prepared position for testing); null starts a new game. */
    public GameScreen(BackgammonGame game, Game model) {
        super(game);
        settings = game.getSettings();
        audio = game.getAudio();
        this.model = model;
        for (Side side : Side.values()) names.put(side, settings.playerName(side));
    }

    @Override
    protected void build() {
        if (model == null) model = new Game(settings.initialSide(), DiceRoller.random(new Random()));

        boardView = new BoardView(gameplayAtlas, skin, this);
        stage.addActor(boardView);

        for (Side side : Side.values()) {
            PlayerPanel panel = new PlayerPanel(gameplayAtlas, skin, side, names.get(side));
            // Each player's card sits on the same side as their home board: bright at the top, dark at the bottom.
            float y = side == Side.BRIGHT ? BoardLayout.WORLD_HEIGHT - 24 - PlayerPanel.HEIGHT : 24;
            panel.setPosition((BoardLayout.TRAY_WIDTH - PlayerPanel.WIDTH) / 2f, y);
            panels.put(side, panel);
            stage.addActor(panel);
        }

        TextButton menuButton = Ui.secondaryButton("Menu", skin, this::onBack);
        menuButton.setSize(PlayerPanel.WIDTH, 76);
        menuButton.setPosition((BoardLayout.TRAY_WIDTH - PlayerPanel.WIDTH) / 2f, BoardLayout.WORLD_HEIGHT / 2f - 38);
        stage.addActor(menuButton);

        diceTray = new DiceTray(gameplayAtlas, skin);
        diceTray.hide();
        stage.addActor(diceTray);

        rollButton = Ui.button("Roll dice", skin, this::roll);
        rollButton.setSize(ROLL_BUTTON_WIDTH, Ui.BUTTON_HEIGHT);
        rollButton.setTransform(true);
        rollButton.setOrigin(Align.center);
        rollButton.addAction(Actions.forever(Actions.sequence(
                Actions.scaleTo(1.06f, 1.06f, 0.6f, Interpolation.sine),
                Actions.scaleTo(1f, 1f, 0.6f, Interpolation.sine))));
        stage.addActor(rollButton);

        toast = new Table();
        toast.setBackground(skin.getDrawable(RegionNames.PANEL_ACTIVE));
        toast.setTouchable(Touchable.disabled);
        toast.getColor().a = 0f;
        stage.addActor(toast);

        boardView.sync(model.board());
        showToast(names.get(model.currentSide()) + " starts", 1.4f);
        refresh();
    }

    // ------------------------------------------------------------------------------------------------ turn flow

    private void roll() {
        if (busy || model.phase() != Game.Phase.AWAITING_ROLL || isDialogOpen()) return;
        audio.dice();
        DiceRoll roll = model.roll();
        diceTray.show(roll, model.currentSide(), true);
        if (!model.hasLegalMoves()) {
            busy = true;
            showToast("No legal moves - " + names.get(model.currentSide()) + " passes", NO_MOVES_DELAY);
            later(NO_MOVES_DELAY, this::endTurn);
        } else {
            autoSelect();
        }
        refresh();
    }

    /** Checkers on the bar must enter first, so they are selected right away; so is a lone movable checker. */
    private void autoSelect() {
        Set<Integer> sources = model.movableSources();
        selected = sources.size() == 1 ? sources.iterator().next() : null;
    }

    private boolean selectionForced() {
        return selected != null && model.movableSources().size() == 1;
    }

    @Override
    public void locationClicked(int location) {
        if (busy || model.phase() != Game.Phase.MOVING || isDialogOpen()) return;

        if (selected != null) {
            for (Destination destination : model.destinationsFrom(selected)) {
                if (destination.to() == location) {
                    play(destination);
                    return;
                }
            }
        }
        if (model.movableSources().contains(location)) {
            selected = selected != null && selected == location && !selectionForced() ? null : location;
        } else if (!selectionForced()) {
            selected = null;
        }
        refresh();
    }

    @Override
    public void cancelSelection() {
        if (busy || selectionForced()) return;
        selected = null;
        refresh();
    }

    private void play(Destination destination) {
        busy = true;
        selected = null;
        boardView.clearHighlights();
        playStep(destination.path(), 0);
    }

    /** Plays and animates the moves of a path one after another. */
    private void playStep(List<Move> path, int index) {
        if (index == path.size()) {
            busy = false;
            afterMove();
            return;
        }
        Move move = path.get(index);
        Side side = model.currentSide();
        Board before = model.board().copy();
        boolean hit = model.play(move);
        audio.checker();
        diceTray.refresh();
        updatePanels();
        boardView.animateMove(before, model.board(), move, side, hit, () -> playStep(path, index + 1));
    }

    private void afterMove() {
        if (model.isOver()) {
            gameOver();
        } else if (model.isTurnOver()) {
            busy = true;
            refresh();
            later(TURN_END_DELAY, this::endTurn);
        } else {
            autoSelect();
            refresh();
        }
    }

    private void endTurn() {
        model.endTurn();
        busy = false;
        selected = null;
        diceTray.hide();
        showToast(names.get(model.currentSide()) + "'s turn", 1.1f);
        refresh();
    }

    private void gameOver() {
        Side winner = model.winner();
        Game.WinType type = model.winType();
        game.getLeaderboard().recordWin(names.get(winner), type.points);
        diceTray.hide();
        refresh();

        Table overlay = new Table();
        overlay.setFillParent(true);
        overlay.setBackground(skin.newDrawable(RegionNames.WHITE, 0f, 0f, 0f, 0.55f));
        Table card = new Table();
        card.setBackground(skin.getDrawable(RegionNames.PANEL_ACTIVE));
        card.pad(48, 72, 52, 72);
        card.add(new Label(names.get(winner) + " wins!", skin, "title")).colspan(2).padBottom(12).row();
        String detail = switch (type) {
            case SINGLE -> "Single game - 1 point";
            case GAMMON -> "Gammon! - 2 points";
            case BACKGAMMON -> "Backgammon!! - 3 points";
        };
        card.add(new Label(detail, skin)).colspan(2).padBottom(40).row();
        card.add(Ui.button("Rematch", skin, () -> goTo(new GameScreen(game)))).width(300).height(Ui.BUTTON_HEIGHT).padRight(24);
        card.add(Ui.secondaryButton("Menu", skin, () -> goTo(new MenuScreen(game)))).width(300).height(Ui.BUTTON_HEIGHT);
        overlay.add(card);
        overlay.getColor().a = 0f;
        overlay.addAction(Actions.fadeIn(0.4f));
        stage.addActor(overlay);
    }

    // ------------------------------------------------------------------------------------------------ view

    private void refresh() {
        Side current = model.currentSide();
        boardView.setPerspective(current);
        boardView.showPointNumbers(settings.showPointNumbers() ? current : null);

        boolean canRoll = !busy && model.phase() == Game.Phase.AWAITING_ROLL;
        rollButton.setVisible(canRoll);
        if (canRoll) {
            Vector2 center = BoardLayout.diceCenter(current);
            rollButton.setPosition(center.x - ROLL_BUTTON_WIDTH / 2f, center.y - Ui.BUTTON_HEIGHT / 2f);
        }

        boardView.clearHighlights();
        if (!busy && model.hasLegalMoves()) {
            if (selected != null) {
                boardView.showSelection(model.board(), current, selected, model.destinationsFrom(selected));
            } else if (settings.moveHints()) {
                boardView.showMovable(model.board(), current, model.movableSources());
            }
        }
        updatePanels();
    }

    private void updatePanels() {
        for (Side side : Side.values()) {
            boolean winner = model.isOver() && side == model.winner();
            boolean active = winner || (!model.isOver() && side == model.currentSide());
            String badge = winner ? "WINNER" : active ? "YOUR TURN" : "WAITING";
            panels.get(side).update(model.board(), side, active, badge, statusFor(side));
        }
    }

    private String statusFor(Side side) {
        if (model.isOver()) return side == model.winner() ? "Well played!" : "";
        if (side != model.currentSide()) return "";
        if (model.phase() == Game.Phase.AWAITING_ROLL) return "Roll the dice";
        if (!model.hasLegalMoves()) return model.dice().allUsed() ? "Turn complete" : "No legal moves";
        if (selected != null && selected == Move.BAR) return "Enter from the bar";
        if (selected != null) return "Tap a lit point";
        return "Pick a checker";
    }

    private void showToast(String text, float seconds) {
        toast.clearChildren();
        toast.clearActions();
        toast.add(new Label(text, skin, "bold")).pad(18, 40, 18, 40);
        toast.pack();
        toast.setPosition(BoardLayout.WORLD_WIDTH / 2f - toast.getWidth() / 2f,
                BoardLayout.WORLD_HEIGHT / 2f - toast.getHeight() / 2f);
        toast.addAction(Actions.sequence(Actions.fadeIn(0.15f), Actions.delay(seconds), Actions.fadeOut(0.35f)));
        toast.toFront();
    }

    private void later(float seconds, Runnable action) {
        stage.addAction(Actions.delay(seconds, Actions.run(action)));
    }

    // ------------------------------------------------------------------------------------------------ keys & menu

    @Override
    protected boolean onKey(int keycode) {
        if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
            roll();
            return true;
        }
        return false;
    }

    @Override
    protected void onBack() {
        if (model.isOver()) {
            goTo(new MenuScreen(game));
        } else if (isDialogOpen()) {
            leaveDialog.hide();
            leaveDialog = null;
        } else if (selected != null && !selectionForced()) {
            cancelSelection();
        } else {
            showLeaveDialog();
        }
    }

    private boolean isDialogOpen() {
        return leaveDialog != null && leaveDialog.getStage() != null;
    }

    private void showLeaveDialog() {
        leaveDialog = new Dialog("", skin, "dialog") {
            @Override
            protected void result(Object leave) {
                if (Boolean.TRUE.equals(leave)) goTo(new MenuScreen(game));
                leaveDialog = null;
            }
        };
        leaveDialog.pad(40, 56, 40, 56);
        leaveDialog.getContentTable().add(new Label("Leave this game?", skin, "title")).padBottom(8).row();
        leaveDialog.getContentTable().add(new Label("The current game will not be saved.", skin)).padBottom(28);
        leaveDialog.getButtonTable().defaults().width(320).height(Ui.BUTTON_HEIGHT).pad(0, 12, 0, 12);
        leaveDialog.button(new TextButton("Keep playing", skin), false);
        leaveDialog.button(new TextButton("Leave", skin, "secondary"), true);
        // The dialog takes keyboard focus, so it handles Escape / back itself.
        leaveDialog.key(Input.Keys.ESCAPE, false);
        leaveDialog.key(Input.Keys.BACK, false);
        leaveDialog.show(stage);
    }
}
