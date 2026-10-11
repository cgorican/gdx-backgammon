package si.um.feri.backgammon.model;

import java.util.Arrays;

/**
 * Checker positions. Pure data plus the mechanics of applying a move; legality lives in {@link Rules}.
 * Point counts are signed: positive values are {@link Side#BRIGHT} checkers, negative values {@link Side#DARK}.
 */
public final class Board {
    public static final int POINTS = 24;
    public static final int HOME_POINTS = 6;
    public static final int CHECKERS_PER_SIDE = 15;

    private final int[] points = new int[POINTS];
    private final int[] bar = new int[2];
    private final int[] off = new int[2];

    private Board() {
    }

    public static Board empty() {
        return new Board();
    }

    /** The standard starting position (for each side: 2 on its 24-point, 5 on 13, 3 on 8, 5 on 6). */
    public static Board standard() {
        Board board = new Board();
        for (Side side : Side.values()) {
            board.set(side, toPoint(side, 24), 2);
            board.set(side, toPoint(side, 13), 5);
            board.set(side, toPoint(side, 8), 3);
            board.set(side, toPoint(side, 6), 5);
        }
        return board;
    }

    /** Converts a point number as seen by {@code side} (1..24, where 1 is the last point of its home) to an index. */
    public static int toPoint(Side side, int pointNumber) {
        return side == Side.BRIGHT ? POINTS - pointNumber : pointNumber - 1;
    }

    public Board copy() {
        Board copy = new Board();
        System.arraycopy(points, 0, copy.points, 0, POINTS);
        System.arraycopy(bar, 0, copy.bar, 0, 2);
        System.arraycopy(off, 0, copy.off, 0, 2);
        return copy;
    }

    /** Number of {@code side}'s checkers on {@code point}. */
    public int count(int point, Side side) {
        int value = points[point];
        if (side == Side.BRIGHT) return Math.max(value, 0);
        return Math.max(-value, 0);
    }

    /** Total number of checkers on {@code point}, of whichever side. */
    public int height(int point) {
        return Math.abs(points[point]);
    }

    /** The side occupying {@code point}, or {@code null} when it is empty. */
    public Side owner(int point) {
        if (points[point] > 0) return Side.BRIGHT;
        if (points[point] < 0) return Side.DARK;
        return null;
    }

    public int barCount(Side side) {
        return bar[side.ordinal()];
    }

    public int offCount(Side side) {
        return off[side.ordinal()];
    }

    /** True if {@code side} may land on {@code point}: it is empty, its own, or a single opposing blot. */
    public boolean isOpenFor(int point, Side side) {
        return count(point, side.opponent()) <= 1;
    }

    /** True when every checker of {@code side} that is still in play is inside its home board. */
    public boolean allInHome(Side side) {
        if (barCount(side) > 0) return false;
        for (int point = 0; point < POINTS; point++) {
            if (count(point, side) > 0 && !side.isHomePoint(point)) return false;
        }
        return true;
    }

    /** True if {@code side} has a checker farther from bearing off than {@code point}. */
    public boolean hasCheckerBehind(int point, Side side) {
        int distance = side.pipsToBearOff(point);
        for (int p = 0; p < POINTS; p++) {
            if (count(p, side) > 0 && side.pipsToBearOff(p) > distance) return true;
        }
        return barCount(side) > 0;
    }

    /** Pips needed by {@code side} to bear off all of its checkers. */
    public int pipCount(Side side) {
        int pips = barCount(side) * (POINTS + 1);
        for (int point = 0; point < POINTS; point++) {
            pips += count(point, side) * side.pipsToBearOff(point);
        }
        return pips;
    }

    /** Checkers of {@code side} on the board, on the bar and borne off; always 15 for a consistent board. */
    public int totalCheckers(Side side) {
        int total = barCount(side) + offCount(side);
        for (int point = 0; point < POINTS; point++) total += count(point, side);
        return total;
    }

    /**
     * Moves one checker. Does not check legality (see {@link Rules}); does handle hitting a single opposing checker.
     *
     * @return true when the move hit an opposing blot
     */
    public boolean apply(Move move, Side side) {
        if (move.from() == Move.BAR) {
            if (bar[side.ordinal()] <= 0) throw new IllegalStateException("No " + side + " checker on the bar: " + move);
            bar[side.ordinal()]--;
        } else {
            if (count(move.from(), side) <= 0) throw new IllegalStateException("No " + side + " checker on " + move);
            points[move.from()] -= side.direction();
        }

        if (move.to() == Move.OFF) {
            off[side.ordinal()]++;
            return false;
        }

        boolean hit = count(move.to(), side.opponent()) == 1;
        if (hit) {
            points[move.to()] = 0;
            bar[side.opponent().ordinal()]++;
        }
        points[move.to()] += side.direction();
        return hit;
    }

    // --- setup helpers (used by tests and the starting position) -------------------------------------------------

    /** Puts {@code count} checkers of {@code side} on {@code point}, replacing whatever was there. */
    public Board set(Side side, int point, int count) {
        points[point] = count * side.direction();
        return this;
    }

    public Board setBar(Side side, int count) {
        bar[side.ordinal()] = count;
        return this;
    }

    public Board setOff(Side side, int count) {
        off[side.ordinal()] = count;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board other)) return false;
        return Arrays.equals(points, other.points) && Arrays.equals(bar, other.bar) && Arrays.equals(off, other.off);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * Arrays.hashCode(points) + Arrays.hashCode(bar)) + Arrays.hashCode(off);
    }

    @Override
    public String toString() {
        return "Board" + Arrays.toString(points) + " bar=" + Arrays.toString(bar) + " off=" + Arrays.toString(off);
    }
}
