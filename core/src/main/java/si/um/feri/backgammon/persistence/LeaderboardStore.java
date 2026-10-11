package si.um.feri.backgammon.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Loads and saves the {@link Leaderboard} in the per-user {@link Preferences} (the same store as the settings), so it
 * works no matter which directory the game is started from, e.g. a packaged macOS app, whose working directory is
 * read-only. A leaderboard file written by older versions next to the game is imported once.
 */
public class LeaderboardStore {
    private static final String TAG = LeaderboardStore.class.getSimpleName();
    static final String KEY = "leaderboard";
    /** File used by versions up to 1.1.0, relative to the working directory. */
    public static final String LEGACY_FILE_NAME = "backgammon_leaderboard.json";

    private final Preferences prefs;
    private final FileHandle legacyFile;
    private Leaderboard leaderboard;

    /** @param legacyFile old leaderboard file to import when the preferences have none yet; may be null */
    public LeaderboardStore(Preferences prefs, FileHandle legacyFile) {
        this.prefs = prefs;
        this.legacyFile = legacyFile;
    }

    public Leaderboard get() {
        if (leaderboard == null) {
            String json = prefs.getString(KEY, "");
            if (json.isEmpty()) {
                json = readLegacyFile();
                leaderboard = Leaderboard.fromJson(json);
                if (!leaderboard.users.isEmpty()) save();
            } else {
                leaderboard = Leaderboard.fromJson(json);
            }
        }
        return leaderboard;
    }

    public void recordWin(String username, int points) {
        get().recordWin(username, points);
        save();
    }

    private void save() {
        try {
            prefs.putString(KEY, leaderboard.toJson());
            prefs.flush();
        } catch (GdxRuntimeException e) {
            log("Could not save the leaderboard", e);
        }
    }

    private String readLegacyFile() {
        try {
            if (legacyFile != null && legacyFile.exists()) return legacyFile.readString("UTF-8");
        } catch (GdxRuntimeException e) {
            log("Could not read " + legacyFile.path(), e);
        }
        return null;
    }

    private static void log(String message, Exception e) {
        if (Gdx.app != null) Gdx.app.error(TAG, message, e);
    }
}
