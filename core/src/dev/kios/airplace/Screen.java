//package dev.kios.airplace;
//
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.OrthographicCamera;
//import com.badlogic.gdx.input.GestureDetector;
//import com.badlogic.gdx.math.MathUtils;
//import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.math.Vector3;
//import com.badlogic.gdx.physics.box2d.World;
//import com.badlogic.gdx.scenes.scene2d.Stage;
//import dev.kios.airplace.FloorPlan;
//import dev.kios.airplace.LogMarker;
//import dev.kios.airplace.ui.MainUI;
//
//public class Screen implements GestureDetector.GestureListener {
////    private static final Vector2 GRAVITY = new Vector2(0, 0);
////    private static final int MILLISECONDS_TO_VIBRATE = 250;
////
////    private static final float INITIAL_ZOOM = 0.025f;
////    private static final float MIN_ZOOM = 0.005f;
////    private static final float MAX_ZOOM = 0.05f;
////
////    private final Vector3 normal = new Vector3();
////    private final Vector3 tangent = new Vector3();
////    public final OrthographicCamera orthographicCamera;
////
////    private float initialScale;
////    private float initialAngle;
////    private float currentAngle;
////
////    public World world;
////    private final Stage stage;
////    private final LogMarker logMarker;
//
//    public Screen(Stage stage) {
//        this.stage = stage;
//        logMarker = new LogMarker();
//
//        world = new World(GRAVITY, true);
//
//        FloorPlan floorPlan = new FloorPlan(world);
//        stage.addActor(floorPlan);
//
//        Vector3 centerCamera = new Vector3();
//        centerCamera.x = (floorPlan.getWidth() * floorPlan.getScaleX()) / 2.0f;
//        centerCamera.y = (floorPlan.getHeight() * floorPlan.getScaleY()) / 2.0f;
//
//        orthographicCamera = (OrthographicCamera) stage.getViewport().getCamera();
//        orthographicCamera.zoom = INITIAL_ZOOM;
//        orthographicCamera.position.set(centerCamera);
//    }
//
//    @Override
//    public boolean touchDown(float x, float y, int pointer, int button) {
//        initialScale = orthographicCamera.zoom;
//        initialAngle = currentAngle;
//        return false;
//    }
//
//    @Override
//    public boolean tap(float x, float y, int count, int button) {
//        return false;
//    }
//
//    @Override
//    public boolean longPress(float x, float y) {
//        Vector3 v = new Vector3(x, y, 0);
//        orthographicCamera.unproject(v);
//
//        logMarker.setMarkerPosition(v.x, v.y);
//        stage.addActor(logMarker);
//
//        MainUI.calculateDistance(v.x, v.y);
//        Gdx.input.vibrate(MILLISECONDS_TO_VIBRATE);
//        return false;
//    }
//
//    @Override
//    public boolean fling(float velocityX, float velocityY, int button) {
//        return false;
//    }
//
//    @Override
//    public boolean pan(float x, float y, float deltaX, float deltaY) {
//        tangent.set(orthographicCamera.direction).crs(orthographicCamera.up);
//        normal.set(orthographicCamera.up);
//
//        orthographicCamera.position.add(tangent.scl(-deltaX * orthographicCamera.zoom));
//        orthographicCamera.position.add(normal.scl(deltaY * orthographicCamera.zoom));
//        return false;
//    }
//
//    @Override
//    public boolean panStop(float x, float y, int pointer, int button) {
//        return false;
//    }
//
//    @Override
//    public boolean zoom(float initialDistance, float distance) {
//        float ratio = initialDistance / distance;
//        orthographicCamera.zoom = MathUtils.clamp(initialScale * ratio, MIN_ZOOM, MAX_ZOOM);
//        return false;
//    }
//
//    @Override
//    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
//        float toDegrees = MathUtils.atan2(pointer2.y - pointer1.y, pointer2.x - pointer1.x);
//        float fromDegrees = MathUtils.atan2(initialPointer2.y - initialPointer1.y, initialPointer2.x - initialPointer1.x);
//        float deltaAngle = toDegrees - fromDegrees;
//
//        currentAngle = initialAngle + deltaAngle;
//        orthographicCamera.up.set(MathUtils.sin(-currentAngle), MathUtils.cos(-currentAngle), 0);
//        return false;
//    }
//
//    @Override
//    public void pinchStop() {
//
//    }
//}
