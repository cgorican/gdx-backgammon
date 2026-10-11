package si.um.feri.backgammon.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One game of backgammon between two local players: whose turn it is, the dice, and the legal moves.
 * Contains no rendering code, so it can be unit-tested and driven by any UI.
 *
 * <p>Turn flow: {@link #roll()} &rarr; zero or more {@link #play(Move)} / {@link #play(Destination)} &rarr;
 * {@link #endTurn()} once {@link #isTurnOver()}. When a player has no legal move, the turn is over right after rolling.
 */
public final class Game {
    public enum Phase {AWAITING_ROLL, MOVING, GAME_OVER}

    public enum WinType {
        SINGLE(1), GAMMON(2), BACKGAMMON(3);

        public final int points;

        WinType(int points) {
            this.points = points;
        }
    }

    private final Board board;
    private final DiceRoller roller;
    private Side current;
    private Phase phase = Phase.AWAITING_ROLL;
    private DiceRoll dice;
    private List<List<Move>> sequences = Collections.emptyList();
    private Side winner;

    public Game(Side startingSide, DiceRoller roller) {
        this(Board.standard(), startingSide, roller);
    }

    public Game(Board board, Side startingSide, DiceRoller roller) {
        this.board = board;
        this.current = startingSide;
        this.roller = roller;
    }

    /** The live board. Callers must not modify it; all changes go through {@link #play(Move)}. */
    public Board board() {
        return board;
    }

    public Side currentSide() {
        return current;
    }

    public Phase phase() {
        return phase;
    }

    /** The current turn's dice, or {@code null} before rolling. */
    public DiceRoll dice() {
        return dice;
    }

    public DiceRoll roll() {
        requirePhase(Phase.AWAITING_ROLL);
        dice = roller.roll();
        phase = Phase.MOVING;
        recomputeSequences();
        return dice;
    }

    /** True while the current player still has a legal move to make this turn. */
    public boolean hasLegalMoves() {
        return phase == Phase.MOVING && !sequences.isEmpty() && !sequences.get(0).isEmpty();
    }

    /** The turn is finished: every usable die has been used (possibly none, when the player is blocked). */
    public boolean isTurnOver() {
        return phase == Phase.MOVING && !hasLegalMoves();
    }

    /** Legal single-die moves available right now. */
    public List<Move> legalMoves() {
        Set<Move> moves = new LinkedHashSet<>();
        if (hasLegalMoves()) {
            for (List<Move> sequence : sequences) moves.add(sequence.get(0));
        }
        return new ArrayList<>(moves);
    }

    /** Locations (point indexes or {@link Move#BAR}) holding a checker that can move right now. */
    public Set<Integer> movableSources() {
        Set<Integer> sources = new LinkedHashSet<>();
        for (Move move : legalMoves()) sources.add(move.from());
        return sources;
    }

    /**
     * Where the checker at {@code from} can go: single-die moves and combinations moving the same checker with
     * several dice. Each destination keeps the shortest path (and for bearing off, the smallest dice).
     */
    public List<Destination> destinationsFrom(int from) {
        Map<Integer, Destination> best = new LinkedHashMap<>();
        if (!hasLegalMoves()) return new ArrayList<>();
        for (List<Move> sequence : sequences) {
            int position = from;
            for (int k = 0; k < sequence.size(); k++) {
                Move move = sequence.get(k);
                if (move.from() != position) break;
                position = move.to();
                Destination candidate = new Destination(position, sequence.subList(0, k + 1));
                Destination existing = best.get(position);
                if (existing == null || isBetter(candidate, existing)) best.put(position, candidate);
                if (position == Move.OFF) break;
            }
        }
        List<Destination> result = new ArrayList<>(best.values());
        result.sort(Comparator.comparingInt((Destination d) -> d.path().size()).thenComparingInt(Destination::pips));
        return result;
    }

    private static boolean isBetter(Destination candidate, Destination existing) {
        if (candidate.path().size() != existing.path().size()) return candidate.path().size() < existing.path().size();
        return candidate.pips() < existing.pips();
    }

    /**
     * Plays one move.
     *
     * @return true if the move hit an opposing checker
     * @throws IllegalArgumentException if the move is not legal now
     */
    public boolean play(Move move) {
        requirePhase(Phase.MOVING);
        if (!legalMoves().contains(move)) {
            throw new IllegalArgumentException("Illegal move " + move + " for " + current + " with dice " + dice);
        }
        boolean hit = board.apply(move, current);
        dice.use(move.die());
        if (board.offCount(current) == Board.CHECKERS_PER_SIDE) {
            winner = current;
            phase = Phase.GAME_OVER;
            sequences = Collections.emptyList();
        } else {
            recomputeSequences();
        }
        return hit;
    }

    /** Plays every move of a destination's path in order. */
    public void play(Destination destination) {
        for (Move move : destination.path()) play(move);
    }

    /** Passes the dice to the opponent. Only allowed once {@link #isTurnOver()}. */
    public void endTurn() {
        if (!isTurnOver()) throw new IllegalStateException("Turn is not over: legal moves remain or not rolled yet");
        current = current.opponent();
        dice = null;
        sequences = Collections.emptyList();
        phase = Phase.AWAITING_ROLL;
    }

    public boolean isOver() {
        return phase == Phase.GAME_OVER;
    }

    /** The winner, or {@code null} while the game is running. */
    public Side winner() {
        return winner;
    }

    /** Single, gammon (loser bore off nothing) or backgammon (... and still has a checker on the bar or in the
     * winner's home board). {@code null} while the game is running. */
    public WinType winType() {
        if (winner == null) return null;
        Side loser = winner.opponent();
        if (board.offCount(loser) > 0) return WinType.SINGLE;
        if (board.barCount(loser) > 0) return WinType.BACKGAMMON;
        for (int point = 0; point < Board.POINTS; point++) {
            if (board.count(point, loser) > 0 && winner.isHomePoint(point)) return WinType.BACKGAMMON;
        }
        return WinType.GAMMON;
    }

    private void recomputeSequences() {
        sequences = Rules.legalSequences(board, current, dice.remaining());
    }

    private void requirePhase(Phase expected) {
        if (phase != expected) throw new IllegalStateException("Expected phase " + expected + " but was " + phase);
    }
}
