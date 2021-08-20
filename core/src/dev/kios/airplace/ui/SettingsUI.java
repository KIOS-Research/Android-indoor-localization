package dev.kios.airplace.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static com.badlogic.gdx.Application.ApplicationType.Desktop;

public class SettingsUI extends AbstractUI {
    public static final String TITLE = "Settings";

    public SettingsUI(Skin skin, Stage stage) {
        super(skin, stage);
    }

    @Override
    public void create() {
        TextButton btnNavigator = UtilsUI.createTextButton("Navigator", skin);
        TextButton btnLogger = UtilsUI.createTextButton("Logger", skin);
        TextButton btnBuilder = UtilsUI.createTextButton("Builder", skin);

        UtilsUI.addEventListener(btnNavigator, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (stage.getRoot().findActor(NavigatorUI.TITLE) == null) {
                    new NavigatorUI(skin, stage);
                }
            }
        });

        UtilsUI.addEventListener(btnLogger, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (stage.getRoot().findActor(LoggerUI.TITLE) == null) {
                    new LoggerUI(skin, stage);
                }
            }
        });

        UtilsUI.addEventListener(btnBuilder, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (stage.getRoot().findActor(BuilderUI.TITLE) == null) {
                    new BuilderUI(skin, stage);
                }
            }
        });

        Table table = new Table();
        table.setName(TITLE);

        table.add(btnNavigator).pad(PADDING).fillX();
        table.row();
        table.add(btnLogger).pad(PADDING).fillX();
        table.row();
        table.add(btnBuilder).pad(PADDING).fillX();

        if (Gdx.app.getType() == Desktop) {
            TextButton btnErrors = UtilsUI.createTextButton("Errors", skin);

            UtilsUI.addEventListener(btnErrors, new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (stage.getRoot().findActor(ErrorsUI.TITLE) == null) {
                        new ErrorsUI(skin, stage);
                    }
                }
            });

            table.row();
            table.add(btnErrors).pad(PADDING).fillX();
        }

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