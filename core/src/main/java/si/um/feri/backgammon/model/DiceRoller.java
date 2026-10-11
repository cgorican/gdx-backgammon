package si.um.feri.backgammon.model;

import java.util.Random;

/** Source of dice rolls; replaceable in tests. */
@FunctionalInterface
public interface DiceRoller {
    DiceRoll roll();

    static DiceRoller random(Random random) {
        return () -> new DiceRoll(random.nextInt(6) + 1, random.nextInt(6) + 1);
    }
}
