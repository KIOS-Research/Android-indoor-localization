package com.kios.airplace;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class AirPlace extends Game {
	private WiFiMagneticListener wiFiMagneticReader;

	public AirPlace(WiFiMagneticListener wiFiMagneticReader) {
		this.wiFiMagneticReader = wiFiMagneticReader;
	}

	@Override
	public void create() {
		switch (Gdx.app.getType()) {
			case Android:
				Globals.ZOOM_IN = 0.004f;
				Globals.ZOOM_OUT = 0.04f;
				Globals.CURRENT_ZOOM = 0.025f;
				break;
			case Desktop:
				Globals.GAME = this;
				Globals.ZOOM_IN = 0.01f;
				Globals.ZOOM_OUT = 0.06f;
				Globals.CURRENT_ZOOM = 0.06f;
				break;
		}
		setScreen(new MainScreen(wiFiMagneticReader));
	}
}