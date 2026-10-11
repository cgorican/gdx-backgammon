package si.um.feri.backgammon.audio;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import si.um.feri.backgammon.assets.AssetDescriptors;
import si.um.feri.backgammon.config.GameConfig;
import si.um.feri.backgammon.persistence.Settings;

/** Music and sound effects, honouring the user's settings. Assets are owned by the {@link AssetManager}. */
public class GameAudio {
    private final AssetManager assets;
    private final Settings settings;

    public GameAudio(AssetManager assets, Settings settings) {
        this.assets = assets;
        this.settings = settings;
    }

    /** Starts or stops the background music according to the settings. */
    public void updateMusic() {
        Music music = assets.get(AssetDescriptors.MUSIC);
        music.setLooping(true);
        music.setVolume(GameConfig.MUSIC_VOLUME);
        if (settings.musicEnabled() && !music.isPlaying()) music.play();
        else if (!settings.musicEnabled() && music.isPlaying()) music.stop();
    }

    public void checker() {
        play(assets.get(AssetDescriptors.SFX_CHECKER));
    }

    public void dice() {
        play(assets.get(AssetDescriptors.SFX_DICE));
    }

    private void play(Sound sound) {
        if (settings.soundEffectsEnabled()) sound.play(GameConfig.SFX_VOLUME);
    }
}
