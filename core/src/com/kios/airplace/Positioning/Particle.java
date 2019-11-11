package com.kios.airplace.Positioning;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.kios.airplace.Globals;
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Particle extends Image {
	public Body body;
	public float radius;
	public double weight;
	public RealMatrix location;
	public RealMatrix prediction;
	private Vector2 velocity;
	private Vector2 v = new Vector2();
	private NormalDistribution normalDistribution = new NormalDistribution();

	public Particle(RealMatrix baseLocation, double initialWeight) {
		radius = 0.05f;
		weight = initialWeight;
		velocity = new Vector2();

		location = MatrixUtils.createRealMatrix(2, 1);
		prediction = MatrixUtils.createRealMatrix(2, 1);

		location.setEntry(0, 0, baseLocation.getEntry(0, 0) + Math.sqrt(Globals.INITIAL_VARIANCE) * normalDistribution.sample());
		location.setEntry(1, 0, baseLocation.getEntry(1, 0) + Math.sqrt(Globals.INITIAL_VARIANCE) * normalDistribution.sample());

		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set((float) location.getEntry(0, 0), (float) location.getEntry(1, 0));
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.fixedRotation = true;

		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(radius);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circleShape;

		body = Globals.WORLD.createBody(bodyDef);
		body.createFixture(fixtureDef);
		body.setUserData(this);
		setPosition(body.getPosition().x, body.getPosition().y);

		circleShape.dispose();
	}

	public void eliminate() {
		Globals.WORLD.destroyBody(body);
		remove();
	}

	public void update(double sumWeights) {
		weight /= sumWeights;
	}

	public void predict(RealMatrix fusedEngineLocationMatrix) {
		prediction.setEntry(0, 0, body.getPosition().x + Globals.STEP_LENGTH * Math.cos(Globals.ORIENTATION) + Math.sqrt(Globals.SYSTEM_NOISE.getEntry(0, 0)) * normalDistribution.sample());
		prediction.setEntry(1, 0, body.getPosition().y + Globals.STEP_LENGTH * Math.sin(Globals.ORIENTATION) + Math.sqrt(Globals.SYSTEM_NOISE.getEntry(1, 1)) * normalDistribution.sample());

		try {
			MultivariateNormalDistribution mvnPdf = new MultivariateNormalDistribution(fusedEngineLocationMatrix.getColumn(0), Globals.MEASUREMENTS_NOISE.getData());
			weight = mvnPdf.density(prediction.getColumn(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void act(float delta) {
		super.act(delta);

		if (!Globals.NAVIGATOR) {
			eliminate();
			return;
		}

		int speed = 10;
		v.set((float) location.getEntry(0, 0), (float) location.getEntry(1, 0));
		velocity = v.cpy().sub(body.getPosition()).nor().scl(speed);

		body.setLinearVelocity(velocity);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}
}