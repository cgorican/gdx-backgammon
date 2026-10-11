package si.um.feri.backgammon.view;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import si.um.feri.backgammon.assets.RegionNames;
import si.um.feri.backgammon.model.Board;
import si.um.feri.backgammon.model.Side;

/** A player's card in the left tray: name, whose turn it is, what to do next, pip count and checkers borne off. */
public class PlayerPanel extends Table {
    public static final float WIDTH = 246f;
    public static final float HEIGHT = 410f;

    private final Skin skin;
    private final Label turnBadge;
    private final Table badgeHolder;
    private final Label status;
    private final Label pips;
    private final Label off;

    public PlayerPanel(TextureAtlas atlas, Skin skin, Side side, String name) {
        this.skin = skin;
        setTouchable(Touchable.disabled);
        setSize(WIDTH, HEIGHT);
        pad(22, 16, 22, 16);
        top();

        Image checker = new Image(new TextureRegionDrawable(atlas.findRegion(RegionNames.checker(side))), Scaling.fit);
        add(checker).size(60).padBottom(6).row();
        Label nameLabel = new Label(name, skin, "bold");
        nameLabel.setAlignment(Align.center);
        nameLabel.setEllipsis(true);
        add(nameLabel).width(WIDTH - 32).padBottom(10).row();

        turnBadge = new Label("", skin, "ink");
        badgeHolder = new Table();
        badgeHolder.add(turnBadge);
        add(badgeHolder).height(46).padBottom(14).row();

        status = new Label("", skin, "small");
        status.setAlignment(Align.center);
        status.setWrap(true);
        add(status).width(WIDTH - 32).height(88).row();

        pips = new Label("", skin, "small");
        off = new Label("", skin, "small");
        add(pips).row();
        add(off).row();
    }

    /** @param badge text of the pill under the name, e.g. "YOUR TURN" (highlighted when {@code active}) */
    public void update(Board board, Side side, boolean active, String badge, String statusText) {
        setBackground(skin.getDrawable(active ? RegionNames.PANEL_ACTIVE : RegionNames.PANEL));
        badgeHolder.setBackground(active ? skin.newDrawable(RegionNames.PILL, Ui.GOLD)
                : skin.newDrawable(RegionNames.PILL, 1f, 1f, 1f, 0.12f));
        turnBadge.setText(badge);
        turnBadge.setStyle(skin.get(active ? "ink" : "small", Label.LabelStyle.class));
        status.setText(statusText);
        status.setStyle(skin.get(active ? "small-cream" : "small", Label.LabelStyle.class));
        pips.setText("Pips  " + board.pipCount(side));
        off.setText("Off  " + board.offCount(side) + " / " + Board.CHECKERS_PER_SIDE);
        getColor().a = active ? 1f : 0.72f;
    }
}
