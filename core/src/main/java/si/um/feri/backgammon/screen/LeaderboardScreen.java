package si.um.feri.backgammon.screen;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.util.List;

import si.um.feri.backgammon.BackgammonGame;
import si.um.feri.backgammon.assets.RegionNames;
import si.um.feri.backgammon.persistence.PlayerScore;
import si.um.feri.backgammon.view.Ui;

public class LeaderboardScreen extends BaseScreen {

    public LeaderboardScreen(BackgammonGame game) {
        super(game);
    }

    @Override
    protected void build() {
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.BACKGROUND)));

        Table panel = new Table();
        panel.setBackground(skin.getDrawable(RegionNames.PANEL));
        panel.pad(40, 64, 44, 64);

        panel.add(new Label("Leaderboard", skin, "title")).padBottom(28).row();

        Table rows = new Table();
        rows.top();
        rows.defaults().padBottom(10);
        List<PlayerScore> ranking = game.getLeaderboard().get().ranking();
        if (ranking.isEmpty()) {
            Label empty = new Label("No games played yet.\nWin a game to get on the board!", skin, "small");
            empty.setAlignment(Align.center);
            rows.add(empty).pad(40);
        } else {
            rows.add(header("#")).width(70);
            Label playerHeader = header("Player");
            playerHeader.setAlignment(Align.left);
            rows.add(playerHeader).width(420).left();
            rows.add(header("Wins")).width(140);
            rows.add(header("Points")).width(140).row();
            int rank = 1;
            for (PlayerScore score : ranking) {
                String style = rank == 1 ? "gold" : "default";
                rows.add(cell(String.valueOf(rank), style, Align.center)).width(70);
                rows.add(cell(score.username, style, Align.left)).width(420).left();
                rows.add(cell(String.valueOf(score.wins), style, Align.center)).width(140);
                rows.add(cell(String.valueOf(score.points), style, Align.center)).width(140).row();
                rank++;
            }
        }
        Table content = new Table();
        content.top();
        content.add(rows).expandX().fillX().top();
        ScrollPane scroll = new ScrollPane(content, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        panel.add(scroll).height(520).width(800).row();

        Label hint = new Label("Points: 1 per win, 2 for a gammon, 3 for a backgammon.", skin, "small");
        panel.add(hint).padTop(12).row();

        panel.add(Ui.button("Back", skin, () -> goTo(new MenuScreen(game)))).width(388).height(Ui.BUTTON_HEIGHT).padTop(24);

        root.add(panel);
        stage.addActor(root);
        stage.setScrollFocus(scroll);
    }

    private Label header(String text) {
        Label label = new Label(text, skin, "small");
        label.setAlignment(Align.center);
        return label;
    }

    private Label cell(String text, String style, int align) {
        Label label = new Label(text, skin, style);
        label.setAlignment(align);
        label.setEllipsis(true);
        return label;
    }

    @Override
    protected void onBack() {
        goTo(new MenuScreen(game));
    }
}
