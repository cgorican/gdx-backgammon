package si.um.feri.backgammon.config;

import si.um.feri.backgammon.view.BoardLayout;

public final class GameConfig {
    /** Virtual world size; every screen uses a FitViewport of this size. */
    public static final float WIDTH = BoardLayout.WORLD_WIDTH;
    public static final float HEIGHT = BoardLayout.WORLD_HEIGHT;

    public static final float MUSIC_VOLUME = 0.6f;
    public static final float SFX_VOLUME = 0.9f;

    private GameConfig() {
    }
}
