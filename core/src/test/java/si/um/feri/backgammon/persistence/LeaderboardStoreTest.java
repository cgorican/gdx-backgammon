package si.um.feri.backgammon.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.files.FileHandle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LeaderboardStoreTest {

    @TempDir
    Path dir;

    private FileHandle legacyFile(String json) throws IOException {
        File file = dir.resolve(LeaderboardStore.LEGACY_FILE_NAME).toFile();
        if (json != null) Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
        return new FileHandle(file);
    }

    @Test
    void winsAreSavedInThePreferences() throws IOException {
        InMemoryPreferences prefs = new InMemoryPreferences();
        LeaderboardStore store = new LeaderboardStore(prefs, legacyFile(null));

        store.recordWin("Ana", 2);

        assertTrue(prefs.flushes > 0);
        LeaderboardStore reopened = new LeaderboardStore(prefs, legacyFile(null));
        assertEquals(2, reopened.get().find("Ana").points);
    }

    @Test
    void importsTheOldLeaderboardFileOnce() throws IOException {
        InMemoryPreferences prefs = new InMemoryPreferences();
        FileHandle legacy = legacyFile("{\"users\":[{\"username\":\"crt\",\"wins\":4}]}");

        assertEquals(4, new LeaderboardStore(prefs, legacy).get().find("crt").wins);
        assertTrue(prefs.contains(LeaderboardStore.KEY), "imported into the preferences");

        // Later changes live in the preferences; the old file is no longer read.
        new LeaderboardStore(prefs, legacy).recordWin("crt", 1);
        assertEquals(5, new LeaderboardStore(prefs, legacy).get().find("crt").wins);
    }

    @Test
    void missingOrBrokenOldFileGivesAnEmptyLeaderboard() throws IOException {
        assertTrue(new LeaderboardStore(new InMemoryPreferences(), legacyFile(null)).get().ranking().isEmpty());
        assertTrue(new LeaderboardStore(new InMemoryPreferences(), legacyFile("{broken")).get().ranking().isEmpty());
        assertTrue(new LeaderboardStore(new InMemoryPreferences(), null).get().ranking().isEmpty());
    }
}
