package dev.kios.airplace.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class PFSettingsUI extends AbstractUI {
    public static final String TITLE = "Particle Filter Settings";

    public PFSettingsUI(Skin skin, Stage stage) {
        super(skin, stage);
    }

    @Override
    public void create() {
        //Filters
        TextField.TextFieldFilter integer = new TextField.TextFieldFilter() {
            @Override
            public boolean acceptChar(TextField textField, char c) {
                return Character.toString(c).matches("^[0-9]");
            }
        };
        TextField.TextFieldFilter decimal = new TextField.TextFieldFilter() {

            @Override
            public boolean acceptChar(TextField textField, char c) {
                if (textField.getText().contains("."))
                    return Character.toString(c).matches("^[0-9]");
                else {
                    return Character.toString(c).matches("^[0-9.]");
                }
            }
        };

        //Crete Widgets
        Label lblNOP = UtilsUI.createLabel("Number of Particles", Color.WHITE, skin);
        final TextField txtNOP = UtilsUI.createTextField(preferences.getString("NumberOfParticles"), "300", Align.right, integer, skin);

        Label lblStepLength = UtilsUI.createLabel("Step Length", Color.WHITE, skin);
        final TextField txtStepLength = UtilsUI.createTextField(preferences.getString("StepLength"), "0.8", Align.right, decimal, skin);

        Label lblInitialVariance = UtilsUI.createLabel("Initial Variance", Color.WHITE, skin);
        final TextField txtInitialVariance = UtilsUI.createTextField(preferences.getString("InitialVariance"), "5.0", Align.right, decimal, skin);

        Label lblSystemNoise = UtilsUI.createLabel("System Noise", Color.WHITE, skin);
        final TextField txtSN_00 = UtilsUI.createTextField(preferences.getString("SystemNoise00"), "5.0", Align.center, decimal, skin);
        final TextField txtSN_01 = UtilsUI.createTextField(preferences.getString("SystemNoise01"), "10.0", Align.center, decimal, skin);
        final TextField txtSN_10 = UtilsUI.createTextField(preferences.getString("SystemNoise10"), "10.0", Align.center, decimal, skin);
        final TextField txtSN_11 = UtilsUI.createTextField(preferences.getString("SystemNoise11"), "5.0", Align.center, decimal, skin);

        Label lblMeasurementNoise = UtilsUI.createLabel("Measurement Noise", Color.WHITE, skin);
        final CheckBox cbFECM = UtilsUI.createCheckBox("Fused Engine Covariance Matrix", skin);
        cbFECM.setChecked(preferences.getBoolean("FusedEngineCovarianceMatrix"));

        final TextField txtMN_00 = UtilsUI.createTextField(preferences.getString("MeasurementNoise00"), "10.0", Align.center, decimal, skin);
        final TextField txtMN_01 = UtilsUI.createTextField(preferences.getString("MeasurementNoise01"), "5.0", Align.center, decimal, skin);
        final TextField txtMN_10 = UtilsUI.createTextField(preferences.getString("MeasurementNoise10"), "5.0", Align.center, decimal, skin);
        final TextField txtMN_11 = UtilsUI.createTextField(preferences.getString("MeasurementNoise11"), "10.0", Align.center, decimal, skin);

        //Create UI
        final Table table = new Table();
        table.add(lblNOP).pad(PADDING).fill(true);
        table.add(txtNOP).pad(PADDING);
        table.row();
        table.add(lblStepLength).pad(PADDING).fill(true);
        table.add(txtStepLength).pad(PADDING);
        table.row();
        table.add(lblInitialVariance).pad(PADDING).fill(true);
        table.add(txtInitialVariance).pad(PADDING);
        table.row();
        table.add(lblSystemNoise).pad(PADDING).colspan(2);
        table.row();
        table.add(txtSN_00).pad(PADDING);
        table.add(txtSN_01).pad(PADDING);
        table.row();
        table.add(txtSN_10).pad(PADDING);
        table.add(txtSN_11).pad(PADDING);
        table.row();
        table.add(lblMeasurementNoise).pad(PADDING).colspan(2);
        table.row();
        table.add(cbFECM).pad(PADDING).colspan(2);

        final Table mnTable = new Table();
        mnTable.add(txtMN_00).pad(PADDING);
        mnTable.add(txtMN_01).pad(PADDING);
        mnTable.row();
        mnTable.add(txtMN_10).pad(PADDING);
        mnTable.add(txtMN_11).pad(PADDING);

        if (!cbFECM.isChecked()) {
            table.row();
            table.add(mnTable).colspan(2);
        }

        //Save
        final Window window = UtilsUI.createWindow(TITLE, table, skin, stage);
        Image imgClose = window.findActor("imgClose");
        UtilsUI.addEventListener(imgClose, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                preferences.putString("NumberOfParticles", txtNOP.getText());
                preferences.putString("StepLength", txtStepLength.getText());
                preferences.putString("InitialVariance", txtInitialVariance.getText());
                preferences.putString("SystemNoise00", txtSN_00.getText());
                preferences.putString("SystemNoise01", txtSN_01.getText());
                preferences.putString("SystemNoise10", txtSN_10.getText());
                preferences.putString("SystemNoise11", txtSN_11.getText());
                preferences.putBoolean("FusedEngineCovarianceMatrix", cbFECM.isChecked());
                preferences.putString("MeasurementNoise00", txtMN_00.getText());
                preferences.putString("MeasurementNoise01", txtMN_01.getText());
                preferences.putString("MeasurementNoise10", txtMN_10.getText());
                preferences.putString("MeasurementNoise11", txtMN_11.getText());

                preferences.flush();
                window.remove();
            }
        });
        stage.addActor(window);

        UtilsUI.addEventListener(cbFECM, new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!cbFECM.isChecked()) {
                    table.row();
                    table.add(mnTable).colspan(2);
                } else {
                    table.removeActor(mnTable);
                }
                table.pack();
                window.pack();
            }
        });
    }
}
