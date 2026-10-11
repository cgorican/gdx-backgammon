package si.um.feri.backgammon.persistence;

import com.badlogic.gdx.Preferences;

import si.um.feri.backgammon.model.Side;

/** User settings, persisted in libGDX {@link Preferences}. Keys are compatible with earlier versions. */
public class Settings {
    private static final String KEY_INITIAL_SIDE = "initMove";
    private static final String KEY_POINT_NUMBERS = "fieldIndexes";
    private static final String KEY_MUSIC = "music";
    private static final String KEY_SOUND_EFFECTS = "soundEffects";
    private static final String KEY_MOVE_HINTS = "moveHints";
    private static final String KEY_NAME_BRIGHT = "nameBright";
    private static final String KEY_NAME_DARK = "nameDark";

    public static final int MAX_NAME_LENGTH = 16;

    private final Preferences prefs;

    public Settings(Preferences prefs) {
        this.prefs = prefs;
    }

    public Side initialSide() {
        return Side.parse(prefs.getString(KEY_INITIAL_SIDE, Side.BRIGHT.name()), Side.BRIGHT);
    }

    public void setInitialSide(Side side) {
        prefs.putString(KEY_INITIAL_SIDE, side.name()).flush();
    }

    public boolean showPointNumbers() {
        return prefs.getBoolean(KEY_POINT_NUMBERS, false);
    }

    public void setShowPointNumbers(boolean show) {
        prefs.putBoolean(KEY_POINT_NUMBERS, show).flush();
    }

    public boolean musicEnabled() {
        return prefs.getBoolean(KEY_MUSIC, true);
    }

    public void setMusicEnabled(boolean enabled) {
        prefs.putBoolean(KEY_MUSIC, enabled).flush();
    }

    public boolean soundEffectsEnabled() {
        return prefs.getBoolean(KEY_SOUND_EFFECTS, true);
    }

    public void setSoundEffectsEnabled(boolean enabled) {
        // Previously written to the music key by mistake.
        prefs.putBoolean(KEY_SOUND_EFFECTS, enabled).flush();
    }

    /** Whether the movable checkers get a subtle highlight before one is selected. */
    public boolean moveHints() {
        return prefs.getBoolean(KEY_MOVE_HINTS, true);
    }

    public void setMoveHints(boolean enabled) {
        prefs.putBoolean(KEY_MOVE_HINTS, enabled).flush();
    }

    public String playerName(Side side) {
        String fallback = side == Side.BRIGHT ? "Player 2" : "Player 1";
        String name = prefs.getString(side == Side.BRIGHT ? KEY_NAME_BRIGHT : KEY_NAME_DARK, fallback);
        return sanitizeName(name, fallback);
    }

    public void setPlayerName(Side side, String name) {
        String fallback = side == Side.BRIGHT ? "Player 2" : "Player 1";
        prefs.putString(side == Side.BRIGHT ? KEY_NAME_BRIGHT : KEY_NAME_DARK, sanitizeName(name, fallback)).flush();
    }

    public static String sanitizeName(String name, String fallback) {
        String trimmed = name == null ? "" : name.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) return fallback;
        return trimmed.length() > MAX_NAME_LENGTH ? trimmed.substring(0, MAX_NAME_LENGTH) : trimmed;
    }
}
