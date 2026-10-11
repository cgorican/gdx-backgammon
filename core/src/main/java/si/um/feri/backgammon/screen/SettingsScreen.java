package si.um.feri.backgammon.screen;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import java.util.function.Consumer;

import si.um.feri.backgammon.BackgammonGame;
import si.um.feri.backgammon.assets.RegionNames;
import si.um.feri.backgammon.model.Side;
import si.um.feri.backgammon.persistence.Settings;
import si.um.feri.backgammon.view.Ui;

public class SettingsScreen extends BaseScreen {
    private final Settings settings;
    private TextField darkName;
    private TextField brightName;

    public SettingsScreen(BackgammonGame game) {
        super(game);
        settings = game.getSettings();
    }

    @Override
    protected void build() {
        Table root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.BACKGROUND)));

        Table panel = new Table();
        panel.setBackground(skin.getDrawable(RegionNames.PANEL));
        panel.pad(40, 64, 44, 64);
        panel.defaults().left().padBottom(18);

        Label title = new Label("Settings", skin, "title");
        panel.add(title).colspan(3).center().padBottom(32).row();

        // Player names
        darkName = nameField(Side.DARK);
        brightName = nameField(Side.BRIGHT);
        panel.add(new Label("Players", skin, "gold")).colspan(3).row();
        addNameRow(panel, Side.DARK, darkName);
        addNameRow(panel, Side.BRIGHT, brightName);

        // Who starts
        CheckBox startBright = new CheckBox(" Bright", skin);
        CheckBox startDark = new CheckBox(" Dark", skin);
        ButtonGroup<CheckBox> startGroup = new ButtonGroup<>(startDark, startBright);
        startGroup.setMaxCheckCount(1);
        startGroup.setMinCheckCount(1);
        (settings.initialSide() == Side.BRIGHT ? startBright : startDark).setChecked(true);
        ChangeListener startListener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                settings.setInitialSide(startBright.isChecked() ? Side.BRIGHT : Side.DARK);
            }
        };
        startBright.addListener(startListener);
        startDark.addListener(startListener);
        panel.add(new Label("First move", skin, "gold")).padTop(14).colspan(3).row();
        Table startRow = new Table();
        startRow.add(startDark).padRight(48);
        startRow.add(startBright);
        panel.add(startRow).colspan(3).padLeft(8).row();

        // Toggles
        panel.add(new Label("Game", skin, "gold")).padTop(14).colspan(3).row();
        panel.add(toggle("Highlight checkers that can move", settings.moveHints(), settings::setMoveHints))
                .colspan(3).padLeft(8).row();
        panel.add(toggle("Show point numbers", settings.showPointNumbers(), settings::setShowPointNumbers))
                .colspan(3).padLeft(8).row();
        Table sound = new Table();
        sound.add(toggle("Music", settings.musicEnabled(), enabled -> {
            settings.setMusicEnabled(enabled);
            game.getAudio().updateMusic();
        })).padRight(48);
        sound.add(toggle("Sound effects", settings.soundEffectsEnabled(), settings::setSoundEffectsEnabled));
        panel.add(sound).colspan(3).padLeft(8).row();

        panel.add(Ui.button("Back", skin, this::saveAndLeave)).colspan(3).center().width(388).height(Ui.BUTTON_HEIGHT)
                .padTop(28).padBottom(0).row();

        root.add(panel).width(900);
        stage.addActor(root);
    }

    private TextField nameField(Side side) {
        TextField field = new TextField(settings.playerName(side), skin);
        field.setMaxLength(Settings.MAX_NAME_LENGTH);
        field.setMessageText(side.displayName() + " player");
        field.setTextFieldListener((textField, c) -> {
            if (c == '\r' || c == '\n') stage.unfocus(textField);
        });
        return field;
    }

    private void addNameRow(Table panel, Side side, TextField field) {
        Image checker = new Image(new TextureRegionDrawable(gameplayAtlas.findRegion(RegionNames.checker(side))), Scaling.fit);
        panel.add(checker).size(56).padLeft(8).padRight(16);
        Label label = new Label(side.displayName(), skin);
        label.setAlignment(Align.left);
        panel.add(label).width(150);
        panel.add(field).width(480).height(72).row();
    }

    private CheckBox toggle(String text, boolean checked, Consumer<Boolean> onChange) {
        CheckBox box = new CheckBox(" " + text, skin);
        box.setChecked(checked);
        box.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onChange.accept(box.isChecked());
            }
        });
        return box;
    }

    private void saveAndLeave() {
        settings.setPlayerName(Side.DARK, darkName.getText());
        settings.setPlayerName(Side.BRIGHT, brightName.getText());
        goTo(new MenuScreen(game));
    }

    @Override
    protected void onBack() {
        saveAndLeave();
    }
}
