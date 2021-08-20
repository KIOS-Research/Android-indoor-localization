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
import com.badlogic.gdx.utils.Align;
import dev.kios.airplace.file.FileChooser;

import java.io.File;
import java.io.FileFilter;

public class NavigatorUI extends AbstractUI {
    public static final String TITLE = "Navigator";

    public NavigatorUI(Skin skin, Stage stage) {
        super(skin, stage);
    }

    @Override
    public void create() {
        //Create Widgets
        Label lblAlgorithm = UtilsUI.createLabel("Algorithm", Color.WHITE, skin);

        final SelectBox<String> selectBox = new SelectBox<>(skin);
        selectBox.setItems("KNN", "WKNN", "MAP", "MMSE");
        selectBox.setAlignment(Align.left);

        final CheckBox cbRSS = UtilsUI.createCheckBox("RSS", skin);
        final CheckBox cbMagnetic = UtilsUI.createCheckBox("Magnetic", skin);

        final CheckBox cbFused = UtilsUI.createCheckBox("Fused Engine", skin);
        UtilsUI.addEventListener(cbFused, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectBox.getSelected().equals("KNN")) {
                    cbFused.setChecked(false);
                    Toast.makeToast("Fused Engine is not available for KNN", Toast.LENGTH_SHORT, skin);
                }
            }
        });

        final CheckBox cbBluetooth = UtilsUI.createCheckBox("Bluetooth", skin);
        UtilsUI.addEventListener(cbBluetooth, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cbBluetooth.setChecked(false);
                Toast.makeToast("TODO", Toast.LENGTH_SHORT, skin);
            }
        });

        final CheckBox cbParticleFilter = UtilsUI.createCheckBox("Particle Filter", skin);
        UtilsUI.addEventListener(cbParticleFilter, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectBox.getSelected().equals("KNN")) {
                    cbParticleFilter.setChecked(false);
                    Toast.makeToast("Particle Filter is not available for KNN", Toast.LENGTH_SHORT, skin);
                }
            }
        });

        TextButton btnSettings = UtilsUI.createTextButton("Settings", skin);
        UtilsUI.addEventListener(btnSettings, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (stage.getRoot().findActor(PFSettingsUI.TITLE) == null) {
                    new PFSettingsUI(skin, stage);
                }
            }
        });

        final CheckBox cbRadioMapPoints = UtilsUI.createCheckBox("RadioMap Points", skin);

        final Label lblTotalPoints = UtilsUI.createLabel("Total Points: [-]", Color.WHITE, skin);

        final Label lblRadioMaps = UtilsUI.createLabel("Radio Maps", Color.WHITE, skin);
        lblRadioMaps.setAlignment(Align.center);

        TextButton btnRssRadioMapFile = UtilsUI.createTextButton("Rss", skin);
        UtilsUI.addEventListener(btnRssRadioMapFile, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String dir = Gdx.files.getExternalStoragePath();
                if (!preferences.getString("rssRadioMapFile").equals("")) {
                    dir = preferences.getString("rssRadioMapFile");
                }

                FileChooser fileChooser = FileChooser.createPickDialog("Select RadioMap File", skin, Gdx.files.absolute(dir));
                fileChooser.setResultListener(new FileChooser.ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if (success) {
                            if (result.isDirectory())
                                return false;

                            //TODO: Update points

                            preferences.putString("rssRadioMapFile", result.toString());
                            preferences.flush();
                            return true;
                        }
                        return false;
                    }
                });
                fileChooser.setFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (!file.isHidden()) {
                            if (file.isDirectory())
                                return true;
                            else
                                return file.getName().endsWith(".rrm");
                        }
                        return false;
                    }
                });
                fileChooser.show(stage);
            }
        });

        TextButton btnMagneticRadioMapFile = UtilsUI.createTextButton("Magnetic", skin);
        UtilsUI.addEventListener(btnMagneticRadioMapFile, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String dir = Gdx.files.getExternalStoragePath();
                if (!preferences.getString("magneticRadioMapFile").equals("")) {
                    dir = preferences.getString("magneticRadioMapFile");
                }

                FileChooser fileChooser = FileChooser.createPickDialog("Select RadioMap File", skin, Gdx.files.absolute(dir));
                fileChooser.setResultListener(new FileChooser.ResultListener() {
                    @Override
                    public boolean result(boolean success, FileHandle result) {
                        if (success) {
                            if (result.isDirectory())
                                return false;

                            //TODO: Update points

                            preferences.putString("magneticRadioMapFile", result.toString());
                            preferences.flush();
                            return true;
                        }
                        return false;
                    }
                });
                fileChooser.setFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (!file.isHidden()) {
                            if (file.isDirectory())
                                return true;
                            else
                                return file.getName().endsWith(".mrm");
                        }
                        return false;
                    }
                });
                fileChooser.show(stage);
            }
        });

        UtilsUI.addEventListener(selectBox, new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (selectBox.getSelected().equals("KNN")) {
                    cbFused.setChecked(false);
                    cbParticleFilter.setChecked(false);
                }
            }
        });

        //Load Preferences
        selectBox.setSelectedIndex(preferences.getInteger("Algorithm", 1));

        cbRSS.setChecked(preferences.getBoolean("RSS", true));
        cbMagnetic.setChecked(preferences.getBoolean("Magnetic", true));
        cbFused.setChecked(preferences.getBoolean("Fused", false));
        cbBluetooth.setChecked(preferences.getBoolean("Bluetooth", false));
        cbParticleFilter.setChecked(preferences.getBoolean("ParticleFilter", false));
        cbRadioMapPoints.setChecked(preferences.getBoolean("RadioMapPoints", false));

        //Crete UI
        Table table = new Table();
        table.setName(TITLE);

        table.add(lblAlgorithm).pad(PADDING).fillX();
        table.add(selectBox).pad(PADDING).fillX();
        table.row();
        table.add(cbRSS).pad(PADDING).align(Align.left);
        table.add(cbMagnetic).pad(PADDING).align(Align.left);
        table.row();
        table.add(cbFused).pad(PADDING).align(Align.left);
        table.add(cbBluetooth).pad(PADDING).align(Align.left);
        table.row();
        table.add(cbParticleFilter).pad(PADDING).align(Align.left);
        table.add(btnSettings).pad(PADDING);
        table.row();
        table.add(cbRadioMapPoints).pad(PADDING).align(Align.left);
        table.add(lblTotalPoints).pad(PADDING).fillX();
        table.row();
        table.add(lblRadioMaps).pad(PADDING).fillX().colspan(2);
        table.row();
        table.add(btnRssRadioMapFile).pad(PADDING).fillX();
        table.add(btnMagneticRadioMapFile).pad(PADDING).fillX();

        //Save
        final Window window = UtilsUI.createWindow(TITLE, table, skin, stage);
        Image imgClose = window.findActor("imgClose");
        UtilsUI.addEventListener(imgClose, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                preferences.putInteger("Algorithm", selectBox.getSelectedIndex());
                preferences.putBoolean("RSS", cbRSS.isChecked());
                preferences.putBoolean("Magnetic", cbMagnetic.isChecked());
                preferences.putBoolean("Fused", cbFused.isChecked());
                preferences.putBoolean("Bluetooth", cbBluetooth.isChecked());
                preferences.putBoolean("ParticleFilter", cbParticleFilter.isChecked());
                preferences.putBoolean("RadioMapPoints", cbRadioMapPoints.isChecked());

                preferences.flush();
                window.remove();
            }
        });
        stage.addActor(window);
    }
}
