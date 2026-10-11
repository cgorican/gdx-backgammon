package si.um.feri.backgammon.screen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import si.um.feri.backgammon.BackgammonGame;
import si.um.feri.backgammon.assets.RegionNames;
import si.um.feri.backgammon.view.Ui;

public class MenuScreen extends BaseScreen {

    public MenuScreen(BackgammonGame game) {
        super(game);
    }

    @Override
    protected void build() {
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.BACKGROUND)));

        Table menu = new Table();
        menu.setBackground(new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.MENU_BACKGROUND)));
        menu.pad(48, 56, 56, 56);
        menu.defaults().width(388).height(Ui.BUTTON_HEIGHT).padBottom(22);

        Table logo = new Table();
        logo.add(new Image(new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.FIGURE_DARK)), Scaling.fit)).size(72);
        logo.add(new Image(new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.FIGURE_BRIGHT)), Scaling.fit)).size(72).padLeft(-18);
        menu.add(logo).height(80).padBottom(6).row();

        Label title = new Label("Backgammon", skin, "title");
        title.setAlignment(Align.center);
        title.setFontScale(0.8f);
        menu.add(title).height(96).padBottom(36).row();

        menu.add(Ui.button("Play", skin, () -> goTo(new GameScreen(game)))).row();
        menu.add(Ui.secondaryButton("Leaderboard", skin, () -> goTo(new LeaderboardScreen(game)))).row();
        menu.add(Ui.secondaryButton("Settings", skin, () -> goTo(new SettingsScreen(game)))).row();
        if (Gdx.app.getType() != Application.ApplicationType.WebGL) {
            menu.add(Ui.secondaryButton("Quit", skin, () -> Gdx.app.exit())).padBottom(0).row();
        }

        root.add(menu).width(500).minHeight(800);
        stage.addActor(root);
    }

    @Override
    protected void onBack() {
        Gdx.app.exit();
    }
}
