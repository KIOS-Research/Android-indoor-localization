package dev.kios.airplace.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class ErrorsUI extends AbstractUI {
    public static final String TITLE = "Errors";

    public ErrorsUI(Skin skin, Stage stage) {
        super(skin, stage);
    }

    @Override
    public void create() {

        Label lblRSS = UtilsUI.createLabel("RSS: [-]", Color.WHITE, skin);
        Label lblMagnetic = new Label("Magnetic: [-]", skin);
        Label lblFE = new Label("Fused Engine: [-]", skin);
        Label lblBT = new Label("Bluetooth: [-]", skin);
        Label lblPF = new Label("Particle Filter: [-]", skin);

        Table table = new Table();

        table.add(lblRSS).pad(PADDING).fillX();
        table.add(lblMagnetic).pad(PADDING);
        table.row();
        table.add(lblFE).pad(PADDING);
        table.add(lblBT).pad(PADDING);
        table.row();
        table.add(lblPF).pad(PADDING);

        final Window window = UtilsUI.createWindow(TITLE, table, skin, stage);
        Image imgClose = window.findActor("imgClose");
        UtilsUI.addEventListener(imgClose, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                window.remove();
            }
        });

        stage.addActor(window);
    }
}
