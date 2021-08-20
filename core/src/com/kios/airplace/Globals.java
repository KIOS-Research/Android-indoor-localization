package com.kios.airplace;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kios.airplace.Positioning.Particle;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;

public class Globals {
	//Particle Filter
	public static int PARTICLES_NUMBER = 500;
	static int PARTICLES_NUMBER_TO_SHOW = 250;

	public static float STEP_LENGTH = 0.8f;
	public static float INITIAL_VARIANCE = 5;

	public static RealMatrix SYSTEM_NOISE = MatrixUtils.createRealMatrix(new double[][] {{5, 10}, {10, 5}});
	public static RealMatrix MEASUREMENTS_NOISE = MatrixUtils.createRealMatrix(new double[][] {{10, 5}, {5, 10}});

	public static ArrayList<Particle> PARTICLES = new ArrayList<>();

	//Room
	public static String ROOM_NAME = "SFC-03";
	public static ArrayList<Polygon> ROOM_POLYGONS = new ArrayList<>();

	//Paths
	static String RSS_LOG_FILE_PATH = "AirPlace\\logs\\rss\\rssFile-";
	public static String RSS_NAV_FILE_PATH = "/AirPlace/radioMaps/rss/rssRadioMap-mean.txt";

	static String MAGNETIC_LOG_FILE_PATH = "AirPlace\\logs\\magnetic\\magneticFile-";
	public static String MAGNETIC_NAV_FILE_PATH = "/AirPlace/radioMaps/magnetic/magneticRadioMap-mean.txt";

	//Headings
	static String RSS_HEADING = "# Timestamp, X, Y, Orientation, MAC Address of AP, RSS\n";
	static String MAGNETIC_HEADING = "# Timestamp, X, Y, Orientation, Magnetic Field X, Magnetic Field Y, Magnetic Field Z\n";

	//Logger
	static int RECORDED_SAMPLES = 0;
	public static float ORIENTATION = 0;
	static boolean ENABLE_LOGGING = false;
	static float LONG_PRESS_SECONDS = 0.5f;
	public static STATE STATUS = STATE.STANDING;
	static ArrayList<LogRecord> WIFI_LIST = new ArrayList<>();
	static ArrayList<Float> MAGNETIC_FIELD = new ArrayList<>();

	public enum STATE {
		STANDING("Standing"), WALKING("Walking");

		private String status;
		STATE(String status) {
			this.status = status;
		}

		public String getStatus(){
			return status;
		}
	}

	//Android
	static int TOTAL_POINTS = -1;
	static int ACCESS_POINTS = 0;
	static int SELECTED_ALGORITHM = 2;
	static boolean ONE_TIME_SCAN = false;
	static boolean CAN_DISTRIBUTE = false;
	static boolean NEW_SCAN_AVAILABLE = false;

	static boolean LOGGER = true;
	public static boolean NAVIGATOR = true;

	static boolean SHOW_RECORDED_POINTS = false;
	static boolean[] SHOW_MARKER_LOCATION = {true, true, false, false};

	//LibGDX
	static boolean DEBUG = false;

	static float TIME_STEP = 1f / 60f;
	static int VELOCITY_ITERATIONS = 6;
	static int POSITION_ITERATIONS = 2;
	static Vector2 GRAVITY = new Vector2(0, 0);

	static Stage GAME_STAGE;
	public static World WORLD;

	static float ZOOM_IN;
	static float ZOOM_OUT;
	static float CURRENT_ZOOM ;
	static Vector3 CAMERA_POSITION = new Vector3(6, 26, 0);

	static OrthographicCamera CAMERA;
	static Box2DDebugRenderer RENDERER;

	//Desktop
	static Game GAME;
	static Stage UI_STAGE;
	static String WRITER_PATH;
	static boolean FE_MN = false;
	static boolean SHOW_NEXT = false;
	static double[][] REAL_LOCATION = new double[2][1];
	static ArrayList<Vector2> rlPoints = new ArrayList<>();
	static ArrayList<Vector2> rssPoints = new ArrayList<>();
	static ArrayList<Vector2> magPoints = new ArrayList<>();
	static ArrayList<Vector2> fePoints = new ArrayList<>();
	static ArrayList<Vector2> pfPoints = new ArrayList<>();

}