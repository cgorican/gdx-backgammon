package si.um.feri.backgammon.view;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import si.um.feri.backgammon.model.Board;
import si.um.feri.backgammon.model.Move;
import si.um.feri.backgammon.model.Side;

/**
 * Geometry of the board in world units (1920x1080, origin bottom-left), matching the background art:
 * <pre>
 *  | tray 270 | 6 points x 100 | bar 180 | 6 points x 100 | tray 270 |
 * </pre>
 * Points 12..23 run left to right along the top, points 0..11 run right to left along the bottom.
 * Pure math, no rendering, so it is unit-testable.
 */
public final class BoardLayout {
    public static final float WORLD_WIDTH = 1920f;
    public static final float WORLD_HEIGHT = 1080f;
    public static final float TRAY_WIDTH = 270f;
    public static final float POINT_WIDTH = 100f;
    public static final float POINT_HEIGHT = 500f;
    public static final float BAR_WIDTH = 180f;
    public static final float CHECKER_SIZE = 100f;
    public static final float OFF_CHECKER_HEIGHT = 25f;

    /** Pseudo-location returned by {@link #locate} when nothing relevant is under the pointer. */
    public static final int NONE = Integer.MIN_VALUE;

    private static final float HALF_WIDTH = 6 * POINT_WIDTH;
    private static final float LEFT_HALF_X = TRAY_WIDTH;
    private static final float BAR_X = TRAY_WIDTH + HALF_WIDTH;
    private static final float RIGHT_HALF_X = BAR_X + BAR_WIDTH;
    private static final float RIGHT_TRAY_X = RIGHT_HALF_X + HALF_WIDTH;
    private static final float BAR_MARGIN = 40f;
    private static final float BAR_STACK_EXTENT = 380f;
    private static final float OFF_X = RIGHT_TRAY_X + (TRAY_WIDTH - CHECKER_SIZE) / 2f;
    private static final float OFF_MARGIN = 60f;

    private BoardLayout() {
    }

    public static boolean isTopRow(int point) {
        return point >= 12;
    }

    /** Left edge of a point's column. */
    public static float pointX(int point) {
        if (point < 0 || point >= Board.POINTS) throw new IllegalArgumentException("Not a point: " + point);
        if (point < 12) {
            float x = RIGHT_TRAY_X - (point + 1) * POINT_WIDTH;
            return point >= 6 ? x - BAR_WIDTH : x;
        }
        int column = point - 12;
        float x = LEFT_HALF_X + column * POINT_WIDTH;
        return column >= 6 ? x + BAR_WIDTH : x;
    }

    /** The clickable column of a point: the whole triangle. */
    public static Rectangle pointBounds(int point) {
        float y = isTopRow(point) ? WORLD_HEIGHT - POINT_HEIGHT : 0f;
        return new Rectangle(pointX(point), y, POINT_WIDTH, POINT_HEIGHT);
    }

    /** Distance between stacked checkers: they overlap when more than 5 are on a point. */
    public static float stackSpacing(int total) {
        return total <= 5 ? CHECKER_SIZE : (POINT_HEIGHT - CHECKER_SIZE) / (total - 1);
    }

    /** Bottom-left corner of the {@code index}-th checker (0 = at the board edge) of a stack of {@code total}. */
    public static Vector2 checkerPosition(int point, int index, int total) {
        float offset = index * stackSpacing(Math.max(total, index + 1));
        float y = isTopRow(point) ? WORLD_HEIGHT - CHECKER_SIZE - offset : offset;
        return new Vector2(pointX(point), y);
    }

    /** Bar checkers: bright in the upper half, dark in the lower half, stacking towards the middle. */
    public static Vector2 barPosition(Side side, int index, int total) {
        float spacing = total <= 4 ? CHECKER_SIZE : Math.min(CHECKER_SIZE, (BAR_STACK_EXTENT - CHECKER_SIZE) / (total - 1));
        float x = BAR_X + (BAR_WIDTH - CHECKER_SIZE) / 2f;
        float y = side == Side.BRIGHT
                ? WORLD_HEIGHT - BAR_MARGIN - CHECKER_SIZE - index * spacing
                : BAR_MARGIN + index * spacing;
        return new Vector2(x, y);
    }

    public static Rectangle barBounds(Side side) {
        float y = side == Side.BRIGHT ? WORLD_HEIGHT / 2f : 0f;
        return new Rectangle(BAR_X, y, BAR_WIDTH, WORLD_HEIGHT / 2f);
    }

    /** Borne-off checkers (drawn edge-on) in the right tray: bright top slot, dark bottom slot. */
    public static Vector2 offPosition(Side side, int index) {
        float y = side == Side.BRIGHT
                ? WORLD_HEIGHT - OFF_MARGIN - (index + 1) * OFF_CHECKER_HEIGHT
                : OFF_MARGIN + index * OFF_CHECKER_HEIGHT;
        return new Vector2(OFF_X, y);
    }

    /** The bear-off target area of a side (its half of the right tray). */
    public static Rectangle offBounds(Side side) {
        float height = Board.CHECKERS_PER_SIDE * OFF_CHECKER_HEIGHT + 40f;
        float y = side == Side.BRIGHT ? WORLD_HEIGHT - OFF_MARGIN - height + 20f : OFF_MARGIN - 20f;
        return new Rectangle(RIGHT_TRAY_X + 40f, y, TRAY_WIDTH - 80f, height);
    }

    /** Centre of the board half where {@code side} rolls its dice (bright left, dark right). */
    public static Vector2 diceCenter(Side side) {
        float x = side == Side.BRIGHT ? LEFT_HALF_X + HALF_WIDTH / 2f : RIGHT_HALF_X + HALF_WIDTH / 2f;
        return new Vector2(x, WORLD_HEIGHT / 2f);
    }

    /** Left tray, where the player panels and menu button live. */
    public static Rectangle leftTray() {
        return new Rectangle(0, 0, TRAY_WIDTH, WORLD_HEIGHT);
    }

    /**
     * What is under a world coordinate, from the point of view of {@code side}: a point index, {@link Move#BAR}
     * (the side's half of the bar), {@link Move#OFF} (the side's bear-off tray) or {@link #NONE}.
     */
    public static int locate(float x, float y, Side side) {
        for (int point = 0; point < Board.POINTS; point++) {
            if (pointBounds(point).contains(x, y)) return point;
        }
        if (barBounds(side).contains(x, y)) return Move.BAR;
        if (offBounds(side).contains(x, y)) return Move.OFF;
        return NONE;
    }
}
