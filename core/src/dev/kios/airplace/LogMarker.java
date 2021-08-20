package dev.kios.airplace;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class LogMarker extends Image {
    public LogMarker() {
        super(new Texture("Marker.png"));
        setOrigin(-getWidth() / 200.0f, -getHeight() / 200.0f);
        setPosition(Float.NaN, Float.NaN);
        setColor(Color.BLACK);
        setScale(1 / 100.0f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
    }

    public void setMarkerPosition(float x, float y) {
        setPosition(x, y);
    }
}