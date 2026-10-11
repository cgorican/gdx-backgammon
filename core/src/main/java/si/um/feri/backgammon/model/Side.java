package si.um.feri.backgammon.model;

/**
 * The two players. Board points are indexed 0..23.
 * <ul>
 *     <li>{@link #BRIGHT} moves from low to high indexes; its home board is points 18..23.</li>
 *     <li>{@link #DARK} moves from high to low indexes; its home board is points 0..5.</li>
 * </ul>
 */
public enum Side {
    BRIGHT("Bright", 1),
    DARK("Dark", -1);

    private final String displayName;
    private final int direction;

    Side(String displayName, int direction) {
        this.displayName = displayName;
        this.direction = direction;
    }

    public String displayName() {
        return displayName;
    }

    /** +1 when this side moves towards higher point indexes, -1 otherwise. */
    public int direction() {
        return direction;
    }

    public Side opponent() {
        return this == BRIGHT ? DARK : BRIGHT;
    }

    /** The point a checker enters on from the bar with the given die. */
    public int entryPoint(int die) {
        return this == BRIGHT ? die - 1 : Board.POINTS - die;
    }

    /** Distance in pips from the given point to bearing off (1 for the last point of the home board). */
    public int pipsToBearOff(int point) {
        return this == BRIGHT ? Board.POINTS - point : point + 1;
    }

    public boolean isHomePoint(int point) {
        return pipsToBearOff(point) >= 1 && pipsToBearOff(point) <= Board.HOME_POINTS;
    }

    /** Parses a stored name, falling back to {@code fallback} for unknown values (e.g. the legacy "NONE"). */
    public static Side parse(String name, Side fallback) {
        if (name != null) {
            for (Side side : values()) {
                if (side.name().equalsIgnoreCase(name.trim())) return side;
            }
        }
        return fallback;
    }
}
