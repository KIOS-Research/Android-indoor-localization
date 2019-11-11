package com.kios.airplace.LibGDX;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.codeandweb.physicseditor.PhysicsShapeCache;
import com.kios.airplace.Globals;

public class Room extends Image {
	private static final float scale = 1 / 115.0f;

	public Room() {
		super(new Texture(Globals.ROOM_NAME + ".png"));
		setScale(scale);

		PhysicsShapeCache physicsShapeCache = new PhysicsShapeCache(Globals.ROOM_NAME + ".xml");
		Body body = physicsShapeCache.createBody(Globals.ROOM_NAME, Globals.WORLD, scale, scale);
		body.setUserData(this);

		float[] polygonVertices;
		Vector2 v = new Vector2();
		Array<Fixture> fixtures = body.getFixtureList();

		for (Fixture fixture : fixtures) {
			PolygonShape polygonShape = (PolygonShape) fixture.getShape();
			polygonVertices = new float[2 * polygonShape.getVertexCount()];

			for (int i = 0, j = 0; i < polygonShape.getVertexCount(); i++, j += 2) {
				polygonShape.getVertex(i, v);
				polygonVertices[j] = v.x;
				polygonVertices[j + 1] = v.y;
			}
			Globals.ROOM_POLYGONS.add(new Polygon(polygonVertices));
		}
		physicsShapeCache.dispose();
	}

	@Override
	public void act(float delta) {
		super.act(delta);
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
	}
}