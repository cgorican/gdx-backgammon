package si.um.feri.backgammon.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import org.junit.jupiter.api.Test;

import si.um.feri.backgammon.model.Move;
import si.um.feri.backgammon.model.Side;

class BoardLayoutTest {

    @Test
    void pointColumnsMatchTheBoardArt() {
        assertEquals(1550f, BoardLayout.pointX(0));   // bottom right, next to the right tray
        assertEquals(1050f, BoardLayout.pointX(5));   // right of the bar
        assertEquals(770f, BoardLayout.pointX(6));    // left of the bar
        assertEquals(270f, BoardLayout.pointX(11));   // bottom left, next to the left tray
        assertEquals(270f, BoardLayout.pointX(12));   // top left
        assertEquals(770f, BoardLayout.pointX(17));
        assertEquals(1050f, BoardLayout.pointX(18));
        assertEquals(1550f, BoardLayout.pointX(23));  // top right
    }

    @Test
    void columnsNeverOverlapAndStayOffTheBar() {
        for (int a = 0; a < 24; a++) {
            Rectangle ra = BoardLayout.pointBounds(a);
            assertFalse(ra.overlaps(BoardLayout.barBounds(Side.BRIGHT)), "point " + a);
            assertFalse(ra.overlaps(BoardLayout.barBounds(Side.DARK)), "point " + a);
            for (int b = a + 1; b < 24; b++) {
                assertFalse(ra.overlaps(BoardLayout.pointBounds(b)), a + " vs " + b);
            }
        }
    }

    @Test
    void stacksStayInsideTheirTriangle() {
        for (int total = 1; total <= 15; total++) {
            Vector2 last = BoardLayout.checkerPosition(3, total - 1, total);
            assertTrue(last.y + BoardLayout.CHECKER_SIZE <= BoardLayout.POINT_HEIGHT + 0.01f, "bottom stack of " + total);
            Vector2 lastTop = BoardLayout.checkerPosition(20, total - 1, total);
            assertTrue(lastTop.y >= BoardLayout.WORLD_HEIGHT - BoardLayout.POINT_HEIGHT - 0.01f, "top stack of " + total);
        }
        assertEquals(100f, BoardLayout.stackSpacing(5));
        assertEquals(80f, BoardLayout.stackSpacing(6));
    }

    @Test
    void locateFindsPointsBarAndTray() {
        assertEquals(0, BoardLayout.locate(1600, 50, Side.BRIGHT));
        assertEquals(23, BoardLayout.locate(1600, 1000, Side.DARK));
        assertEquals(Move.BAR, BoardLayout.locate(960, 900, Side.BRIGHT));
        assertEquals(Move.BAR, BoardLayout.locate(960, 100, Side.DARK));
        assertEquals(BoardLayout.NONE, BoardLayout.locate(960, 100, Side.BRIGHT), "the other side's half of the bar");
        assertEquals(Move.OFF, BoardLayout.locate(1785, 900, Side.BRIGHT));
        assertEquals(Move.OFF, BoardLayout.locate(1785, 200, Side.DARK));
        assertEquals(BoardLayout.NONE, BoardLayout.locate(570, 540, Side.BRIGHT), "the middle strip");
    }

    @Test
    void offTrayHoldsFifteenCheckersInsideItsBounds() {
        for (Side side : Side.values()) {
            Rectangle bounds = BoardLayout.offBounds(side);
            for (int i = 0; i < 15; i++) {
                Vector2 p = BoardLayout.offPosition(side, i);
                assertTrue(bounds.contains(p.x + 50, p.y + 12), side + " #" + i);
            }
        }
    }
}
