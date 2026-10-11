package si.um.feri.backgammon.view;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.List;

import si.um.feri.backgammon.assets.RegionNames;
import si.um.feri.backgammon.model.DiceRoll;
import si.um.feri.backgammon.model.Side;

/**
 * The current roll, shown in the middle of the roller's half of the board. All dice stay visible: unused dice are
 * bright, used dice are dimmed. Doubles show four dice.
 */
public class DiceTray extends Group {
    private static final float DIE_SIZE = 76f;
    private static final float GAP = 16f;
    private static final float PADDING = 14f;

    private final TextureAtlas atlas;
    private final Skin skin;
    private final List<Image> dice = new ArrayList<>();
    private DiceRoll roll;

    public DiceTray(TextureAtlas atlas, Skin skin) {
        this.atlas = atlas;
        this.skin = skin;
        setTouchable(Touchable.disabled);
    }

    public void show(DiceRoll roll, Side side, boolean animate) {
        clearChildren();
        dice.clear();
        this.roll = roll;
        int count = roll.size();
        float width = count * DIE_SIZE + (count - 1) * GAP + 2 * PADDING;
        float height = DIE_SIZE + 2 * PADDING;
        Vector2 center = BoardLayout.diceCenter(side);
        setBounds(center.x - width / 2f, center.y - height / 2f, width, height);

        Image backdrop = new Image(skin.getDrawable(RegionNames.PANEL));
        backdrop.setSize(width, height);
        addActor(backdrop);

        for (int i = 0; i < count; i++) {
            Image die = new Image(atlas.findRegion(RegionNames.dice(roll.value(i))));
            die.setSize(DIE_SIZE, DIE_SIZE);
            die.setOrigin(Align.center);
            die.setPosition(PADDING + i * (DIE_SIZE + GAP), PADDING);
            if (animate) {
                die.setScale(0.4f);
                die.setRotation(MathUtils.random(-200f, 200f));
                die.addAction(Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.35f, Interpolation.swingOut),
                        Actions.rotateTo(0f, 0.35f, Interpolation.pow2Out)));
            }
            addActor(die);
            dice.add(die);
        }
        setVisible(true);
        refresh();
    }

    /** Updates the used / unused look after a move. */
    public void refresh() {
        if (roll == null) return;
        for (int i = 0; i < dice.size(); i++) {
            Image die = dice.get(i);
            if (roll.isUsed(i)) die.setColor(0.55f, 0.55f, 0.55f, 0.4f);
            else die.setColor(1f, 1f, 1f, 1f);
        }
    }

    public void hide() {
        roll = null;
        setVisible(false);
    }
}
