package dev.kios.airplace.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import java.util.Locale;

import static com.badlogic.gdx.Application.ApplicationType.Desktop;

public class MainUI extends AbstractUI {
    private static int pointCounter;
    private static Vector2 prePoint;
    private static Label lblDistance;
    private static Label lblFirstPoint;
    private static Label lblSecondPoint;

    private static Label lblAP;
    private static Label lblStatus;
    private static Label lblHeading;

    private static Label lblSamples;
    public static boolean toggleLogger;
    public static TextButton btnLogger;

    public MainUI(Skin skin, Stage stage) {
        super(skin, stage);
    }

    @Override
    public void create() {
        lblDistance = UtilsUI.createLabel("Distance: [-]", Color.BLACK, skin);
        lblFirstPoint = UtilsUI.createLabel("x1: [-] | y1: [-]", Color.BLACK, skin);
        lblSecondPoint = UtilsUI.createLabel("x2: [-] | y2: [-]", Color.BLACK, skin);

        toggleLogger = false;
        btnLogger = UtilsUI.createTextButton("Start", skin);
        lblSamples = UtilsUI.createLabel("Samples: [-]", Color.BLACK, skin);

        UtilsUI.addEventListener(btnLogger, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (Gdx.app.getType() == Desktop && !toggleLogger) {
                    Preferences preferences = Gdx.app.getPreferences("AirPlacePreferences");
                    if (preferences.getString("RouteFile").equals("")) {
                        Toast.makeToast("Please select a route file", Toast.LENGTH_LONG, skin);
                    } else {
                        toggleLogger = true;
                        btnLogger.setText("Stop");
                    }
                } else { // Stop logging
                    toggleLogger = false;
                    btnLogger.setText("Start");
                }
            }
        });

        Table table = new Table();
        table.align(Align.bottomLeft);
        table.setFillParent(true);

        table.add(lblDistance).pad(PADDING).fillX();
        table.row();
        table.add(lblFirstPoint).pad(PADDING).fillX();
        table.row();
        table.add(lblSecondPoint).pad(PADDING).fillX();
        table.row();

        Table table1 = new Table();
        table1.add(btnLogger).pad(PADDING);
        table1.add(lblSamples).pad(PADDING);

        table.add(table1);
        stage.addActor(table);

        Image imgDown = UtilsUI.createImage("spinner-down", skin);
        Label lblFloor = UtilsUI.createLabel("Floor: [-]", Color.BLACK, skin);
        Image imgUp = UtilsUI.createImage("spinner-up", skin);

        UtilsUI.addEventListener(imgDown, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Toast.makeToast("TODO", Toast.LENGTH_SHORT, skin);
            }
        });

        UtilsUI.addEventListener(imgUp, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Toast.makeToast("TODO", Toast.LENGTH_SHORT, skin);
            }
        });

        table = new Table();
        table.align(Align.bottomRight);
        table.setFillParent(true);

        table.add(imgDown).pad(PADDING).fill(true);
        table.add(lblFloor).pad(PADDING).fillX();
        table.add(imgUp).pad(PADDING).fill(true);

        stage.addActor(table);

        Label lblBT = UtilsUI.createLabel("BT: [-]", Color.BLACK, skin);
        lblAP = UtilsUI.createLabel("AP: [-]", Color.BLACK, skin);
        lblStatus = UtilsUI.createLabel("Status: [-]", Color.BLACK, skin);
        lblHeading = UtilsUI.createLabel("Heading: [-]", Color.BLACK, skin);

        table = new Table();
        table.align(Align.topLeft);
        table.setFillParent(true);

        table.add(lblBT).pad(PADDING).fillX();
        table.row();
        table.add(lblAP).pad(PADDING).fillX();
        table.row();
        table.add(lblStatus).pad(PADDING).fillX();
        table.row();
        table.add(lblHeading).pad(PADDING).fillX();

        stage.addActor(table);

        Image imgSettings = UtilsUI.createImage("icon-settings", skin);

        UtilsUI.addEventListener(imgSettings, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (stage.getRoot().findActor(SettingsUI.TITLE) == null) {
                    new SettingsUI(skin, stage);
                }
            }
        });

        table = new Table();
        table.align(Align.topRight);
        table.setFillParent(true);

        table.add(imgSettings).pad(PADDING).fillX();

        stage.addActor(table);
    }

    public static void calculateDistance(float x, float y) {
        if (pointCounter % 2 == 0) {
            lblFirstPoint.setText(String.format(Locale.getDefault(), "x1: [%04.2f] | y1: [%04.2f]", x, y));
        } else {
            lblSecondPoint.setText(String.format(Locale.getDefault(), "x2: [%04.2f] | y2: [%04.2f]", x, y));
        }

        if (pointCounter >= 1) {
            float distance = (float) Math.sqrt(Math.pow(prePoint.x - x, 2) + Math.pow(prePoint.y - y, 2));
            lblDistance.setText(String.format(Locale.getDefault(), "Distance: [%04.2f]", distance));
        }

        pointCounter++;
        prePoint = new Vector2(x, y);
    }

    public static void updateSamples(int samples) {
        lblSamples.setText(String.format(Locale.getDefault(),"Samples: [%d]", samples));
    }

    public static void updateAP(int ap) {
        lblAP.setText(String.format(Locale.getDefault(),"AP: [%d]", ap));
    }

    public static void updateActivity(String activity) {
        lblStatus.setText(String.format(Locale.getDefault(),"Status: [%s]", activity));
    }

    public static void updateOrientation(float orientation) {
        lblHeading.setText(String.format(Locale.getDefault(), "Heading: [%04.2f]", orientation));
    }
}
