package com.kios.airplace;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kios.airplace.LibGDX.*;
import com.kios.airplace.Positioning.Particle;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import static com.badlogic.gdx.Application.ApplicationType.Android;
import static com.badlogic.gdx.Application.ApplicationType.Desktop;

class MainScreen implements Screen, GestureDetector.GestureListener, InputProcessor {
	private int touchCount;
	private Vector3 firstPoint;
	private Vector3 secondPoint;
	private float touchDistance;
	private float accumulator;
	private float currentAngle;

	private LogMarker logMarker;
	private FileHandle rssFile, magneticFile;
	private WiFiMagneticListener wiFiMagneticListener;
	private MarkerLocation rssMarker, magneticMarker, fusedMarker, pfMarker;

	private BitmapFont bitmapFont;
	private SpriteBatch spriteBatch;
	private ShapeRenderer shapeRenderer;

	private boolean canDrag;
	private boolean writeHeading;
	private boolean shouldDistribute;

	private ArrayList<Vector2> disPoints;
	private ArrayList<LogSample> samples;

	private double[][] rssLocation;
	private double[][] magneticLocation;
	private double[][] fusionEngineLocation;
	private double[][] particleFilterLocation;

	private Label lblRssError;
	private Label lblMagneticError;
	private Label lblFusedError;
	private Label lblParticleFilterError;

	private BufferedWriter writer;
	private PositioningAlgorithms positioningAlgorithms;

	MainScreen(WiFiMagneticListener wiFiMagneticListener) {
		Box2D.init();

		touchCount = 0;
		touchDistance = Float.NaN;
		firstPoint = new Vector3(Float.NaN, Float.NaN, 0);
		secondPoint = new Vector3(Float.NaN, Float.NaN, 0);

		accumulator = 0;
		currentAngle = 0.0f;

		bitmapFont = new BitmapFont();
		spriteBatch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		canDrag = false;
		writeHeading = true;
		shouldDistribute = false;

		samples = new ArrayList<>();
		disPoints = new ArrayList<>();

		rssLocation = null;
		magneticLocation = null;
		fusionEngineLocation = null;
		particleFilterLocation = null;
		this.wiFiMagneticListener = wiFiMagneticListener;

		Globals.RENDERER = new Box2DDebugRenderer();
		Globals.GAME_STAGE = new Stage(new ScreenViewport());

		Globals.WORLD = new World(Globals.GRAVITY, true);
		Globals.WORLD.setContactListener(new Contacts());

		Globals.CAMERA = (OrthographicCamera) Globals.GAME_STAGE.getViewport().getCamera();
		Globals.CAMERA.position.set(Globals.CAMERA_POSITION);
		Globals.CAMERA.zoom = Globals.CURRENT_ZOOM;

		Room room = new Room();
		Globals.GAME_STAGE.addActor(room);

		createBitMapFont();
		logMarker = new LogMarker();
		positioningAlgorithms = new PositioningAlgorithms();

		InputMultiplexer inputMultiplexer = new InputMultiplexer();

		if (Gdx.app.getType() == Android) {
			Gdx.input.setCatchKey(Input.Keys.BACK, true);
			GestureDetector gestureDetector = new GestureDetector(this);

			gestureDetector.setLongPressSeconds(Globals.LONG_PRESS_SECONDS);
			inputMultiplexer.addProcessor(gestureDetector);
		} else if (Gdx.app.getType() == Desktop) {
			Globals.UI_STAGE = new Stage(new ScreenViewport());

			inputMultiplexer.addProcessor(this);
			inputMultiplexer.addProcessor(Globals.UI_STAGE);
			inputMultiplexer.addProcessor(Globals.GAME_STAGE);
			gui();
		}

		Gdx.input.setInputProcessor(inputMultiplexer);

		String date = new SimpleDateFormat("ddMMyy", Locale.getDefault()).format(new Date());

		rssFile = Gdx.files.external(Globals.RSS_LOG_FILE_PATH + date + ".txt");
		magneticFile = Gdx.files.external(Globals.MAGNETIC_LOG_FILE_PATH + date + ".txt");
	}

