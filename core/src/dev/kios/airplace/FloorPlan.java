package dev.kios.airplace;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.codeandweb.physicseditor.PhysicsShapeCache;

import java.util.ArrayList;

public class FloorPlan extends Image {
    private static final float ROOM_SCALE = 1 / 115.0f;
    public ArrayList<Polygon> ROOM_POLYGONS = new ArrayList<>();

    public FloorPlan(World world) {
        super(new Texture("SFC-03.png"));
        setScale(ROOM_SCALE);

        PhysicsShapeCache physicsShapeCache = new PhysicsShapeCache("SFC-03.xml");
        Body body = physicsShapeCache.createBody("SFC-03", world, ROOM_SCALE, ROOM_SCALE);
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
            ROOM_POLYGONS.add(new Polygon(polygonVertices));
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