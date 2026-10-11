package si.um.feri.backgammon.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import si.um.feri.backgammon.BackgammonGame;
import si.um.feri.backgammon.assets.RegionNames;

/** Short animated intro: the points slide away and a checker spins in. Click or press any key to skip. */
public class IntroScreen extends BaseScreen {
    public static final float INTRO_DURATION = 4.5f;

    private float elapsed;
    private boolean leaving;

    public IntroScreen(BackgammonGame game) {
        super(game);
    }

    @Override
    protected void build() {
        Image background = new Image(gameplayAtlas.findRegion(RegionNames.BACKGROUND));
        background.setFillParent(true);
        stage.addActor(background);
        addSpikes();
        stage.addActor(createFigure());
        stage.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                leave();
            }
        });
    }

    @Override
    protected boolean onKey(int keycode) {
        leave();
        return true;
    }

    @Override
    protected void onBack() {
        leave();
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        super.render(delta);
        if (elapsed > INTRO_DURATION) leave();
    }

    private void leave() {
        if (leaving) return;
        leaving = true;
        goTo(new MenuScreen(game));
    }

    private Actor createFigure() {
        Image figure = new Image(gameplayAtlas.findRegion(RegionNames.FIGURE_BRIGHT));
        float centerX = viewport.getWorldWidth() / 2f - figure.getWidth() / 2f;
        float centerY = viewport.getWorldHeight() / 2f - figure.getHeight() / 2f;
        figure.setOrigin(Align.center);
        figure.setPosition(centerX, -figure.getHeight());
        figure.addAction(Actions.sequence(
                Actions.delay(2.2f),
                Actions.parallel(Actions.rotateBy(360, 1f), Actions.moveTo(centerX, centerY, 1f)),
                Actions.fadeOut(1.2f),
                Actions.removeActor()));
        return figure;
    }

    private void addSpikes() {
        float spikeWidth = gameplayAtlas.findRegion(RegionNames.SPIKE_INTRO_DARK).getRegionWidth();
        int count = (int) Math.ceil(viewport.getWorldWidth() / spikeWidth) + 1;
        for (int i = 0; i < count; i++) {
            stage.addActor(createSpike(i, true));
            stage.addActor(createSpike(i, false));
        }
    }

    private Actor createSpike(int index, boolean top) {
        boolean bright = (index % 2 == 0) != top;
        Image spike = new Image(gameplayAtlas.findRegion(bright ? RegionNames.SPIKE_INTRO_BRIGHT : RegionNames.SPIKE_INTRO_DARK));
        spike.setOrigin(Align.center);
        float x = index * spike.getWidth() - (top ? 0 : spike.getWidth() / 2f);
        float y = top ? viewport.getWorldHeight() - spike.getHeight() : 0;
        float exitY = top ? viewport.getWorldHeight() : -spike.getHeight();
        if (top) spike.setRotation(180);
        spike.setPosition(x, y);
        spike.addAction(Actions.sequence(Actions.delay(1.8f), Actions.moveTo(x, exitY, 1.5f), Actions.removeActor()));
        return spike;
    }
}
