package si.um.feri.backgammon.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Somewhere the selected checker can go this turn.
 *
 * @param to   the target point index or {@link Move#OFF}
 * @param path the moves that take the checker there: one move for a single-die move, several for a combination
 */
public record Destination(int to, List<Move> path) {
    public Destination {
        // Java 8 APIs only: this code also runs on Android (minSdk 21).
        path = Collections.unmodifiableList(new ArrayList<>(path));
    }

    public boolean isCombination() {
        return path.size() > 1;
    }

    /** The dice this destination consumes, e.g. "5" or "3+5". */
    public String diceLabel() {
        StringBuilder sb = new StringBuilder();
        for (Move move : path) {
            if (sb.length() > 0) sb.append('+');
            sb.append(move.die());
        }
        return sb.toString();
    }

    public int pips() {
        int pips = 0;
        for (Move move : path) pips += move.die();
        return pips;
    }
}
