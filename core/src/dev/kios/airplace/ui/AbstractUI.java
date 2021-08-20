package dev.kios.airplace.ui;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public abstract class AbstractUI extends ApplicationAdapter {
    protected static final float PADDING = 5.0f;

    protected final Skin skin;
    protected final Stage stage;
    protected final Preferences preferences;

    public AbstractUI(Skin skin, Stage stage) {
        this.skin = skin;
        this.stage = stage;

        preferences = Gdx.app.getPreferences("AirPlacePreferences");
        create();
    }
}
