package si.um.feri.backgammon.persistence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/** Loads and saves the {@link Leaderboard} in the app's local storage. */
public class LeaderboardStore {
    private static final String TAG = LeaderboardStore.class.getSimpleName();
    public static final String FILE_NAME = "backgammon_leaderboard.json";

    private final FileHandle file;
    private Leaderboard leaderboard;

    public LeaderboardStore(FileHandle file) {
        this.file = file;
    }

    public static LeaderboardStore local() {
        return new LeaderboardStore(Gdx.files.local(FILE_NAME));
    }

    public Leaderboard get() {
        if (leaderboard == null) {
            String text = null;
            try {
                if (file.exists()) text = file.readString("UTF-8");
            } catch (GdxRuntimeException e) {
                Gdx.app.error(TAG, "Could not read " + file.path(), e);
            }
            leaderboard = Leaderboard.fromJson(text);
        }
        return leaderboard;
    }

    public void recordWin(String username, int points) {
        get().recordWin(username, points);
        try {
            file.writeString(leaderboard.toJson(), false, "UTF-8");
        } catch (GdxRuntimeException e) {
            Gdx.app.error(TAG, "Could not save " + file.path(), e);
        }
    }
}
