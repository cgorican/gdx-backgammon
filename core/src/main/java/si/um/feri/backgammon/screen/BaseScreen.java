package si.um.feri.backgammon.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import si.um.feri.backgammon.BackgammonGame;
import si.um.feri.backgammon.assets.AssetDescriptors;
import si.um.feri.backgammon.config.GameConfig;

/**
 * Common screen plumbing: one stage on a 1920x1080 {@link FitViewport}, the shared skin and atlas, and
 * Escape / Android back handling via {@link #onBack()}. Screens are disposed when hidden.
 */
public abstract class BaseScreen extends ScreenAdapter {
    protected final BackgammonGame game;
    protected final Skin skin;
    protected final TextureAtlas gameplayAtlas;
    protected Viewport viewport;
    protected Stage stage;

    protected BaseScreen(BackgammonGame game) {
        this.game = game;
        this.skin = game.getAssetManager().get(AssetDescriptors.UI_SKIN);
        this.gameplayAtlas = game.getAssetManager().get(AssetDescriptors.GAMEPLAY);
    }

    @Override
    public final void show() {
        viewport = new FitViewport(GameConfig.WIDTH, GameConfig.HEIGHT);
        stage = new Stage(viewport, game.getBatch());
        build();
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK) {
                    onBack();
                    return true;
                }
                return onKey(keycode);
            }
        }));
    }

    /**
     * Switches screens after the current frame. Calling {@code game.setScreen} directly from an input handler would
     * dispose this screen's stage while it is still dispatching the event.
     */
    protected void goTo(Screen next) {
        Gdx.app.postRunnable(() -> {
            if (game.getScreen() == this) game.setScreen(next);
        });
    }

    /** Adds this screen's actors to {@link #stage}. */
    protected abstract void build();

    /** Escape / Android back. */
    protected void onBack() {
    }

    /** Other key presses not consumed by the stage. */
    protected boolean onKey(int keycode) {
        return false;
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        stage.act(Math.min(delta, 1 / 10f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
            stage = null;
        }
    }
}
