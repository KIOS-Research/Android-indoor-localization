package dev.kios.airplace.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kios.airplace.file.FileChooser;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;

import static com.badlogic.gdx.Application.ApplicationType.Android;
import static com.badlogic.gdx.Application.ApplicationType.Desktop;

public class LoggerUI extends AbstractUI {
    public static final String TITLE = "Logger";

    public LoggerUI(Skin skin, Stage stage) {
        super(skin, stage);
    }

    @Override
    public void create() {
        int walkingSensitivity = preferences.getInteger("WalkingSensitivity", 26);
        final Label lblWalkingSensitivity = UtilsUI.createLabel(String.format(Locale.getDefault(),"Walking Sensitivity [%d]", walkingSensitivity), Color.WHITE, skin);
        final Slider slider = new Slider(0.0f, 30.0f, 1.0f, false, skin, "default-horizontal");

        TextButton btnRouteFile = UtilsUI.createTextButton("Route File", skin);
        UtilsUI.addEventListener(btnRouteFile, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String dir = Gdx.files.getExternalStoragePath();
                if (!preferences.getString("RouteFile").isEmpty()) {
                    dir = preferences.getString("RouteFile");
                }

                if (Gdx.app.getType() == Desktop) {
                    FileChooser fileChooser = FileChooser.createPickDialog("Select Route File", skin, Gdx.files.absolute(dir));
                    fileChooser.setResultListener(new FileChooser.ResultListener() {
                        @Override
                        public boolean result(boolean success, FileHandle result) {
                            if (success) {
                                preferences.putString("RouteFile", result.toString());
                                preferences.flush();
                                return true;
                            }
                            return false;
                        }
                    });
                    fileChooser.setFilter(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            if (file.isDirectory()) {
                                return true;
                            } else {
                                return file.getName().endsWith(".rf");
                            }
                        }
                    });
                    fileChooser.show(stage);
                } else if (Gdx.app.getType() == Android) {
                    FileChooser fileChooser = FileChooser.createSaveDialog("Save Route File", skin, Gdx.files.absolute(dir));
                    fileChooser.setResultListener(new FileChooser.ResultListener() {
                        @Override
                        public boolean result(boolean success, FileHandle result) {
                            if (success) {
                                preferences.putString("RouteFile", result.toString());
                                preferences.flush();
                                return true;
                            }
                            return false;
                        }
                    });
                    fileChooser.setFilter(new FileFilter() {
                        @Override
                        public boolean accept(File file) {
                            return true;
                        }
                    });
                    fileChooser.show(stage);
                }
            }
        });

        UtilsUI.addEventListener(slider, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                lblWalkingSensitivity.setText(String.format(Locale.getDefault(), "Walking Sensitivity [%d]", (int) slider.getValue()));
                preferences.putInteger("WalkingSensitivity", (int) slider.getValue());
                preferences.flush();
            }
        });

        slider.setValue(walkingSensitivity);

        Table table = new Table();
        table.add(lblWalkingSensitivity).pad(PADDING).fillX();
        table.row();
        table.add(slider).pad(PADDING).fillX();
        table.row();
        table.add(btnRouteFile).pad(PADDING).fillX();

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
