package si.um.feri.backgammon.model;

/**
 * A single checker movement using one die.
 *
 * @param from a point index (0..23) or {@link #BAR}
 * @param to   a point index (0..23) or {@link #OFF}
 * @param die  the die value (1..6) this move consumes
 */
public record Move(int from, int to, int die) {
    /** Pseudo-location of checkers that were hit and must re-enter. */
    public static final int BAR = -1;
    /** Pseudo-location of checkers that were borne off. */
    public static final int OFF = -2;

    public boolean isFromBar() {
        return from == BAR;
    }

    public boolean isBearOff() {
        return to == OFF;
    }

    @Override
    public String toString() {
        return name(from) + "/" + name(to) + "(" + die + ")";
    }

    private static String name(int location) {
        if (location == BAR) return "bar";
        if (location == OFF) return "off";
        return Integer.toString(location + 1);
    }
}
