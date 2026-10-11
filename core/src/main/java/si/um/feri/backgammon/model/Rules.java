package si.um.feri.backgammon.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Backgammon move legality.
 * <ul>
 *     <li>Checkers on the bar must re-enter before any other checker moves.</li>
 *     <li>A checker may land on an empty point, its own point, or a single opposing checker (a hit).</li>
 *     <li>Bearing off is allowed once all checkers are in the home board. A die higher than needed may bear off
 *     the farthest checker only.</li>
 *     <li>A player must use as many dice as possible; if only one die of a non-double can be used, it must be the
 *     larger one when that is playable.</li>
 * </ul>
 */
public final class Rules {
    private Rules() {
    }

    /** All moves {@code side} could make with a single {@code die}, ignoring the "use as many dice" rule. */
    public static List<Move> singleMoves(Board board, Side side, int die) {
        List<Move> moves = new ArrayList<>();
        if (board.barCount(side) > 0) {
            int entry = side.entryPoint(die);
            if (board.isOpenFor(entry, side)) moves.add(new Move(Move.BAR, entry, die));
            return moves;
        }

        boolean canBearOff = board.allInHome(side);
        for (int point = 0; point < Board.POINTS; point++) {
            if (board.count(point, side) == 0) continue;
            int target = point + side.direction() * die;
            if (target >= 0 && target < Board.POINTS) {
                if (board.isOpenFor(target, side)) moves.add(new Move(point, target, die));
            } else if (canBearOff) {
                int distance = side.pipsToBearOff(point);
                if (die == distance || (die > distance && !board.hasCheckerBehind(point, side))) {
                    moves.add(new Move(point, Move.OFF, die));
                }
            }
        }
        return moves;
    }

    /**
     * Every complete, legal way to play the given dice: each returned sequence uses the maximum possible number of
     * dice, and respects the higher-die rule. Returns a single empty sequence when no move is possible.
     *
     * @param dice the unused dice values, in any order (four equal values for doubles)
     */
    public static List<List<Move>> legalSequences(Board board, Side side, List<Integer> dice) {
        List<List<Move>> all = new ArrayList<>();
        search(board, side, new ArrayList<>(dice), new ArrayList<>(), all);

        int longest = 0;
        for (List<Move> sequence : all) longest = Math.max(longest, sequence.size());
        if (longest == 0) {
            List<List<Move>> none = new ArrayList<>();
            none.add(Collections.emptyList());
            return none;
        }

        List<List<Move>> result = new ArrayList<>();
        for (List<Move> sequence : all) {
            if (sequence.size() == longest) result.add(sequence);
        }

        // Only one of two different dice can be played: the larger must be used if any such play exists.
        if (longest == 1 && dice.size() == 2 && !dice.get(0).equals(dice.get(1))) {
            int larger = Math.max(dice.get(0), dice.get(1));
            List<List<Move>> withLarger = new ArrayList<>();
            for (List<Move> sequence : result) {
                if (sequence.get(0).die() == larger) withLarger.add(sequence);
            }
            if (!withLarger.isEmpty()) result = withLarger;
        }
        return result;
    }

    /** The distinct first moves of all {@link #legalSequences legal sequences}. */
    public static List<Move> legalFirstMoves(Board board, Side side, List<Integer> dice) {
        Set<Move> moves = new LinkedHashSet<>();
        for (List<Move> sequence : legalSequences(board, side, dice)) {
            if (!sequence.isEmpty()) moves.add(sequence.get(0));
        }
        return new ArrayList<>(moves);
    }

    private static void search(Board board, Side side, List<Integer> dice, List<Move> played, List<List<Move>> out) {
        boolean moved = false;
        Set<Integer> triedDice = new LinkedHashSet<>();
        for (int i = 0; i < dice.size(); i++) {
            int die = dice.get(i);
            if (!triedDice.add(die)) continue; // equal dice give identical branches
            for (Move move : singleMoves(board, side, die)) {
                moved = true;
                Board next = board.copy();
                next.apply(move, side);
                List<Integer> remaining = new ArrayList<>(dice);
                remaining.remove(i);
                played.add(move);
                search(next, side, remaining, played, out);
                played.remove(played.size() - 1);
            }
        }
        if (!moved && !played.isEmpty()) out.add(new ArrayList<>(played));
    }
}
