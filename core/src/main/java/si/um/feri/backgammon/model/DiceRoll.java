package si.um.feri.backgammon.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The dice of one turn: two values, or four equal values for doubles, each either used or still available. */
public final class DiceRoll {
    private final int[] values;
    private final boolean[] used;

    public DiceRoll(int first, int second) {
        checkDie(first);
        checkDie(second);
        values = first == second ? new int[]{first, first, first, first} : new int[]{first, second};
        used = new boolean[values.length];
    }

    private static void checkDie(int value) {
        if (value < 1 || value > 6) throw new IllegalArgumentException("Die value out of range: " + value);
    }

    public boolean isDouble() {
        return values.length == 4;
    }

    /** Number of dice to play: 2, or 4 for doubles. */
    public int size() {
        return values.length;
    }

    public int value(int index) {
        return values[index];
    }

    public boolean isUsed(int index) {
        return used[index];
    }

    public List<Integer> remaining() {
        List<Integer> remaining = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            if (!used[i]) remaining.add(values[i]);
        }
        return Collections.unmodifiableList(remaining);
    }

    public boolean allUsed() {
        for (boolean u : used) {
            if (!u) return false;
        }
        return true;
    }

    /** Marks the first unused die with this value as used. */
    void use(int value) {
        for (int i = 0; i < values.length; i++) {
            if (!used[i] && values[i] == value) {
                used[i] = true;
                return;
            }
        }
        throw new IllegalStateException("No unused die with value " + value + " in " + this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(values[i]);
            if (used[i]) sb.append('*');
        }
        return sb.append(']').toString();
    }
}
