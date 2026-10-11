package si.um.feri.backgammon.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RulesTest {

    private static List<Integer> dice(Integer... values) {
        return Arrays.asList(values);
    }

    private static Set<Move> firstMoves(Board board, Side side, Integer... dice) {
        return new HashSet<>(Rules.legalFirstMoves(board, side, dice(dice)));
    }

    @Test
    void cannotLandOnAPointHeldByTwoOpposingCheckers() {
        Board board = Board.empty().set(Side.BRIGHT, 0, 1).set(Side.DARK, 3, 2);
        assertFalse(Rules.singleMoves(board, Side.BRIGHT, 3).contains(new Move(0, 3, 3)));
        assertTrue(Rules.singleMoves(board, Side.BRIGHT, 2).contains(new Move(0, 2, 2)));
    }

    @Test
    void openingRollUsesBothDice() {
        List<List<Move>> sequences = Rules.legalSequences(Board.standard(), Side.BRIGHT, dice(6, 5));
        for (List<Move> sequence : sequences) assertEquals(2, sequence.size());
        // "Lover's leap": 24/18/13, i.e. index 0 -> 6 -> 11.
        assertTrue(sequences.contains(Arrays.asList(new Move(0, 6, 6), new Move(6, 11, 5))));
    }

    @Test
    void checkersOnTheBarMustEnterFirst() {
        Board board = Board.standard();
        board.apply(new Move(0, 3, 3), Side.BRIGHT); // leave a bright checker on index 3...
        board.setBar(Side.BRIGHT, 1).set(Side.BRIGHT, 0, 0); // ...and one on the bar
        Set<Move> moves = firstMoves(board, Side.BRIGHT, 3, 4);
        assertEquals(Set.of(new Move(Move.BAR, 2, 3), new Move(Move.BAR, 3, 4)), moves);
    }

    @Test
    void blockedBarEntryWithDoublesHasNoMovesInsteadOfCrashing() {
        // Regression: the old code indexed boardState[24] / boardState[-1] when doubles were rolled on the bar.
        Board board = Board.empty().setBar(Side.DARK, 1).set(Side.DARK, 10, 14).set(Side.BRIGHT, 21, 2);
        List<List<Move>> sequences = Rules.legalSequences(board, Side.DARK, dice(3, 3, 3, 3));
        assertEquals(List.of(List.of()), sequences);

        Board brightOnBar = Board.empty().setBar(Side.BRIGHT, 1).set(Side.BRIGHT, 10, 14).set(Side.DARK, 3, 2);
        assertEquals(List.of(List.of()), Rules.legalSequences(brightOnBar, Side.BRIGHT, dice(4, 4, 4, 4)));
    }

    @Test
    void afterEnteringTheRestOfTheDiceCanBeUsedNormally() {
        Board board = Board.empty().setBar(Side.BRIGHT, 1).set(Side.BRIGHT, 10, 14).set(Side.DARK, 20, 15);
        List<List<Move>> sequences = Rules.legalSequences(board, Side.BRIGHT, dice(2, 2, 2, 2));
        for (List<Move> sequence : sequences) {
            assertEquals(4, sequence.size());
            assertEquals(new Move(Move.BAR, 1, 2), sequence.get(0));
        }
    }

    @Test
    void bearingOffRequiresAllCheckersHome() {
        Board board = Board.empty().set(Side.BRIGHT, 22, 14).set(Side.BRIGHT, 17, 1);
        assertFalse(Rules.singleMoves(board, Side.BRIGHT, 2).contains(new Move(22, Move.OFF, 2)));

        board.set(Side.BRIGHT, 17, 0).set(Side.BRIGHT, 18, 1);
        assertTrue(Rules.singleMoves(board, Side.BRIGHT, 2).contains(new Move(22, Move.OFF, 2)));
    }

    @Test
    void higherDieBearsOffOnlyTheFarthestChecker() {
        Board board = Board.empty().set(Side.DARK, 3, 1).set(Side.DARK, 1, 2).setOff(Side.DARK, 12);
        List<Move> moves = Rules.singleMoves(board, Side.DARK, 6);
        assertTrue(moves.contains(new Move(3, Move.OFF, 6)), "farthest checker (4 pips) bears off with a 6");
        assertFalse(moves.contains(new Move(1, Move.OFF, 6)), "a nearer checker must not bear off while one is behind");
    }

    @Test
    void oldSoftLockPositionIsCorrectlyReportedAsBlocked() {
        // Regression: bright on 19 (index 18) and 23 (index 22), dark holds index 23. With a 5 the old code
        // believed a move existed (index 22 bearing off) but rejected it on click, so the turn never ended.
        Board board = Board.empty().set(Side.BRIGHT, 18, 1).set(Side.BRIGHT, 22, 1).setOff(Side.BRIGHT, 13)
                .set(Side.DARK, 23, 2).set(Side.DARK, 0, 13);
        assertTrue(Rules.singleMoves(board, Side.BRIGHT, 5).isEmpty());
        assertEquals(List.of(List.of()), Rules.legalSequences(board, Side.BRIGHT, dice(5, 5, 5, 5)));
    }

    @Test
    void mustPlayBothDiceWhenPossible() {
        // Bright has one checker on index 0. Dark blocks index 6 (0+6) and index 9 (0+4+5... ) etc.
        // With 6-1: 0->6 is blocked, so the only way to use both dice is 0->1 then 1->7.
        Board board = Board.empty().set(Side.BRIGHT, 0, 1).setOff(Side.BRIGHT, 14)
                .set(Side.DARK, 6, 2).set(Side.DARK, 12, 13);
        List<List<Move>> sequences = Rules.legalSequences(board, Side.BRIGHT, dice(6, 1));
        assertEquals(List.of(List.of(new Move(0, 1, 1), new Move(1, 7, 6))), sequences);
    }

    @Test
    void whenOnlyOneDieCanBePlayedTheLargerMustBeUsed() {
        // Bright checker on index 10 can move 10->13 (3) or 10->15 (5), but never both: 13+5=18 and 15+3=18 are blocked.
        Board board = Board.empty().set(Side.BRIGHT, 10, 1).setOff(Side.BRIGHT, 14)
                .set(Side.DARK, 18, 2).set(Side.DARK, 0, 13);
        List<List<Move>> sequences = Rules.legalSequences(board, Side.BRIGHT, dice(3, 5));
        assertEquals(List.of(List.of(new Move(10, 15, 5))), sequences);
    }

    @Test
    void doublesCanMoveFourTimes() {
        Board board = Board.empty().set(Side.BRIGHT, 0, 1).setOff(Side.BRIGHT, 14).set(Side.DARK, 23, 15);
        List<List<Move>> sequences = Rules.legalSequences(board, Side.BRIGHT, dice(2, 2, 2, 2));
        assertEquals(List.of(List.of(new Move(0, 2, 2), new Move(2, 4, 2), new Move(4, 6, 2), new Move(6, 8, 2))),
                sequences);
    }
}
