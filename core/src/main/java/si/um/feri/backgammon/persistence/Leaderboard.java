package si.um.feri.backgammon.persistence;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Wins per player name. Serialized as {@code {"users":[{"username":"...","wins":3,"points":4}]}}, which is also
 * compatible with files written by earlier versions of the game (that had no "points").
 */
public class Leaderboard {
    private static final Comparator<PlayerScore> RANKING = Comparator
            .comparingInt((PlayerScore s) -> -s.wins)
            .thenComparingInt(s -> -s.points)
            .thenComparing(s -> s.username, String.CASE_INSENSITIVE_ORDER);

    public ArrayList<PlayerScore> users = new ArrayList<>();

    public void recordWin(String username, int points) {
        String name = normalize(username);
        PlayerScore score = find(name);
        if (score == null) {
            score = new PlayerScore(name);
            users.add(score);
        }
        score.wins++;
        score.points += points;
        users.sort(RANKING);
    }

    /** Players ordered by wins, then points. Players without wins (old default rows) are left out. */
    public List<PlayerScore> ranking() {
        List<PlayerScore> ranking = new ArrayList<>();
        for (PlayerScore score : users) {
            if (score != null && score.username != null && score.wins > 0) ranking.add(score);
        }
        ranking.sort(RANKING);
        return Collections.unmodifiableList(ranking);
    }

    public PlayerScore find(String username) {
        String name = normalize(username);
        for (PlayerScore score : users) {
            if (score != null && name.equals(score.username)) return score;
        }
        return null;
    }

    static String normalize(String username) {
        String name = username == null ? "" : username.trim();
        return name.isEmpty() ? "Player" : name;
    }

    public String toJson() {
        return json().prettyPrint(this);
    }

    /** Parses saved data; returns an empty leaderboard for missing or corrupt data instead of failing. */
    public static Leaderboard fromJson(String text) {
        if (text == null || text.trim().isEmpty()) return new Leaderboard();
        try {
            Leaderboard leaderboard = json().fromJson(Leaderboard.class, text);
            if (leaderboard == null) return new Leaderboard();
            if (leaderboard.users == null) leaderboard.users = new ArrayList<>();
            leaderboard.users.removeIf(s -> s == null || s.username == null);
            leaderboard.users.sort(RANKING);
            return leaderboard;
        } catch (SerializationException | IllegalArgumentException e) {
            return new Leaderboard();
        }
    }

    private static Json json() {
        Json json = new Json(JsonWriter.OutputType.json);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setElementType(Leaderboard.class, "users", PlayerScore.class);
        return json;
    }
}
