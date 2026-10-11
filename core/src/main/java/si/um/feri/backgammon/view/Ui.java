package si.um.feri.backgammon.view;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

/** Small helpers and shared colours for building the scene2d UI. */
public final class Ui {
    public static final Color GOLD = Color.valueOf("E9B44C");
    /** Highlight for a destination reached with a single die. */
    public static final Color SINGLE_MOVE = Color.valueOf("6BD66B");
    /** Highlight for a destination reached by combining dice. */
    public static final Color COMBINED_MOVE = Color.valueOf("5AB8F0");
    /** Highlight for a destination that hits an opposing blot. */
    public static final Color HIT = Color.valueOf("F0624F");

    public static final float BUTTON_HEIGHT = 88f;

    private Ui() {
    }

    /** A primary (gold) button. */
    public static TextButton button(String text, Skin skin, Runnable action) {
        return withAction(new TextButton(text, skin), action);
    }

    /** A secondary (dark) button. */
    public static TextButton secondaryButton(String text, Skin skin, Runnable action) {
        return withAction(new TextButton(text, skin, "secondary"), action);
    }

    private static TextButton withAction(TextButton button, Runnable action) {
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                action.run();
            }
        });
        return button;
    }
}
