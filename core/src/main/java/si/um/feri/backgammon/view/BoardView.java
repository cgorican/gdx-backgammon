package si.um.feri.backgammon.view;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import si.um.feri.backgammon.assets.RegionNames;
import si.um.feri.backgammon.model.Board;
import si.um.feri.backgammon.model.Destination;
import si.um.feri.backgammon.model.Move;
import si.um.feri.backgammon.model.Side;

/**
 * Draws the board, the checkers and the move highlights, animates moves, and reports clicks as board locations
 * (point index, {@link Move#BAR}, {@link Move#OFF} or {@link BoardLayout#NONE}). Holds no game state of its own:
 * call {@link #sync(Board)} after the model changes.
 */
public class BoardView extends Group {

    /** Receives clicks on the board. */
    public interface Listener {
        void locationClicked(int location);

        /** Right click: cancel the current selection. */
        void cancelSelection();
    }

    private static final float RING_SIZE = 124f;
    private static final float GLOW_SIZE = 180f;
    private static final float BRANDING_Y = 500f;

    private final TextureAtlas atlas;
    private final Skin skin;
    private final Group pointGlows = layer();
    private final Group underlay = layer();
    private final Group checkers = layer();
    private final Group markers = layer();
    private final Group labels = layer();
    private final Group flying = layer();

    private final List<List<Image>> pointActors = new ArrayList<>();
    private final Map<Side, List<Image>> barActors = new EnumMap<>(Side.class);
    private final Map<Side, List<Image>> offActors = new EnumMap<>(Side.class);

    /** The side whose half of the bar and bear-off tray clicks refer to. */
    private Side perspective = Side.BRIGHT;

