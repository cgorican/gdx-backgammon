package si.um.feri.backgammon.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class LeaderboardTest {

    @Test
    void winsAccumulatePerNameAndRankingIsByWinsThenPoints() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.recordWin("Ana", 1);
        leaderboard.recordWin("Bor", 2);
        leaderboard.recordWin(" Ana ", 1);
        leaderboard.recordWin("Cene", 3);
        leaderboard.recordWin("Bor", 1);

        List<PlayerScore> ranking = leaderboard.ranking();
        assertEquals(List.of("Bor", "Ana", "Cene"), ranking.stream().map(s -> s.username).toList());
        assertEquals(2, ranking.get(0).wins);
        assertEquals(3, ranking.get(0).points);
    }

    @Test
    void blankNamesAreRecordedAsPlayer() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.recordWin("   ", 1);
        assertEquals("Player", leaderboard.ranking().get(0).username);
    }

    @Test
    void jsonRoundTrip() {
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.recordWin("Ana", 2);
        leaderboard.recordWin("Žan", 1);

        Leaderboard loaded = Leaderboard.fromJson(leaderboard.toJson());

        assertEquals(2, loaded.ranking().size());
        assertEquals(2, loaded.find("Ana").points);
        assertEquals(1, loaded.find("Žan").wins);
    }

    @Test
    void readsFilesWrittenByThePreviousGsonVersion() {
        String legacy = "{\"users\":[{\"username\":\"player1\",\"wins\":0},{\"username\":\"crt\",\"wins\":4},"
                + "{\"username\":\"player2\",\"wins\":1}]}";
        Leaderboard loaded = Leaderboard.fromJson(legacy);

        List<PlayerScore> ranking = loaded.ranking();
        assertEquals(2, ranking.size(), "rows without wins are hidden");
        assertEquals("crt", ranking.get(0).username);
        assertEquals(4, ranking.get(0).wins);
        assertEquals(0, ranking.get(0).points);
    }

    @Test
    void corruptOrMissingDataGivesAnEmptyLeaderboard() {
        assertTrue(Leaderboard.fromJson(null).ranking().isEmpty());
        assertTrue(Leaderboard.fromJson("").ranking().isEmpty());
        assertTrue(Leaderboard.fromJson("{not json").ranking().isEmpty());
        assertTrue(Leaderboard.fromJson("{\"users\":null}").ranking().isEmpty());
    }

    @Test
    void namesAreSanitized() {
        assertEquals("Fallback", Settings.sanitizeName("  ", "Fallback"));
        assertEquals("Ana Bor", Settings.sanitizeName("  Ana   Bor ", "x"));
        assertEquals(Settings.MAX_NAME_LENGTH, Settings.sanitizeName("a".repeat(40), "x").length());
    }
}
