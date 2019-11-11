package com.kios.airplace.LibGDX;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.kios.airplace.Globals;

public class MarkerLocation extends Image {
	public Body body;

	public MarkerLocation(float xPos, float yPos, String texturePath) {
		super(new Texture(texturePath));

		setScale(1 / 100.0f);
		setPosition(xPos, yPos);
		setOrigin(-getWidth() / 200.0f, 0.0f);

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(xPos, yPos);
		bodyDef.type = BodyDef.BodyType.KinematicBody;

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(0.30f);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circleShape;

		body = Globals.WORLD.createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setUserData(this);

		circleShape.dispose();
	}

	public void eliminate() {
		Globals.WORLD.destroyBody(body);
		remove();
	}

	public void moveToLocation(float x, float y) {
		body.setTransform(x, y, 0);
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		setPosition(body.getPosition().x, body.getPosition().y);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}
}