package si.um.feri.backgammon.assets;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public final class AssetDescriptors {
    public static final AssetDescriptor<Skin> UI_SKIN = new AssetDescriptor<>(AssetPaths.UI_SKIN, Skin.class);
    public static final AssetDescriptor<TextureAtlas> GAMEPLAY =
            new AssetDescriptor<>(AssetPaths.GAMEPLAY, TextureAtlas.class);
    public static final AssetDescriptor<Music> MUSIC = new AssetDescriptor<>(AssetPaths.MUSIC, Music.class);
    public static final AssetDescriptor<Sound> SFX_CHECKER = new AssetDescriptor<>(AssetPaths.SFX_CHECKER, Sound.class);
    public static final AssetDescriptor<Sound> SFX_DICE = new AssetDescriptor<>(AssetPaths.SFX_DICE, Sound.class);

    private AssetDescriptors() {
    }
}
