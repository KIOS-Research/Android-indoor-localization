package dev.kios.airplace.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import dev.kios.airplace.RadioMap;
import dev.kios.airplace.file.FileChooser;

import java.io.File;
import java.io.FileFilter;

public class BuilderUI extends AbstractUI {
    public static final String TITLE = "Builder";

    public BuilderUI(Skin skin, Stage stage) {
        super(skin, stage);
    }

    @Override
    public void create() {
        TextButton btnRouteFolder = UtilsUI.createTextButton("Routes Folder", skin);
        TextButton btnRadioMapsFolder = UtilsUI.createTextButton("RadioMaps Folder", skin);
        TextButton btnBuild = UtilsUI.createTextButton("Build", skin);

        UtilsUI.addEventListener(btnRouteFolder, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String dir = Gdx.files.getExternalStoragePath();
                if (!preferences.getString("RoutesFolder").equals("")) {
                    dir = preferences.getString("RoutesFolder");
                }

                FileChooser fileChooser = FileChooser.createLoadDialog("Select Routes Folder", skin, Gdx.files.absolute(dir));
                fileChooser.setResultListener(new FileChooser.ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if (success) {
                            if (result.isDirectory()) {
                                preferences.putString("RoutesFolder", result.toString());
                                preferences.flush();
                                return true;
                            } else {
                                Toast.makeToast("Please select a directory", Toast.LENGTH_LONG, skin);
                            }
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
        });

        UtilsUI.addEventListener(btnRadioMapsFolder, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String dir = Gdx.files.getExternalStoragePath();
                if (!preferences.getString("RadioMapsFolder").equals("")) {
                    dir = preferences.getString("RadioMapsFolder");
                }

                FileChooser fileChooser = FileChooser.createLoadDialog("Select RadioMaps Folder", skin, Gdx.files.absolute(dir));
                fileChooser.setResultListener(new FileChooser.ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if (success) {
                            if (result.isDirectory()) {
                                preferences.putString("RadioMapsFolder", result.toString());
                                preferences.flush();
                                return true;
                            } else {
                                Toast.makeToast("Please select a directory", Toast.LENGTH_LONG, skin);
                            }
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
        });

        UtilsUI.addEventListener(btnBuild, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Preferences preferences = Gdx.app.getPreferences("AirPlacePreferences");

                String inputFolderPath = preferences.getString("RoutesFolder");
                String outputFolderPath = preferences.getString("RadioMapsFolder");

                if (inputFolderPath.isEmpty() || outputFolderPath.isEmpty()) {
                    Toast.makeToast("Please select routes and/or radioMap folder(s)", Toast.LENGTH_LONG, skin);
                    return;
                }

                new RadioMap(skin, inputFolderPath, outputFolderPath);
            }
        });

        Table table = new Table();
        table.setName(TITLE);

        table.add(btnRouteFolder).pad(PADDING).fillX();
        table.add(btnRadioMapsFolder).pad(PADDING).fillX();
        table.row();
        table.add(btnBuild).pad(PADDING).fillX().colspan(2);

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
