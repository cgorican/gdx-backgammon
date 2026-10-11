package si.um.feri.backgammon.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class GameTest {

    /** Rolls the given pairs in order. */
    private static DiceRoller scripted(int... values) {
        Deque<DiceRoll> rolls = new ArrayDeque<>();
        for (int i = 0; i < values.length; i += 2) rolls.add(new DiceRoll(values[i], values[i + 1]));
        return rolls::removeFirst;
    }

    private static Destination destination(List<Destination> destinations, int to) {
        return destinations.stream().filter(d -> d.to() == to).findFirst()
                .orElseThrow(() -> new AssertionError("No destination " + to + " in " + destinations));
    }

    @Test
    void turnFlowAlternatesPlayers() {
        Game game = new Game(Side.BRIGHT, scripted(6, 5, 3, 1));
        assertEquals(Game.Phase.AWAITING_ROLL, game.phase());

        game.roll();
        assertEquals(Game.Phase.MOVING, game.phase());
        assertFalse(game.isTurnOver());
        assertThrows(IllegalStateException.class, game::endTurn);

        game.play(new Move(0, 6, 6));
        game.play(new Move(6, 11, 5));
        assertTrue(game.isTurnOver());
        assertTrue(game.dice().allUsed());

        game.endTurn();
        assertEquals(Side.DARK, game.currentSide());
        assertEquals(Game.Phase.AWAITING_ROLL, game.phase());
        assertNull(game.dice());
    }

    @Test
    void illegalMovesAreRejected() {
        Game game = new Game(Side.BRIGHT, scripted(6, 5));
        assertThrows(IllegalStateException.class, () -> game.play(new Move(0, 6, 6)), "must roll first");
        game.roll();
        assertThrows(IllegalArgumentException.class, () -> game.play(new Move(0, 4, 4)), "no 4 rolled");
        assertThrows(IllegalArgumentException.class, () -> game.play(new Move(0, 5, 5)), "point 5 is held by dark");
        assertThrows(IllegalArgumentException.class, () -> game.play(new Move(12, 18, 6)), "not bright's checker");
    }

    @Test
    void doublesGiveFourDice() {
        Game game = new Game(Side.DARK, scripted(4, 4));
        DiceRoll roll = game.roll();
        assertTrue(roll.isDouble());
        assertEquals(List.of(4, 4, 4, 4), roll.remaining());

        game.play(new Move(12, 8, 4));
        assertEquals(List.of(4, 4, 4), game.dice().remaining());
        assertTrue(game.dice().isUsed(0));
        assertFalse(game.dice().isUsed(1));
    }

    @Test
    void blockedPlayerHasNoMovesAndTheTurnIsOverImmediately() {
        Board board = Board.empty().setBar(Side.BRIGHT, 1).set(Side.BRIGHT, 10, 14);
        for (int point = 0; point < 6; point++) board.set(Side.DARK, point, 2);
        board.set(Side.DARK, 20, 3);
        Game game = new Game(board, Side.BRIGHT, scripted(3, 3));

        game.roll();

        assertFalse(game.hasLegalMoves());
        assertTrue(game.isTurnOver());
        assertTrue(game.legalMoves().isEmpty());
        game.endTurn();
        assertEquals(Side.DARK, game.currentSide());
    }

    @Test
    void movableSourcesForcesTheBarWhenACheckerWasHit() {
        Board board = Board.standard();
        board.set(Side.BRIGHT, 0, 1).setBar(Side.BRIGHT, 1);
        Game game = new Game(board, Side.BRIGHT, scripted(3, 1));
        game.roll();
        assertEquals(Set.of(Move.BAR), game.movableSources());
    }

    @Test
    void destinationsIncludeSingleAndCombinedMoves() {
        Game game = new Game(Side.BRIGHT, scripted(3, 1));
        game.roll();

        List<Destination> fromSixteen = game.destinationsFrom(16); // bright's 8-point
        Destination single3 = destination(fromSixteen, 19);
        Destination single1 = destination(fromSixteen, 17);
        Destination combined = destination(fromSixteen, 20);

        assertFalse(single3.isCombination());
        assertEquals("3", single3.diceLabel());
        assertFalse(single1.isCombination());
        assertTrue(combined.isCombination());
        assertEquals(4, combined.pips());
        assertEquals(2, combined.path().size());

        game.play(combined);
        assertEquals(1, game.board().count(20, Side.BRIGHT));
        assertTrue(game.isTurnOver());
    }

    @Test
    void combinationsThroughABlockedPointAreNotOffered() {
        // From index 0 with 6-5: 0+5 = 5 is dark's 6-point (blocked), 0+6 = 6 is open, so 11 is reachable only via 6.
        Game game = new Game(Side.BRIGHT, scripted(6, 5));
        game.roll();
        List<Destination> destinations = game.destinationsFrom(0);
        Set<Integer> targets = destinations.stream().map(Destination::to).collect(Collectors.toSet());
        assertEquals(Set.of(6, 11), targets);
        assertEquals(List.of(new Move(0, 6, 6), new Move(6, 11, 5)), destination(destinations, 11).path());
    }

    @Test
    void doublesOfferUpToFourStepCombinations() {
        Board board = Board.empty().set(Side.DARK, 20, 1).setOff(Side.DARK, 14).set(Side.BRIGHT, 23, 15);
        Game game = new Game(board, Side.DARK, scripted(2, 2));
        game.roll();
        Set<Integer> targets = game.destinationsFrom(20).stream().map(Destination::to).collect(Collectors.toSet());
        assertEquals(Set.of(18, 16, 14, 12), targets);
    }

    @Test
    void bearingOffPrefersTheSmallestSufficientDie() {
        Board board = Board.empty().set(Side.BRIGHT, 22, 1).set(Side.BRIGHT, 23, 1).setOff(Side.BRIGHT, 13)
                .set(Side.DARK, 0, 15);
        Game game = new Game(board, Side.BRIGHT, scripted(6, 4));
        game.roll();
        Destination off = destination(game.destinationsFrom(22), Move.OFF);
        assertEquals(List.of(new Move(22, Move.OFF, 4)), off.path());
    }

    @Test
    void bearingOffTheLastCheckerWinsTheGame() {
        Board board = Board.empty().set(Side.DARK, 0, 1).setOff(Side.DARK, 14).set(Side.BRIGHT, 10, 15);
        Game game = new Game(board, Side.DARK, scripted(2, 1));
        game.roll();

        // Only one die can be used, so the higher one (2) is mandatory.
        assertEquals(List.of(new Move(0, Move.OFF, 2)), game.legalMoves());
        game.play(new Move(0, Move.OFF, 2));

        assertTrue(game.isOver());
        assertEquals(Side.DARK, game.winner());
        assertEquals(Game.WinType.GAMMON, game.winType());
        assertEquals(2, game.winType().points);
        assertFalse(game.isTurnOver());
        assertThrows(IllegalStateException.class, game::roll);
    }

    @Test
    void winTypes() {
        Board single = Board.empty().set(Side.DARK, 0, 1).setOff(Side.DARK, 14).set(Side.BRIGHT, 20, 14).setOff(Side.BRIGHT, 1);
        assertEquals(Game.WinType.SINGLE, winBy(single));

        Board backgammon = Board.empty().set(Side.DARK, 0, 1).setOff(Side.DARK, 14).set(Side.BRIGHT, 20, 14).set(Side.BRIGHT, 3, 1);
        assertEquals(Game.WinType.BACKGAMMON, winBy(backgammon));

        Board onBar = Board.empty().set(Side.DARK, 0, 1).setOff(Side.DARK, 14).set(Side.BRIGHT, 20, 14).setBar(Side.BRIGHT, 1);
        assertEquals(Game.WinType.BACKGAMMON, winBy(onBar));
    }

    private static Game.WinType winBy(Board board) {
        Game game = new Game(board, Side.DARK, scripted(1, 2));
        game.roll();
        game.play(new Move(0, Move.OFF, 2));
        return game.winType();
    }

    @Test
    void randomGamesAlwaysFinishAndKeepFifteenCheckersPerSide() {
        java.util.Random random = new java.util.Random(42);
        for (int g = 0; g < 30; g++) {
            Game game = new Game(Side.BRIGHT, DiceRoller.random(random));
            int turns = 0;
            while (!game.isOver()) {
                assertTrue(++turns < 2000, "game did not finish");
                game.roll();
                while (game.hasLegalMoves()) {
                    List<Move> moves = game.legalMoves();
                    game.play(moves.get(random.nextInt(moves.size())));
                    for (Side side : Side.values()) assertEquals(15, game.board().totalCheckers(side));
                }
                if (!game.isOver()) game.endTurn();
            }
            assertTrue(game.board().offCount(game.winner()) == 15);
        }
    }
}
