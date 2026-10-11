package si.um.feri.backgammon;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import si.um.feri.backgammon.assets.AssetDescriptors;
import si.um.feri.backgammon.audio.GameAudio;
import si.um.feri.backgammon.persistence.LeaderboardStore;
import si.um.feri.backgammon.persistence.Settings;
import si.um.feri.backgammon.screen.IntroScreen;

/** Application entry point shared by all platforms. Owns the long-lived services used by the screens. */
public class BackgammonGame extends Game {
    private AssetManager assetManager;
    private SpriteBatch batch;
    private Settings settings;
    private LeaderboardStore leaderboard;
    private GameAudio audio;

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);
        Gdx.input.setCatchKey(Input.Keys.BACK, true);

        assetManager = new AssetManager();
        assetManager.load(AssetDescriptors.UI_SKIN);
        assetManager.load(AssetDescriptors.GAMEPLAY);
        assetManager.load(AssetDescriptors.MUSIC);
        assetManager.load(AssetDescriptors.SFX_CHECKER);
        assetManager.load(AssetDescriptors.SFX_DICE);
        assetManager.finishLoading();
        // The 1920x1080 world is scaled to the window; linear filtering keeps the art smooth when scaled.
        for (Texture texture : assetManager.get(AssetDescriptors.GAMEPLAY).getTextures()) {
            texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        batch = new SpriteBatch();
        Preferences preferences = Gdx.app.getPreferences(BackgammonGame.class.getSimpleName());
        settings = new Settings(preferences);
        leaderboard = new LeaderboardStore(preferences, Gdx.files.local(LeaderboardStore.LEGACY_FILE_NAME));
        audio = new GameAudio(assetManager, settings);
        audio.updateMusic();

        setScreen(new IntroScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        assetManager.dispose();
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public Settings getSettings() {
        return settings;
    }

    public LeaderboardStore getLeaderboard() {
        return leaderboard;
    }

    public GameAudio getAudio() {
        return audio;
    }

    public TextureAtlas getGameplayAtlas() {
        return assetManager.get(AssetDescriptors.GAMEPLAY);
    }
}
