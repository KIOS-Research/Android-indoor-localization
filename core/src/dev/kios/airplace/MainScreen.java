package dev.kios.airplace;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import dev.kios.airplace.ui.MainUI;
import dev.kios.airplace.ui.Toast;

import java.util.ArrayList;

import static com.badlogic.gdx.Application.ApplicationType.Android;
import static com.badlogic.gdx.Application.ApplicationType.Desktop;

public class MainScreen implements GestureDetector.GestureListener, Screen {
    private static final float LONG_PRESS_SECONDS = 0.5f;
    private static final Vector2 GRAVITY = new Vector2(0, 0);
    private static final int MILLISECONDS_TO_VIBRATE = 250;
    private static final float INITIAL_ZOOM = 0.025f;
    private static final float MIN_ZOOM = 0.005f;
    private static final float MAX_ZOOM = 0.05f;
    public final OrthographicCamera orthographicCamera;
    private final Stage ui;
    private final Stage stage;
    private final ShapeRenderer shapeRenderer;
    private final ScreenViewport uiViewport;
    private final Skin skin;
    private final CoreSpecificCode mCoreSpecificCode;
    private final Vector3 normal = new Vector3();
    private final Vector3 tangent = new Vector3();
    private final LogMarker logMarker;
    private final ArrayList<Vector2> rlPoints = new ArrayList<>();
    private float timeSeconds = 0.0f;
    private final float period = 0.05f;
    private int samples = 0;
    private float initialScale;
    private float initialAngle;
    private float currentAngle;

    public MainScreen(CoreSpecificCode coreSpecificCode) {
        mCoreSpecificCode = coreSpecificCode;
        shapeRenderer = new ShapeRenderer();
        uiViewport = new ScreenViewport();
        ui = new Stage(uiViewport);

        stage = new Stage(new ScreenViewport());

        skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("truetypefont/Amble-Light.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        BitmapFont bitmapFont = generator.generateFont(parameter);
        generator.dispose();

        TextButton.TextButtonStyle textButtonStyle = skin.get("default", TextButton.TextButtonStyle.class);
        textButtonStyle.font = bitmapFont;

        TextField.TextFieldStyle textFieldStyle = skin.get("default", TextField.TextFieldStyle.class);
        textFieldStyle.font = bitmapFont;

        Label.LabelStyle labelStyle = skin.get("default", Label.LabelStyle.class);
        labelStyle.font = bitmapFont;

        CheckBox.CheckBoxStyle checkBoxStyle = skin.get("default", CheckBox.CheckBoxStyle.class);
        checkBoxStyle.fontColor = Color.WHITE;
        checkBoxStyle.font = bitmapFont;

        SelectBox.SelectBoxStyle selectBoxStyle = skin.get("default", SelectBox.SelectBoxStyle.class);
        selectBoxStyle.font = bitmapFont;

        Window.WindowStyle windowStyle = skin.get("default", Window.WindowStyle.class);
        windowStyle.titleFont = bitmapFont;

        logMarker = new LogMarker();

        World world = new World(GRAVITY, true);

        FloorPlan floorPlan = new FloorPlan(world);
        stage.addActor(floorPlan);

        Vector3 centerCamera = new Vector3();
        centerCamera.x = (floorPlan.getWidth() * floorPlan.getScaleX()) / 2.0f;
        centerCamera.y = (floorPlan.getHeight() * floorPlan.getScaleY()) / 2.0f;

        orthographicCamera = (OrthographicCamera) stage.getViewport().getCamera();
        orthographicCamera.zoom = INITIAL_ZOOM;
        orthographicCamera.position.set(centerCamera);

        new MainUI(skin, ui);
        GestureDetector gestureDetector = new GestureDetector(this);
        gestureDetector.setLongPressSeconds(LONG_PRESS_SECONDS);

        InputMultiplexer inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(ui);
        inputMultiplexer.addProcessor(gestureDetector);

        Gdx.input.setInputProcessor(inputMultiplexer);
        resizeUiScale();
    }

    public void resizeUiScale() {
        float scale;
        if (Gdx.app.getType() == Desktop) {
            scale = Gdx.graphics.getDisplayMode().height >= 1440 ? 2.0f : 1.0f;
        } else if (Gdx.app.getType() == Android) {
            scale = Gdx.graphics.getDensity();
        } else {
            scale = 1.0f;
        }

        uiViewport.setUnitsPerPixel(1.0f / scale);
        uiViewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.app.getType() == Desktop) {
            if (MainUI.toggleLogger) {
                timeSeconds += Gdx.graphics.getDeltaTime();
                if (timeSeconds > period) {
                    timeSeconds = 0.0f;
                    if (!mCoreSpecificCode.getLogSample()) {
                        MainUI.toggleLogger = false;
                        MainUI.btnLogger.setText("Start");
                    } else {
                        MainUI.updateSamples(++samples);
                        rlPoints.add(new Vector2(mCoreSpecificCode.getPoint()));
                    }
                }
            } else {
                timeSeconds = 0.0f;
            }
        }

        MainUI.updateAP(mCoreSpecificCode.getScanResults().size());
        MainUI.updateActivity(mCoreSpecificCode.getActivity());
        MainUI.updateOrientation(mCoreSpecificCode.getOrientation());

        stage.act();
        stage.draw();

        drawLines();

        ui.act();
        ui.draw();

        Toast.show();
    }

    private void drawLines() {
        if (Gdx.app.getType() == Desktop) {
            shapeRenderer.setProjectionMatrix(orthographicCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            shapeRenderer.setColor(Color.BLACK);
            for (Vector2 rlPoint : rlPoints) {
                shapeRenderer.circle(rlPoint.x, rlPoint.y, 0.125f, 30);
            }

            shapeRenderer.end();
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            shapeRenderer.setColor(Color.BLACK);
            for (int i = 1; i < rlPoints.size(); i++) {
                shapeRenderer.line(rlPoints.get(i - 1), rlPoints.get(i));
            }

            shapeRenderer.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
        ui.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        ui.dispose();
        skin.dispose();
        stage.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        initialScale = orthographicCamera.zoom;
        initialAngle = currentAngle;
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        Vector3 v = new Vector3(x, y, 0);
        orthographicCamera.unproject(v);

        logMarker.setMarkerPosition(v.x, v.y);
        stage.addActor(logMarker);

        MainUI.calculateDistance(v.x, v.y);
        Gdx.input.vibrate(MILLISECONDS_TO_VIBRATE);
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        tangent.set(orthographicCamera.direction).crs(orthographicCamera.up);
        normal.set(orthographicCamera.up);

        orthographicCamera.position.add(tangent.scl(-deltaX * orthographicCamera.zoom));
        orthographicCamera.position.add(normal.scl(deltaY * orthographicCamera.zoom));
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float ratio = initialDistance / distance;
        orthographicCamera.zoom = MathUtils.clamp(initialScale * ratio, MIN_ZOOM, MAX_ZOOM);
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        float toDegrees = MathUtils.atan2(pointer2.y - pointer1.y, pointer2.x - pointer1.x);
        float fromDegrees = MathUtils.atan2(initialPointer2.y - initialPointer1.y, initialPointer2.x - initialPointer1.x);
        float deltaAngle = toDegrees - fromDegrees;

        currentAngle = initialAngle + deltaAngle;
        orthographicCamera.up.set(MathUtils.sin(-currentAngle), MathUtils.cos(-currentAngle), 0);
        return false;
    }

    @Override
    public void pinchStop() {

    }
}