    public BoardView(TextureAtlas atlas, Skin skin, Listener listener) {
        this.atlas = atlas;
        this.skin = skin;
        setSize(BoardLayout.WORLD_WIDTH, BoardLayout.WORLD_HEIGHT);
        setTouchable(Touchable.enabled);
        for (int i = 0; i < Board.POINTS; i++) pointActors.add(new ArrayList<>());
        for (Side side : Side.values()) {
            barActors.put(side, new ArrayList<>());
            offActors.put(side, new ArrayList<>());
        }

        addArt();
        addActor(pointGlows);
        addActor(underlay);
        addActor(checkers);
        addActor(labels);
        addActor(markers);
        addActor(flying);

        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) listener.cancelSelection();
                else listener.locationClicked(BoardLayout.locate(x, y, perspective));
                return true;
            }
        });
    }

    private static Group layer() {
        Group group = new Group();
        group.setTouchable(Touchable.disabled);
        return group;
    }

    private void addArt() {
        Group art = layer();
        art.addActor(image(RegionNames.BACKGROUND, 0, 0));
        art.addActor(image("storage", 0, 0));
        art.addActor(image("storage", BoardLayout.WORLD_WIDTH - BoardLayout.TRAY_WIDTH, 0));
        art.addActor(image("bar", BoardLayout.TRAY_WIDTH + 6 * BoardLayout.POINT_WIDTH, 0));
        art.addActor(image("branding", BoardLayout.TRAY_WIDTH, BRANDING_Y));
        art.addActor(image("branding", BoardLayout.TRAY_WIDTH + 6 * BoardLayout.POINT_WIDTH + BoardLayout.BAR_WIDTH, BRANDING_Y));
        for (int point = 0; point < Board.POINTS; point++) {
            Rectangle bounds = BoardLayout.pointBounds(point);
            Image spike = image(isDarkPoint(point) ? "spike-dark" : "spike-bright", bounds.x, bounds.y);
            if (BoardLayout.isTopRow(point)) {
                spike.setOrigin(Align.center);
                spike.setRotation(180);
            }
            art.addActor(spike);
        }
        addActor(art);
    }

    /** Alternating point colours, as in the original board. */
    private static boolean isDarkPoint(int point) {
        float x = BoardLayout.pointX(point);
        float quadrantX = x >= BoardLayout.WORLD_WIDTH / 2f ? BoardLayout.WORLD_WIDTH / 2f + BoardLayout.BAR_WIDTH / 2f
                : BoardLayout.TRAY_WIDTH;
        int column = Math.round((x - quadrantX) / BoardLayout.POINT_WIDTH);
        return BoardLayout.isTopRow(point) ? column % 2 == 0 : column % 2 == 1;
    }

    private Image image(String region, float x, float y) {
        TextureRegion textureRegion = atlas.findRegion(region);
        if (textureRegion == null) textureRegion = skin.getRegion(region);
        Image image = new Image(textureRegion);
        image.setPosition(x, y);
        return image;
    }

    public void setPerspective(Side side) {
        perspective = side;
    }

    // ------------------------------------------------------------------------------------------------ checkers

    /** Rebuilds all checker actors from the model. */
    public void sync(Board board) {
        checkers.clearChildren();
        for (List<Image> stack : pointActors) stack.clear();
        for (Side side : Side.values()) {
            barActors.get(side).clear();
            offActors.get(side).clear();
        }

        for (int point = 0; point < Board.POINTS; point++) {
            Side owner = board.owner(point);
            if (owner == null) continue;
            int total = board.height(point);
            for (int i = 0; i < total; i++) {
                pointActors.get(point).add(checker(owner, BoardLayout.checkerPosition(point, i, total)));
            }
        }
        for (Side side : Side.values()) {
            int onBar = board.barCount(side);
            for (int i = 0; i < onBar; i++) barActors.get(side).add(checker(side, BoardLayout.barPosition(side, i, onBar)));
            for (int i = 0; i < board.offCount(side); i++) {
                Vector2 p = BoardLayout.offPosition(side, i);
                Image edge = image(RegionNames.checkerEdge(side), p.x, p.y);
                checkers.addActor(edge);
                offActors.get(side).add(edge);
            }
        }
    }

    private Image checker(Side side, Vector2 position) {
        Image image = image(RegionNames.checker(side), position.x, position.y);
        image.setOrigin(Align.center);
        checkers.addActor(image);
        return image;
    }

    /** Where the top checker of {@code side} at {@code location} (a point or the bar) is drawn. */
    private static Vector2 topPosition(Board board, int location, Side side) {
        if (location == Move.BAR) {
            int n = board.barCount(side);
            return BoardLayout.barPosition(side, Math.max(n - 1, 0), Math.max(n, 1));
        }
        int n = board.height(location);
        return BoardLayout.checkerPosition(location, Math.max(n - 1, 0), Math.max(n, 1));
    }

    private Image topActor(int location, Side side) {
        List<Image> list;
        if (location == Move.BAR) list = barActors.get(side);
        else if (location == Move.OFF) list = offActors.get(side);
        else list = pointActors.get(location);
        return list.isEmpty() ? null : list.get(list.size() - 1);
    }

    /**
     * Animates one move that has already been applied to the model: {@code before} is the board before it,
     * {@code after} the board now. A hit blot first stays in place, then flies to the bar.
     */
    public void animateMove(Board before, Board after, Move move, Side side, boolean hit, Runnable done) {
        Vector2 from = topPosition(before, move.from(), side);
        sync(after);

        Image destination = topActor(move.to(), side);
        Image hitOnBar = hit ? topActor(Move.BAR, side.opponent()) : null;
        if (destination != null) destination.setVisible(false);
        if (hitOnBar != null) hitOnBar.setVisible(false);

        Image blot = null;
        if (hit) {
            Vector2 blotAt = BoardLayout.checkerPosition(move.to(), 0, 1);
            blot = image(RegionNames.checker(side.opponent()), blotAt.x, blotAt.y);
            flying.addActor(blot);
        }

        Vector2 to = move.to() == Move.OFF
                ? BoardLayout.offPosition(side, after.offCount(side) - 1).add(0, -BoardLayout.CHECKER_SIZE / 2f + 12f)
                : new Vector2(destination.getX(), destination.getY());
        Image mover = image(RegionNames.checker(side), from.x, from.y);
        flying.addActor(mover); // above the blot
        float distance = from.dst(to);
        float duration = Math.min(0.42f, 0.16f + distance / 4200f);

        final Image blotActor = blot;
        mover.addAction(Actions.sequence(
                Actions.moveTo(to.x, to.y, duration, Interpolation.pow2Out),
                Actions.run(() -> {
                    mover.remove();
                    if (destination != null) destination.setVisible(true);
                    if (blotActor == null) {
                        done.run();
                        return;
                    }
                    Vector2 bar = new Vector2(hitOnBar.getX(), hitOnBar.getY());
                    blotActor.addAction(Actions.sequence(
                            Actions.moveTo(bar.x, bar.y, 0.3f, Interpolation.pow2Out),
                            Actions.run(() -> {
                                blotActor.remove();
                                hitOnBar.setVisible(true);
                                done.run();
                            })));
                })));
    }

    // ------------------------------------------------------------------------------------------------ highlights

    public void clearHighlights() {
        pointGlows.clearChildren();
        underlay.clearChildren();
        markers.clearChildren();
    }

    /** Soft pulsing rings on every checker that can move. */
    public void showMovable(Board board, Side side, Collection<Integer> sources) {
        clearHighlights();
        for (int source : sources) {
            Image ring = ring(topPosition(board, source, side), Ui.GOLD);
            ring.getColor().a = 0.35f;
            ring.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.85f, 0.7f, Interpolation.sine),
                    Actions.alpha(0.3f, 0.7f, Interpolation.sine))));
            markers.addActor(ring);
        }
    }

    /** The selected checker and everywhere it can go: single-die moves in green, combinations in blue, hits in red. */
    public void showSelection(Board board, Side side, int source, List<Destination> destinations) {
        clearHighlights();
        Vector2 at = topPosition(board, source, side);

        Image glow = new Image(skin.getRegion(RegionNames.GLOW));
        glow.setColor(Ui.GOLD);
        glow.setPosition(at.x + BoardLayout.CHECKER_SIZE / 2f - GLOW_SIZE / 2f, at.y + BoardLayout.CHECKER_SIZE / 2f - GLOW_SIZE / 2f);
        underlay.addActor(glow);
        markers.addActor(ring(at, Ui.GOLD));

        for (Destination destination : destinations) {
            boolean hit = destination.to() != Move.OFF && board.count(destination.to(), side.opponent()) == 1;
            Color color = hit ? Ui.HIT : destination.isCombination() ? Ui.COMBINED_MOVE : Ui.SINGLE_MOVE;
            if (destination.to() == Move.OFF) showOffTarget(board, side, destination, color);
            else showPointTarget(board, side, destination, color, hit);
        }
    }

    private void showPointTarget(Board board, Side side, Destination destination, Color color, boolean hit) {
        int point = destination.to();
        Rectangle bounds = BoardLayout.pointBounds(point);
        Image triangle = new Image(skin.getRegion(RegionNames.POINT_GLOW));
        triangle.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
        triangle.setColor(color.r, color.g, color.b, 0.55f);
        if (BoardLayout.isTopRow(point)) {
            triangle.setOrigin(Align.center);
            triangle.setRotation(180);
        }
        triangle.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.7f, 0.6f, Interpolation.sine),
                Actions.alpha(0.4f, 0.6f, Interpolation.sine))));
        pointGlows.addActor(triangle);

        int own = board.count(point, side);
        int index = hit ? 0 : own;
        Vector2 landing = BoardLayout.checkerPosition(point, index, index + 1);
        Image ghost = image(RegionNames.checker(side), landing.x, landing.y);
        ghost.getColor().a = 0.5f;
        markers.addActor(ghost);
        markers.addActor(ring(landing, color));
        markers.addActor(badge(destination.diceLabel(), color,
                landing.x + BoardLayout.CHECKER_SIZE / 2f, landing.y + BoardLayout.CHECKER_SIZE / 2f));
    }

    private void showOffTarget(Board board, Side side, Destination destination, Color color) {
        Rectangle bounds = BoardLayout.offBounds(side);
        Image tray = new Image(skin.getPatch(RegionNames.GLOW_RECT));
        tray.setBounds(bounds.x - 20, bounds.y - 20, bounds.width + 40, bounds.height + 40);
        tray.setColor(color.r, color.g, color.b, 0.6f);
        tray.addAction(Actions.forever(Actions.sequence(Actions.alpha(0.75f, 0.6f, Interpolation.sine),
                Actions.alpha(0.4f, 0.6f, Interpolation.sine))));
        pointGlows.addActor(tray);

        Vector2 slot = BoardLayout.offPosition(side, board.offCount(side));
        Image ghost = image(RegionNames.checkerEdge(side), slot.x, slot.y);
        ghost.getColor().a = 0.6f;
        markers.addActor(ghost);
        float labelY = side == Side.BRIGHT ? bounds.y + 40 : bounds.y + bounds.height - 40;
        markers.addActor(badge("Off  " + destination.diceLabel(), color, bounds.x + bounds.width / 2f, labelY));
    }

    private Image ring(Vector2 checkerPosition, Color color) {
        Image ring = new Image(skin.getRegion(RegionNames.RING));
        ring.setColor(color);
        float offset = (RING_SIZE - BoardLayout.CHECKER_SIZE) / 2f;
        ring.setPosition(checkerPosition.x - offset, checkerPosition.y - offset);
        return ring;
    }

    private Actor badge(String text, Color color, float centerX, float centerY) {
        Table badge = new Table();
        badge.setBackground(skin.newDrawable(RegionNames.PILL, color));
        badge.add(new Label(text, skin, "ink"));
        badge.pack();
        badge.setPosition(centerX - badge.getWidth() / 2f, centerY - badge.getHeight() / 2f);
        badge.setTouchable(Touchable.disabled);
        return badge;
    }

    // ------------------------------------------------------------------------------------------------ point numbers

    /** Shows point numbers (1..24) as seen by {@code side}, or hides them when {@code side} is null. */
    public void showPointNumbers(Side side) {
        labels.clearChildren();
        if (side == null) return;
        for (int point = 0; point < Board.POINTS; point++) {
            Label label = new Label(String.valueOf(side.pipsToBearOff(point)), skin, "small-cream");
            label.setFontScale(0.8f);
            label.setAlignment(Align.center);
            label.setSize(BoardLayout.POINT_WIDTH, 34);
            float y = BoardLayout.isTopRow(point) ? 544 : 502;
            label.setPosition(BoardLayout.pointX(point), y);
            labels.addActor(label);
        }
    }
}
