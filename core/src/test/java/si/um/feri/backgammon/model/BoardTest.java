package si.um.feri.backgammon.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BoardTest {

    @Test
    void standardPositionHasFifteenCheckersAndPipCount167PerSide() {
        Board board = Board.standard();
        for (Side side : Side.values()) {
            assertEquals(15, board.totalCheckers(side), side.name());
            assertEquals(167, board.pipCount(side), side.name());
            assertEquals(0, board.barCount(side));
            assertEquals(0, board.offCount(side));
        }
    }

    @Test
    void standardPositionMatchesTheOriginalLayout() {
        Board board = Board.standard();
        assertEquals(2, board.count(0, Side.BRIGHT));
        assertEquals(5, board.count(11, Side.BRIGHT));
        assertEquals(3, board.count(16, Side.BRIGHT));
        assertEquals(5, board.count(18, Side.BRIGHT));
        assertEquals(5, board.count(5, Side.DARK));
        assertEquals(3, board.count(7, Side.DARK));
        assertEquals(5, board.count(12, Side.DARK));
        assertEquals(2, board.count(23, Side.DARK));
    }

    @Test
    void sidesAreMirrored() {
        assertEquals(0, Side.BRIGHT.entryPoint(1));
        assertEquals(5, Side.BRIGHT.entryPoint(6));
        assertEquals(23, Side.DARK.entryPoint(1));
        assertEquals(18, Side.DARK.entryPoint(6));
        assertEquals(1, Side.BRIGHT.pipsToBearOff(23));
        assertEquals(1, Side.DARK.pipsToBearOff(0));
        assertTrue(Side.BRIGHT.isHomePoint(18));
        assertFalse(Side.BRIGHT.isHomePoint(17));
        assertTrue(Side.DARK.isHomePoint(5));
        assertFalse(Side.DARK.isHomePoint(6));
    }

    @Test
    void hittingABlotSendsItToTheBar() {
        Board board = Board.empty().set(Side.BRIGHT, 3, 1).set(Side.DARK, 7, 1);

        boolean hit = board.apply(new Move(3, 7, 4), Side.BRIGHT);

        assertTrue(hit);
        assertEquals(1, board.count(7, Side.BRIGHT));
        assertEquals(0, board.count(7, Side.DARK));
        assertEquals(1, board.barCount(Side.DARK));
        assertEquals(Side.BRIGHT, board.owner(7));
        assertNull(board.owner(3));
    }

    @Test
    void enteringFromTheBarAndBearingOffUpdateCounters() {
        Board board = Board.empty().setBar(Side.DARK, 1).set(Side.DARK, 0, 1);

        board.apply(new Move(Move.BAR, 20, 4), Side.DARK);
        board.apply(new Move(0, Move.OFF, 1), Side.DARK);

        assertEquals(0, board.barCount(Side.DARK));
        assertEquals(1, board.count(20, Side.DARK));
        assertEquals(1, board.offCount(Side.DARK));
    }

    @Test
    void applyingAMoveWithoutACheckerFails() {
        Board board = Board.empty();
        assertThrows(IllegalStateException.class, () -> board.apply(new Move(4, 6, 2), Side.BRIGHT));
        assertThrows(IllegalStateException.class, () -> board.apply(new Move(Move.BAR, 2, 3), Side.BRIGHT));
    }

    @Test
    void copyIsIndependent() {
        Board board = Board.standard();
        Board copy = board.copy();
        assertEquals(board, copy);

        copy.apply(new Move(0, 3, 3), Side.BRIGHT);

        assertNotEquals(board, copy);
        assertEquals(2, board.count(0, Side.BRIGHT));
    }

    @Test
    void allInHomeRequiresEveryCheckerInTheHomeBoardAndNoneOnTheBar() {
        Board board = Board.empty().set(Side.BRIGHT, 18, 10).setOff(Side.BRIGHT, 5);
        assertTrue(board.allInHome(Side.BRIGHT));

        board.setBar(Side.BRIGHT, 1);
        assertFalse(board.allInHome(Side.BRIGHT));

        board.setBar(Side.BRIGHT, 0).set(Side.BRIGHT, 17, 1);
        assertFalse(board.allInHome(Side.BRIGHT));
    }

    @Test
    void pipCountCountsBarCheckersAs25() {
        Board board = Board.empty().setBar(Side.DARK, 1).set(Side.DARK, 0, 2);
        assertEquals(25 + 2, board.pipCount(Side.DARK));
    }

    @Test
    void legacyStoredSideNamesAreParsed() {
        assertEquals(Side.DARK, Side.parse("DARK", Side.BRIGHT));
        assertEquals(Side.BRIGHT, Side.parse("NONE", Side.BRIGHT));
        assertEquals(Side.BRIGHT, Side.parse(null, Side.BRIGHT));
    }
}
