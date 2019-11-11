package com.kios.airplace.LibGDX;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class LogMarker extends Image {

	public LogMarker() {
		super(new Texture("logger-marker.png"));
		setScale(1 / 100.0f);
		setOrigin(-getWidth() / 200.0f, 0.0f);
	}

	public void setMarkerPosition(float x, float y) {
		setPosition(x, y);
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		setPosition(getX(), getY());
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}
}