	private void gui() {
		Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));

		//Construct the Widgets
		Label lblPositioningAlgorithms = new Label("Positioning Algorithms", skin);

		final SelectBox<String> sbPositioningAlgorithms = new SelectBox<>(skin);
		sbPositioningAlgorithms.setItems("KNN", "WKNN", "MAP", "MMSE" , "None");
		sbPositioningAlgorithms.setSelected("None");

		Label lblLocation = new Label("Location", skin);
		CheckBox cbPoints = new CheckBox("Points", skin);
		final CheckBox cbRss = new CheckBox("Rss Location", skin);
		CheckBox cbMagnetic = new CheckBox("Magnetic Location", skin);
		final CheckBox cbFused = new CheckBox("Fused Location", skin);
		final CheckBox cbPF = new CheckBox("Particle Filter Location", skin);

		cbRss.setChecked(true);
		cbMagnetic.setChecked(true);

		Label lblTotalPoints = new Label("Total Points: " + Globals.TOTAL_POINTS, skin);
		TextButton btnDebug = new TextButton("DEBUG", skin, "toggle");

		Label lblNumberOfParticles = new Label("Number of Particles", skin);
		Label lblParticlesToShow = new Label("Particles to show", skin);
		Label lblStepLength = new Label("Step length", skin);
		Label lblInitialVariance = new Label("Initial Variance", skin);
		Label lblSystemNoise = new Label("System Noise", skin);
		Label lblMeasurementNoise = new Label("Measurement Noise", skin);

		final TextField txtNumberOfParticles = new TextField("", skin);
		txtNumberOfParticles.setAlignment(Align.right);
		txtNumberOfParticles.setName("txtNumberOfParticles");
		txtNumberOfParticles.setMessageText(String.valueOf(Globals.PARTICLES_NUMBER));

		final TextField txtParticlesToShow = new TextField("", skin);
		txtParticlesToShow.setAlignment(Align.right);
		txtParticlesToShow.setName("txtParticlesToShow");
		txtParticlesToShow.setMessageText(String.valueOf(Globals.PARTICLES_NUMBER_TO_SHOW));

		final TextField txtStepLength = new TextField("", skin);
		txtStepLength.setAlignment(Align.right);
		txtStepLength.setName("txtStepLength");
		txtStepLength.setMessageText(String.valueOf(Globals.STEP_LENGTH));

		final TextField txtInitialVariance = new TextField("", skin);
		txtInitialVariance.setAlignment(Align.right);
		txtInitialVariance.setName("txtInitialVariance");
		txtInitialVariance.setMessageText(String.valueOf(Globals.INITIAL_VARIANCE));

		final TextField txtSystemNoise00 = new TextField("", skin);
		txtSystemNoise00.setAlignment(Align.right);
		txtSystemNoise00.setName("txtSystemNoise00");
		txtSystemNoise00.setMessageText(String.valueOf(Globals.SYSTEM_NOISE.getEntry(0, 0)));

		final TextField txtSystemNoise01 = new TextField("", skin);
		txtSystemNoise01.setAlignment(Align.right);
		txtSystemNoise01.setName("txtSystemNoise01");
		txtSystemNoise01.setMessageText(String.valueOf(Globals.SYSTEM_NOISE.getEntry(0, 1)));

		final TextField txtSystemNoise10 = new TextField("", skin);
		txtSystemNoise10.setAlignment(Align.right);
		txtSystemNoise10.setName("txtSystemNoise10");
		txtSystemNoise10.setMessageText(String.valueOf(Globals.SYSTEM_NOISE.getEntry(1, 0)));

		final TextField txtSystemNoise11 = new TextField("", skin);
		txtSystemNoise11.setAlignment(Align.right);
		txtSystemNoise11.setName("txtSystemNoise11");
		txtSystemNoise11.setMessageText(String.valueOf(Globals.SYSTEM_NOISE.getEntry(1, 1)));

		final TextField txtMeasurementsNoise00 = new TextField("", skin);
		txtMeasurementsNoise00.setAlignment(Align.right);
		txtMeasurementsNoise00.setName("txtMeasurementsNoise00");
		txtMeasurementsNoise00.setMessageText(String.valueOf(Globals.MEASUREMENTS_NOISE.getEntry(0, 0)));

		final TextField txtMeasurementsNoise01 = new TextField("", skin);
		txtMeasurementsNoise01.setAlignment(Align.right);
		txtMeasurementsNoise01.setName("txtMeasurementsNoise01");
		txtMeasurementsNoise01.setMessageText(String.valueOf(Globals.MEASUREMENTS_NOISE.getEntry(0, 1)));

		final TextField txtMeasurementsNoise10 = new TextField("", skin);
		txtMeasurementsNoise10.setAlignment(Align.right);
		txtMeasurementsNoise10.setName("txtMeasurementsNoise10");
		txtMeasurementsNoise10.setMessageText(String.valueOf(Globals.MEASUREMENTS_NOISE.getEntry(1, 0)));

		final TextField txtMeasurementsNoise11 = new TextField("", skin);
		txtMeasurementsNoise11.setAlignment(Align.right);
		txtMeasurementsNoise11.setName("txtMeasurementsNoise11");
		txtMeasurementsNoise11.setMessageText(String.valueOf(Globals.MEASUREMENTS_NOISE.getEntry(1, 1)));

		TextButton btnStep = new TextButton("STEP", skin);

		cbFused.setDisabled(true);
		cbPF.setDisabled(true);
		txtNumberOfParticles.setDisabled(true);
		txtParticlesToShow.setDisabled(true);
		txtStepLength.setDisabled(true);
		txtInitialVariance.setDisabled(true);
		txtSystemNoise00.setDisabled(true);
		txtSystemNoise01.setDisabled(true);
		txtSystemNoise10.setDisabled(true);
		txtSystemNoise11.setDisabled(true);
		txtMeasurementsNoise00.setDisabled(true);
		txtMeasurementsNoise01.setDisabled(true);
		txtMeasurementsNoise10.setDisabled(true);
		txtMeasurementsNoise11.setDisabled(true);

		final boolean[] isPressed = {false};
		final Label lblErrors = new Label("ERRORS", skin);
		lblRssError = new Label("Rss: N/A", skin);
		lblMagneticError = new Label("Magnetic: N/A", skin);
		lblFusedError = new Label("Fused: NaN", skin);
		lblParticleFilterError = new Label("Particle Filter: NaN", skin);

		//Listeners
		final ChangeListener changeListener = new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				Globals.SELECTED_ALGORITHM = sbPositioningAlgorithms.getSelectedIndex() + 1;
				sbPositioningAlgorithms.setDisabled(true);

				if (!sbPositioningAlgorithms.getSelected().equals("KNN")) {
					cbFused.setDisabled(false);
					cbPF.setDisabled(false);
					txtNumberOfParticles.setDisabled(false);
					txtParticlesToShow.setDisabled(false);
					txtStepLength.setDisabled(false);
					txtInitialVariance.setDisabled(false);
					txtSystemNoise00.setDisabled(false);
					txtSystemNoise01.setDisabled(false);
					txtSystemNoise10.setDisabled(false);
					txtSystemNoise11.setDisabled(false);
					txtMeasurementsNoise00.setDisabled(false);
					txtMeasurementsNoise01.setDisabled(false);
					txtMeasurementsNoise10.setDisabled(false);
					txtMeasurementsNoise11.setDisabled(false);
				} else {
					positioningAlgorithms.K = "1";
				}

				try {
					isPressed[0] = true;
					writer = new BufferedWriter(new FileWriter(Globals.WRITER_PATH + "\\" + sbPositioningAlgorithms.getSelected() + ".txt"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		final ClickListener clickListener = new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				sbPositioningAlgorithms.removeListener(this);
				sbPositioningAlgorithms.addListener(changeListener);
			}
		};

		sbPositioningAlgorithms.addListener(clickListener);

		cbPoints.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				Globals.SHOW_RECORDED_POINTS = !Globals.SHOW_RECORDED_POINTS;
			}
		});

		cbRss.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				Globals.SHOW_MARKER_LOCATION[0] = !Globals.SHOW_MARKER_LOCATION[0];

				if (!Globals.SHOW_MARKER_LOCATION[0] && rssMarker != null) {
					rssMarker.eliminate();
					rssMarker = null;
				} else if (rssLocation != null) {
					rssMarker = new MarkerLocation((float) rssLocation[0][0], (float) rssLocation[1][0], "rss-marker.png");
					Globals.GAME_STAGE.addActor(rssMarker);
				}
			}
		});

		cbMagnetic.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				Globals.SHOW_MARKER_LOCATION[1] = !Globals.SHOW_MARKER_LOCATION[1];

				if (!Globals.SHOW_MARKER_LOCATION[1] && magneticMarker != null) {
					magneticMarker.eliminate();
					magneticMarker = null;
				} else if (magneticLocation != null) {
					magneticMarker = new MarkerLocation((float) magneticLocation[0][0], (float) magneticLocation[1][0], "magnetic-marker.png");
					Globals.GAME_STAGE.addActor(magneticMarker);
				}
			}
		});

		cbFused.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				Globals.SHOW_MARKER_LOCATION[2] = !Globals.SHOW_MARKER_LOCATION[2];

				if (!Globals.SHOW_MARKER_LOCATION[2] && fusedMarker != null) {
					fusedMarker.eliminate();
					fusedMarker = null;
				} else if (fusionEngineLocation != null) {
					fusedMarker = new MarkerLocation((float) fusionEngineLocation[0][0], (float) fusionEngineLocation[1][0], "fused-marker.png");
					Globals.GAME_STAGE.addActor(fusedMarker);
				}
			}
		});

		cbPF.addListener(new ChangeListener() {
			public void changed(ChangeEvent event, Actor actor) {
				Globals.SHOW_MARKER_LOCATION[3] = !Globals.SHOW_MARKER_LOCATION[3];

				if (!Globals.SHOW_MARKER_LOCATION[3] && pfMarker != null) {
					lblParticleFilterError.setText("Particle Filter: NaN");
					positioningAlgorithms.is_pf_initialized = false;
					particleFilterLocation = null;

					for (Particle p : Globals.PARTICLES)
						p.eliminate();
					Globals.pfPoints.clear();
					pfMarker.eliminate();
					pfMarker = null;
				} else if (particleFilterLocation != null) {
					pfMarker = new MarkerLocation((float) particleFilterLocation[0][0], (float) particleFilterLocation[1][0], "pf-marker.png");
					Globals.GAME_STAGE.addActor(pfMarker);
				}
			}
		});

		btnDebug.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				Globals.DEBUG = !Globals.DEBUG;
				return true;
			}
		});

		btnStep.addListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (!isPressed[0])
					return true;
				Globals.SHOW_NEXT = true;
				return true;
			}
		});

		TextField.TextFieldListener textFieldListener = new TextField.TextFieldListener() {
			@Override
			public void keyTyped(TextField textField, char c) {
				if (c == '\r') {
					String string = textField.getText().trim();

					if (string.equals("")) {
						string = textField.getMessageText();
						textField.setText(string);
					} else if (string.contains("..")) {
						string = textField.getMessageText();
						textField.setText(string);
					}

					switch (textField.getName()) {
						case "txtNumberOfParticles":
							Globals.PARTICLES_NUMBER = Integer.parseInt(string);
							for (Particle p : Globals.PARTICLES) {
								p.eliminate();
							}
							Globals.pfPoints.clear();
							Globals.PARTICLES.clear();
							positioningAlgorithms.is_pf_initialized = false;
							positioningAlgorithms.logicalArray = new boolean[Globals.PARTICLES_NUMBER];
							break;
						case "txtParticlesToShow":
							Globals.PARTICLES_NUMBER_TO_SHOW = Integer.parseInt(string);
							break;
						case "txtStepLength":
							Globals.STEP_LENGTH = Float.parseFloat(string);
							break;
						case "txtInitialVariance":
							Globals.INITIAL_VARIANCE = Float.parseFloat(string);
							break;
						case "txtSystemNoise00":
							Globals.SYSTEM_NOISE.setEntry(0, 0, Float.parseFloat(string));
							break;
						case "txtSystemNoise01":
							Globals.SYSTEM_NOISE.setEntry(0, 1, Float.parseFloat(string));
							break;
						case "txtSystemNoise10":
							Globals.SYSTEM_NOISE.setEntry(1, 0, Float.parseFloat(string));
							break;
						case "txtSystemNoise11":
							Globals.SYSTEM_NOISE.setEntry(1, 1, Float.parseFloat(string));
							break;
						case "txtMeasurementsNoise00":
							Globals.MEASUREMENTS_NOISE.setEntry(0, 0, Float.parseFloat(string));
							break;
						case "txtMeasurementsNoise01":
							Globals.MEASUREMENTS_NOISE.setEntry(0, 1, Float.parseFloat(string));
							break;
						case "txtMeasurementsNoise10":
							Globals.MEASUREMENTS_NOISE.setEntry(1, 0, Float.parseFloat(string));
							break;
						case "txtMeasurementsNoise11":
							Globals.MEASUREMENTS_NOISE.setEntry(1, 1, Float.parseFloat(string));
							break;
					}
					textField.setColor(Color.WHITE);
				} else {
					textField.setColor(Color.RED);
				}
			}
		};

		txtNumberOfParticles.setTextFieldListener(textFieldListener);
		txtParticlesToShow.setTextFieldListener(textFieldListener);
		txtStepLength.setTextFieldListener(textFieldListener);
		txtInitialVariance.setTextFieldListener(textFieldListener);
		txtSystemNoise00.setTextFieldListener(textFieldListener);
		txtSystemNoise01.setTextFieldListener(textFieldListener);
		txtSystemNoise10.setTextFieldListener(textFieldListener);
		txtSystemNoise11.setTextFieldListener(textFieldListener);
		txtMeasurementsNoise00.setTextFieldListener(textFieldListener);
		txtMeasurementsNoise01.setTextFieldListener(textFieldListener);
		txtMeasurementsNoise10.setTextFieldListener(textFieldListener);
		txtMeasurementsNoise11.setTextFieldListener(textFieldListener);

		//Filters
		TextField.TextFieldFilter integer = new TextField.TextFieldFilter() {
			@Override
			public boolean acceptChar(TextField textField, char c) {
				return Character.toString(c).matches("^[0-9]");
			}
		};

		TextField.TextFieldFilter decimal = new TextField.TextFieldFilter() {
			@Override
			public boolean acceptChar(TextField textField, char c) {
				return Character.toString(c).matches("^[0-9.]");
			}
		};

		txtNumberOfParticles.setTextFieldFilter(integer);
		txtParticlesToShow.setTextFieldFilter(integer);
		txtStepLength.setTextFieldFilter(decimal);
		txtInitialVariance.setTextFieldFilter(decimal);
		txtSystemNoise00.setTextFieldFilter(decimal);
		txtSystemNoise01.setTextFieldFilter(decimal);
		txtSystemNoise10.setTextFieldFilter(decimal);
		txtSystemNoise11.setTextFieldFilter(decimal);
		txtMeasurementsNoise00.setTextFieldFilter(decimal);
		txtMeasurementsNoise01.setTextFieldFilter(decimal);
		txtMeasurementsNoise10.setTextFieldFilter(decimal);
		txtMeasurementsNoise11.setTextFieldFilter(decimal);

		//Window
		Window window = new Window("Preferences", skin);
		window.add(lblPositioningAlgorithms).uniform();
		window.add(lblNumberOfParticles).uniform();
		window.add(txtNumberOfParticles).uniform();
		window.row();

		window.add(sbPositioningAlgorithms).uniform();
		window.add(lblParticlesToShow).uniform();
		window.add(txtParticlesToShow).uniform();
		window.row();

		window.add(lblLocation).uniform();
		window.add(lblStepLength).uniform();
		window.add(txtStepLength).uniform();
		window.row();

		window.add(cbPoints).align(Align.left).uniform();
		window.add(lblInitialVariance).uniform();
		window.add(txtInitialVariance).uniform();
		window.row();

		window.add(cbRss).align(Align.left).uniform();
		window.add(lblSystemNoise).expand().colspan(2);
		window.row();

		window.add(cbMagnetic).align(Align.left).uniform();
		window.add(txtSystemNoise00).uniform();
		window.add(txtSystemNoise01).uniform();
		window.row();

		window.add(cbFused).align(Align.left).uniform();
		window.add(txtSystemNoise10).uniform();
		window.add(txtSystemNoise11).uniform();
		window.row();

		window.add(cbPF).align(Align.left).uniform();
		window.add(lblMeasurementNoise).expand().colspan(2);
		window.row();

		window.add(lblTotalPoints).uniform();
		window.add(txtMeasurementsNoise00).uniform();
		window.add(txtMeasurementsNoise01).uniform();
		window.row();

		Table table = new Table();
		table.add(btnDebug).uniform();
		table.add(btnStep).uniform();

		window.add(table).uniform();
		window.add(txtMeasurementsNoise10).uniform();
		window.add(txtMeasurementsNoise11).uniform();

		window.row();
		window.add(lblErrors).align(Align.right);

		window.row();
		window.add(lblRssError).uniform().align(Align.left);
		window.add(lblMagneticError).uniform().align(Align.left);
		window.row();
		window.add(lblFusedError).uniform().align(Align.left);
		window.add(lblParticleFilterError).uniform().align(Align.left);

		window.pack();
		window.setPosition(0, Gdx.graphics.getHeight());
		Globals.UI_STAGE.addActor(window);
	}

	private void log() {
		LogSample logSample = new LogSample();

		logSample.timeStamp = System.currentTimeMillis();
		logSample.x = logMarker.getX();
		logSample.y = logMarker.getY();
		logSample.orientation = Globals.ORIENTATION;
		logSample.status = Globals.STATUS.getStatus();

		for (LogRecord wifi : Globals.WIFI_LIST) {
			logSample.BSSID.add(wifi.getBSSID());
			logSample.level.add(wifi.getLevel());
		}

		logSample.magnetic.addAll(Globals.MAGNETIC_FIELD);

		try {
			Globals.RECORDED_SAMPLES++;
			samples.add(logSample.clone());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void navigate() {
		rssLocation = positioningAlgorithms.getRssLocation();

		if (rssLocation != null) {
			if (Globals.SHOW_MARKER_LOCATION[0]) {
				if (rssMarker == null) {
					rssMarker = new MarkerLocation((float) rssLocation[0][0], (float) rssLocation[1][0], "rss-marker.png");
				} else {
					rssMarker.moveToLocation((float) rssLocation[0][0], (float) rssLocation[1][0]);
				}
				Globals.GAME_STAGE.addActor(rssMarker);
			}
			if (Gdx.app.getType() == Desktop) {
				Globals.rssPoints.add(new Vector2((float) rssLocation[0][0], (float) rssLocation[1][0]));
				lblRssError.setText("Rss: " + calculateError(rssLocation));
			}
			if (Globals.SELECTED_ALGORITHM != 1)
				positioningAlgorithms.calculateCovarianceMatrix("rss");
		}

		magneticLocation = positioningAlgorithms.getMagneticLocation();

		if (magneticLocation != null) {
			if (Globals.SHOW_MARKER_LOCATION[1]) {
				if (magneticMarker == null) {
					magneticMarker = new MarkerLocation((float) magneticLocation[0][0], (float) magneticLocation[1][0], "magnetic-marker.png");
				} else {
					magneticMarker.moveToLocation((float) magneticLocation[0][0], (float) magneticLocation[1][0]);
				}
				Globals.GAME_STAGE.addActor(magneticMarker);
			}
			if (Gdx.app.getType() == Desktop) {
				Globals.magPoints.add(new Vector2((float) magneticLocation[0][0], (float) magneticLocation[1][0]));
				lblMagneticError.setText("Magnetic: " + calculateError(magneticLocation));
			}
			if (Globals.SELECTED_ALGORITHM != 1)
				positioningAlgorithms.calculateCovarianceMatrix("magnetic");
		}

		if (Globals.SELECTED_ALGORITHM != 1 && rssLocation != null && magneticLocation != null) {
			fusionEngineLocation = positioningAlgorithms.getFusionEngineLocation();

			if (fusionEngineLocation != null) {
				if (Globals.SHOW_MARKER_LOCATION[2]) {
					if (fusedMarker == null) {
						fusedMarker = new MarkerLocation((float) fusionEngineLocation[0][0], (float) fusionEngineLocation[1][0], "fused-marker.png");
					} else {
						fusedMarker.moveToLocation((float) fusionEngineLocation[0][0], (float) fusionEngineLocation[1][0]);
					}
					Globals.GAME_STAGE.addActor(fusedMarker);
				}
				if (Gdx.app.getType() == Desktop) {
					Globals.fePoints.add(new Vector2((float) fusionEngineLocation[0][0], (float) fusionEngineLocation[1][0]));
					lblFusedError.setText("Fused: " + calculateError(fusionEngineLocation));
				}
			}
		}

		if (Globals.NAVIGATOR && Globals.SHOW_MARKER_LOCATION[3]) {
			if (Globals.SELECTED_ALGORITHM != 1 && rssLocation != null && magneticLocation != null && fusionEngineLocation != null) {
				particleFilterLocation = positioningAlgorithms.getParticleFilterLocation();

				if (particleFilterLocation != null) {
					if (pfMarker == null) {
						pfMarker = new MarkerLocation((float) particleFilterLocation[0][0], (float) particleFilterLocation[1][0], "pf-marker.png");
					} else {
						pfMarker.moveToLocation((float) particleFilterLocation[0][0], (float) particleFilterLocation[1][0]);
					}
					Globals.GAME_STAGE.addActor(pfMarker);
					if (Gdx.app.getType() == Desktop) {
						Globals.pfPoints.add(new Vector2((float) particleFilterLocation[0][0], (float) particleFilterLocation[1][0]));
						lblParticleFilterError.setText("Particle Filter: " + calculateError(particleFilterLocation));
					}
				}
			}
		}
	}

	private void rotateMarkers() {
		//TODO Rotate markers
	}

	private void drawLines() {
		if (Gdx.app.getType() == Desktop) {

			shapeRenderer.setProjectionMatrix(Globals.CAMERA.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

			for (int i = 0; i < Globals.rlPoints.size(); i++) {
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.circle(Globals.rlPoints.get(i).x, Globals.rlPoints.get(i).y, 0.125f, 30);

				if (Globals.SHOW_MARKER_LOCATION[0]) {
					shapeRenderer.setColor(Color.RED);
					shapeRenderer.circle(Globals.rssPoints.get(i).x, Globals.rssPoints.get(i).y, 0.125f, 30);
				}

				if (Globals.SHOW_MARKER_LOCATION[1]) {
					shapeRenderer.setColor(Color.MAGENTA);
					shapeRenderer.circle(Globals.magPoints.get(i).x, Globals.magPoints.get(i).y, 0.125f, 30);
				}
			}

			shapeRenderer.setColor(Color.BLUE);
			if (Globals.SHOW_MARKER_LOCATION[2]) {
				for (int i = 0; i < Globals.fePoints.size(); i++) {
					shapeRenderer.circle(Globals.fePoints.get(i).x, Globals.fePoints.get(i).y, 0.125f, 30);
				}
			}

			shapeRenderer.end();
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

			for (int i = 1; i < Globals.rlPoints.size(); i++) {
				shapeRenderer.setColor(Color.BLACK);
				shapeRenderer.line(Globals.rlPoints.get(i - 1), Globals.rlPoints.get(i));

				if (Globals.SHOW_MARKER_LOCATION[0]) {
					shapeRenderer.setColor(Color.RED);
					shapeRenderer.line(Globals.rssPoints.get(i - 1), Globals.rssPoints.get(i));
				}

				if (Globals.SHOW_MARKER_LOCATION[1]) {
					shapeRenderer.setColor(Color.MAGENTA);
					shapeRenderer.line(Globals.magPoints.get(i - 1), Globals.magPoints.get(i));
				}
			}

			if (Globals.SHOW_MARKER_LOCATION[2]) {
				shapeRenderer.setColor(Color.BLUE);
				for (int i = 1; i < Globals.fePoints.size(); i++) {
					shapeRenderer.line(Globals.fePoints.get(i - 1), Globals.fePoints.get(i));
				}
			}

			if (Globals.SHOW_MARKER_LOCATION[3]) {
				shapeRenderer.setColor(Color.GREEN);
				for (int i = 1; i < Globals.pfPoints.size(); i++) {
					shapeRenderer.line(Globals.pfPoints.get(i - 1), Globals.pfPoints.get(i));
				}
			}

			shapeRenderer.end();
		}
	}

	private void drawPoints() {
		if (!Globals.LOGGER) {
			disPoints.clear();
		}

		shapeRenderer.setProjectionMatrix(Globals.CAMERA.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		shapeRenderer.setColor(Color.BLUE);
		for (int i = 0; i < disPoints.size(); i++) {
			shapeRenderer.circle(disPoints.get(i).x, disPoints.get(i).y, 0.125f, 30);
		}

		if (Globals.SHOW_RECORDED_POINTS) {
			shapeRenderer.setColor(Color.RED);
			for (String location : positioningAlgorithms.rssRadioMap.getRssLocationHashMap().keySet()) {
				shapeRenderer.circle(Float.parseFloat(location.split(" ")[0]), Float.parseFloat(location.split(" ")[1]), 0.125f, 30);
			}
		}
		shapeRenderer.end();
	}

	private void drawLabels() {
		spriteBatch.begin();

		if (Gdx.app.getType() == Android)
			bitmapFont.draw(spriteBatch, "AP: " + Globals.ACCESS_POINTS, Gdx.graphics.getWidth() - 250, (Globals.LOGGER) ? Gdx.graphics.getHeight() - 25 : Gdx.graphics.getHeight() - 193);
		else if (Gdx.app.getType() == Desktop)
			bitmapFont.draw(spriteBatch, "AP: " + Globals.ACCESS_POINTS, Gdx.graphics.getWidth() - 125, Gdx.graphics.getHeight() - 12.5f);

		if (Globals.LOGGER) {
			bitmapFont.draw(spriteBatch, String.format(Locale.getDefault(), "Distance: %.2f", touchDistance), 10, (Gdx.app.getType() == Android) ? 250 : 145);
			bitmapFont.draw(spriteBatch, String.format(Locale.getDefault(), "X1: %.2f Y1: %.2f", firstPoint.x, firstPoint.y), 10, (Gdx.app.getType() == Android) ? 175 : 107.5f);
			bitmapFont.draw(spriteBatch, String.format(Locale.getDefault(), "X2: %.2f Y2: %.2f", secondPoint.x, secondPoint.y), 10, (Gdx.app.getType() == Android) ? 100 : 70);
		}

		spriteBatch.end();
	}

	private void writeErrors() {
		if (Gdx.app.getType() == Android)
			return;

		try {
			writer.write(Globals.REAL_LOCATION[0][0] + " " + Globals.REAL_LOCATION[1][0] + " " +
					lblRssError.getText().toString().split(": ")[1] + " " +
					lblMagneticError.getText().toString().split(": ")[1] + " " +
					lblFusedError.getText().toString().split(": ")[1] + " " +
					lblParticleFilterError.getText().toString().split(": ")[1] + "\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void drawEllipses() {
		double[][] matrix;

		shapeRenderer.setProjectionMatrix(Globals.CAMERA.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

		if (Globals.SELECTED_ALGORITHM != 1) {
			float width, height, rotation;

			if (Globals.SHOW_MARKER_LOCATION[0]) {
				matrix = positioningAlgorithms.getRssCovarianceMatrix();

				if (matrix != null) {
					width = (float) matrix[0][0];
					height = (float) matrix[1][1];
					rotation = (float) matrix[0][1];

					if (rssMarker != null) {
						shapeRenderer.setColor(Color.RED);
						shapeRenderer.ellipse(rssMarker.body.getPosition().x - width / 2.0f, rssMarker.body.getPosition().y - height / 2.0f, width, height, rotation, 50);
					}
				}
			}

			if (Globals.SHOW_MARKER_LOCATION[1]) {
				matrix = positioningAlgorithms.getMagneticCovarianceMatrix();

				if (matrix != null) {
					width = (float) matrix[0][0];
					height = (float) matrix[1][1];
					rotation = (float) matrix[0][1];

					if (magneticMarker != null) {
						shapeRenderer.setColor(Color.MAGENTA);
						shapeRenderer.ellipse(magneticMarker.body.getPosition().x - width / 2.0f, magneticMarker.body.getPosition().y - height / 2.0f, width, height, rotation, 50);
					}
				}
			}

			if (Globals.SHOW_MARKER_LOCATION[2]) {
				matrix = positioningAlgorithms.getFusionEngineCovarianceMatrix();

				if (matrix != null) {
					width = (float) matrix[0][0];
					height = (float) matrix[1][1];
					rotation = (float) matrix[0][1];

					if (fusedMarker != null) {
						shapeRenderer.setColor(Color.BLUE);
						shapeRenderer.ellipse(fusedMarker.body.getPosition().x - width / 2.0f, fusedMarker.body.getPosition().y - height / 2.0f, width, height, rotation, 50);
					}
				}
			}
		}

		shapeRenderer.end();
	}

	private void drawParticles() {
		if (!Globals.SHOW_MARKER_LOCATION[3] || particleFilterLocation == null)
			return;

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		shapeRenderer.setColor(Color.GREEN);

		if (Globals.PARTICLES_NUMBER <= Globals.PARTICLES_NUMBER_TO_SHOW) {
			for (int i = 0; i < Globals.PARTICLES_NUMBER; i++) {
				float xPos = Globals.PARTICLES.get(i).body.getPosition().x;
				float yPos = Globals.PARTICLES.get(i).body.getPosition().y;
				shapeRenderer.circle(xPos, yPos, 0.05f, 30);
			}
		} else {
			int counter = 0;
			for (int i = 0; i < Globals.PARTICLES_NUMBER; i++) {
				Random random = new Random();
				if (counter == Globals.PARTICLES_NUMBER_TO_SHOW)
					break;

				if (random.nextBoolean()) {
					float xPos = Globals.PARTICLES.get(i).body.getPosition().x;
					float yPos = Globals.PARTICLES.get(i).body.getPosition().y;
					shapeRenderer.circle(xPos, yPos, 0.05f, 30);
					counter++;
				}
			}
		}
		shapeRenderer.end();
	}

	private void deleteMarkers() {
		if (!Globals.NAVIGATOR) {
			if (rssMarker != null) {
				rssMarker.eliminate();
				rssMarker = null;
				rssLocation = null;
			}

			if (magneticMarker != null) {
				magneticMarker.eliminate();
				magneticMarker = null;
				magneticLocation = null;
			}

			if (fusedMarker != null) {
				fusedMarker.eliminate();
				fusedMarker = null;
				fusionEngineLocation = null;
			}

			if (pfMarker != null) {
				pfMarker.eliminate();
				pfMarker = null;
				particleFilterLocation = null;
				positioningAlgorithms.is_pf_initialized = false;
			}
		}
	}

	private void writeLogFiles() {
		if (writeHeading) {
			writeHeading = false;
			magneticFile.writeString(Globals.MAGNETIC_HEADING, true);
		}

		for (LogSample logger : samples) {
			//Rss File
			rssFile.writeString(Globals.RSS_HEADING, true);
			for (int i = 0; i < logger.BSSID.size(); i++) {
				rssFile.writeString(logger.timeStamp + " " + logger.x + " " + logger.y + " " + logger.orientation + " " + logger.BSSID.get(i) + " " + logger.level.get(i) + "\n", true);
			}

			//Magnetic File
			magneticFile.writeString(logger.timeStamp + " " + logger.x + " " + logger.y + " " + logger.orientation + " " + logger.magnetic.get(0) + " " + logger.magnetic.get(1) + " " + logger.magnetic.get(2) + "\n", true);
		}
	}

	private void createBitMapFont() {
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Amble-Light.ttf"));
		FreeTypeFontGenerator.FreeTypeFontParameter parameters = new FreeTypeFontGenerator.FreeTypeFontParameter();

		if (Gdx.app.getType() == Android)
			parameters.size = 80;
		else if (Gdx.app.getType() == Desktop)
			parameters.size = 40;

		parameters.color = Color.RED;
		bitmapFont = generator.generateFont(parameters);

		generator.dispose();
	}

	private void distributePointsInLine() {
		if (shouldDistribute) {
			float xVelocity, yVelocity;
			float timeDiff, xDiff, yDiff;

			LogSample fLogger = samples.get(0);
			LogSample sLogger = samples.get(Globals.RECORDED_SAMPLES - 1);

			xDiff = sLogger.x - fLogger.x;
			yDiff = sLogger.y - fLogger.y;
			timeDiff = sLogger.timeStamp - fLogger.timeStamp;

			xVelocity = xDiff / timeDiff;
			yVelocity = yDiff / timeDiff;

			Vector2 lastWalking = new Vector2(fLogger.x, fLogger.y);

			for (int i = 0; i < samples.size() - 1; i++) {
				LogSample logSample = samples.get(i);

				if (logSample.status.equals("Walking")) {
					logSample.x = fLogger.x + xVelocity * (logSample.timeStamp - fLogger.timeStamp);
					logSample.y = fLogger.y + yVelocity * (logSample.timeStamp - fLogger.timeStamp);

					lastWalking.set(logSample.x, logSample.y);
				} else {
					if (samples.get(i + 1).status.equals("Standing")) {
						if (logSample.x != samples.get(i + 1).x || logSample.y != samples.get(i + 1).y) {
							lastWalking.set(samples.get(i + 1).x, samples.get(i + 1).y);
							continue;
						}
					}
					logSample.x = lastWalking.x;
					logSample.y = lastWalking.y;
				}
			}

			for (LogSample logSample : samples) {
				Vector2 v = new Vector2(logSample.x, logSample.y);
				if (!disPoints.contains(v)) {
					disPoints.add(v);
				}
			}

			writeLogFiles();
			samples.clear();
			shouldDistribute = false;
			Globals.RECORDED_SAMPLES = 0;
		}
	}

	private String calculateError(double[][] location) {
		if (location == null)
			return "NaN";
		DecimalFormat decimalFormat = new DecimalFormat("#.####");
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();

		decimalFormatSymbols.setDecimalSeparator('.');
		decimalFormat.setRoundingMode(RoundingMode.CEILING);
		decimalFormat.setDecimalFormatSymbols(decimalFormatSymbols);

		return String.valueOf(decimalFormat.format(Math.sqrt(Math.pow(Globals.REAL_LOCATION[0][0] - location[0][0], 2) + Math.pow(Globals.REAL_LOCATION[1][0] - location[1][0], 2))));
	}

	//Screen
	@Override
	public void show() {

	}

	private float timeSeconds = 0f;
	private float period = 4f;
	private boolean START;

	@Override
	public void render(float delta) {
		if (START) {
			timeSeconds +=Gdx.graphics.getRawDeltaTime();
			if(timeSeconds > period) {
				timeSeconds -= period;
				Globals.SHOW_NEXT = true;
			}
		}

		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (Gdx.app.getType() == Desktop) {
			Globals.UI_STAGE.act();
			Globals.UI_STAGE.draw();
		}

		Globals.GAME_STAGE.act();
		Globals.GAME_STAGE.draw();

		if (!Globals.NEW_SCAN_AVAILABLE) {
			wiFiMagneticListener.scan();
		}

		if ((Globals.NEW_SCAN_AVAILABLE && Globals.ONE_TIME_SCAN)) {
			Globals.NEW_SCAN_AVAILABLE = false;
			Globals.ONE_TIME_SCAN = false;

			Globals.ACCESS_POINTS = Globals.WIFI_LIST.size();

			if (Globals.LOGGER) {
				if (Globals.ENABLE_LOGGING && Globals.CAN_DISTRIBUTE) {
					log();
					distributePointsInLine();
				}
			}

			if (Globals.NAVIGATOR && Globals.TOTAL_POINTS != -1) {
				navigate();
				writeErrors();
			}
		}

		if (!Globals.LOGGER) {
			touchCount = 0;
			logMarker.remove();
			shouldDistribute = false;
			touchDistance = Float.NaN;
			Globals.CAN_DISTRIBUTE = false;
			firstPoint.set(Float.NaN, Float.NaN, 0);
			secondPoint.set(Float.NaN, Float.NaN, 0);
		}

		drawLines();
		drawPoints();
		drawLabels();
		drawEllipses();
		drawParticles();
		deleteMarkers();

		if (Globals.DEBUG)
			Globals.RENDERER.render(Globals.WORLD, Globals.GAME_STAGE.getCamera().combined);

		accumulator += Math.min(delta, 0.25f);

		if (accumulator >= Globals.TIME_STEP) {
			accumulator -= Globals.TIME_STEP;
			Globals.WORLD.step(Globals.TIME_STEP, Globals.VELOCITY_ITERATIONS, Globals.POSITION_ITERATIONS);
		}
	}

	@Override
	public void resize(int width, int height) {
		if (Gdx.app.getType() == Desktop)
			Globals.UI_STAGE.getViewport().update(width, height);
		Globals.GAME_STAGE.getViewport().update(width, height);
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
		Globals.WORLD.dispose();
		Globals.RENDERER.dispose();
		Globals.UI_STAGE.dispose();
		Globals.GAME_STAGE.dispose();
	}

	//GestureDetector.GestureListener
	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		if (Globals.LOGGER) {
			touchCount++;

			if (touchCount == 2) {
				touchCount = 0;
				secondPoint.x = x;
				secondPoint.y = y;
				Globals.CAMERA.unproject(secondPoint);
			} else if (touchCount == 1) {
				firstPoint.x = x;
				firstPoint.y = y;
				Globals.CAMERA.unproject(firstPoint);
			}

			touchDistance = firstPoint.dst(secondPoint);

			Vector3 v = new Vector3(x, y, 0);
			Globals.CAMERA.unproject(v);

			if (Globals.ENABLE_LOGGING && Globals.CAN_DISTRIBUTE) shouldDistribute = true;
			else if (!Globals.CAN_DISTRIBUTE) Globals.CAN_DISTRIBUTE = true;

			Gdx.input.vibrate(250);
			logMarker.setMarkerPosition(v.x, v.y);
			Globals.GAME_STAGE.addActor(logMarker);
		}
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		//TODO Need to find a better way!!
		double angle = Math.atan2(Globals.CAMERA.up.x, Globals.CAMERA.up.y) * MathUtils.radiansToDegrees;

		float dx = (deltaX * Globals.CURRENT_ZOOM);
		float dy = (deltaY * Globals.CURRENT_ZOOM);

		if (angle > -45.0f && angle < 45.0f)
			Globals.CAMERA.translate(-dx, dy);
		else if (angle > -135.0f && angle < -45.0f)
			Globals.CAMERA.translate(-dy, -dx);
		else if (angle > 135.0f || angle < -135.0f)
			Globals.CAMERA.translate(dx, -dy);
		else if (angle > 45.0f && angle < 135.0f)
			Globals.CAMERA.translate(dy, dx);
		Globals.CAMERA.update();
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		Globals.CURRENT_ZOOM = Globals.CAMERA.zoom;
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		Globals.CAMERA.zoom = MathUtils.clamp((initialDistance / distance) * Globals.CURRENT_ZOOM, Globals.ZOOM_IN, Globals.ZOOM_OUT);
		Globals.CAMERA.update();
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		//TODO Try finding anything better.
		float delta1X = pointer2.x - pointer1.x;
		float delta1Y = pointer2.y - pointer1.y;

		float newAngle = (float) Math.atan2((double) delta1Y, (double) delta1X) * MathUtils.radiansToDegrees;

		if (newAngle < 0.0f) {
			newAngle = 360.0f - (-newAngle);
		}

		float rotationAngle = 1.0f;
		float thresholdAngle = 0.5f;
		if (newAngle - currentAngle >= thresholdAngle) {
			Globals.CAMERA.rotate(-rotationAngle);
		} else if (newAngle - currentAngle <= -thresholdAngle) {
			Globals.CAMERA.rotate(rotationAngle);
		}

		if (Math.abs(newAngle - currentAngle) >= thresholdAngle) {
			currentAngle = newAngle;
		}
		return false;
	}

	@Override
	public void pinchStop() {

	}

	//InputProcessor
	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Input.Keys.SPACE)
			if (Globals.SELECTED_ALGORITHM == 2)
				START = true;
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		if (character == ' ')
			if (Globals.SELECTED_ALGORITHM != 2)
				Globals.SHOW_NEXT = true;
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == 0) {
			canDrag = true;
			touchDragged(screenX, screenY, pointer);
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (Globals.LOGGER) {
			if (button == 1) {
				touchCount++;

				if (touchCount == 2) {
					touchCount = 0;
					secondPoint.x = screenX;
					secondPoint.y = screenY;
					Globals.CAMERA.unproject(secondPoint);
				} else if (touchCount == 1) {
					firstPoint.x = screenX;
					firstPoint.y = screenY;
					Globals.CAMERA.unproject(firstPoint);
				}

				touchDistance = firstPoint.dst(secondPoint);

				Vector3 v = new Vector3(screenX, screenY, 0);
				Globals.CAMERA.unproject(v);

				logMarker.setMarkerPosition(v.x, v.y);
				Globals.GAME_STAGE.addActor(logMarker);

			} else if (button == 2) {
				touchCount = 0;
				logMarker.remove();
				touchDistance = Float.NaN;
				firstPoint.set(Float.NaN, Float.NaN, 0);
				secondPoint.set(Float.NaN, Float.NaN, 0);
			}
		}
		canDrag = false;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (canDrag)
			Globals.CAMERA.translate(-Gdx.input.getDeltaX() * Globals.CURRENT_ZOOM, Gdx.input.getDeltaY() * Globals.CURRENT_ZOOM);
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		float zoom = ((amount == 1) ? .02f : -.02f) + Globals.CAMERA.zoom;
		Globals.CAMERA.zoom = MathUtils.clamp(zoom, Globals.ZOOM_IN, Globals.ZOOM_OUT);
		Globals.CURRENT_ZOOM = Globals.CAMERA.zoom;
		return false;
	}
}