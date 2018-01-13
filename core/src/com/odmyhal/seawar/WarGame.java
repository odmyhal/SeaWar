package com.odmyhal.seawar;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.bircks.enterprise.control.panel.InteractiveController;
import org.bircks.enterprise.control.panel.InvisiblePanel;
import org.bircks.enterprise.control.panel.Skinner;
import org.bircks.enterprise.control.panel.camera.CameraPanel;
import org.bircks.entierprise.model.ModelStorage;
import org.bricks.core.entity.Fpoint;
import org.bricks.engine.Engine;
import org.bricks.engine.Motor;
import org.bricks.engine.SecuredMotor;
import org.bricks.engine.event.check.AccelerateToSpeedProcessorChecker;
import org.bricks.engine.event.check.DurableRouteChecker;
import org.bricks.engine.item.Stone;
import org.bricks.engine.pool.World;
import org.bricks.engine.processor.Processor;
import org.bricks.engine.processor.SingleActProcessor;
import org.bricks.engine.processor.tool.TimerApprover;
import org.bricks.engine.tool.Origin2D;
//import org.bricks.enterprise.inform.Informator;
import org.bricks.extent.debug.ShapeDebugger;
import org.bricks.extent.effects.BricksParticleSystem;
import org.bricks.extent.entity.CameraSatellite;
import org.bricks.extent.tool.CameraHelper;
import org.bricks.extent.tool.ModelHelper;
import org.bricks.utils.Cache;
import org.bricks.extent.interact.SpaceInteract;
import org.bricks.extent.interact.InteractiveHandler;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.batches.ModelInstanceParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.batches.PointSpriteParticleBatch;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import com.odmyhal.sf.bot.ShipFightProcessor;
import com.odmyhal.sf.control.ShipMovePanel;
import com.odmyhal.sf.effects.DustEffect;
import com.odmyhal.sf.interact.ShipTouchHandler;
import com.odmyhal.sf.interact.StoneTouchHandler;
//import com.odmyhal.sf.model.Ball;
import com.odmyhal.sf.model.shader.WaveNativeShader;
import com.odmyhal.sf.model.Island;
import com.odmyhal.sf.model.ShaderWaver;
import com.odmyhal.sf.model.bubble.BlabKeeper;
import com.odmyhal.sf.model.construct.ShipConstructor;
import com.odmyhal.sf.model.shader.WaveShaderProvider;
import com.odmyhal.sf.process.DropBubbleProcessor;
import com.odmyhal.sf.process.ShipGunHRollProcessor;
import com.odmyhal.sf.process.ShipGunVRollProcessor;
import com.odmyhal.sf.staff.Ammunition;
import com.odmyhal.sf.staff.CameraShip;
import com.odmyhal.sf.staff.Ship;

import com.odmyhal.sf.staff.CameraShip;
import com.odmyhal.sf.staff.Ship;

public class WarGame extends ApplicationAdapter {
	private Engine engine;
	private InteractiveController interactiveController;
	private ModelBatch modelBatch;
	private Environment environment;
	private DirectionalLight dirLight;
	private Vector3 tmpDirection = new Vector3();
	private Camera camera;
	ShaderWaver waver;
	CameraSatellite cameraSatellite;
	
	ShapeDebugger debug;
	private static final boolean DEBUG_ENABLED = false;
	private static final boolean SPACE_DEBUG_ENABLED = false;
	
	private boolean MOTOR_SECURED = false;
	private String ERROR_LOG_FILE = "/mnt/sdcard/oleh/seawar.error.log";
	private String JUST_LOG_FILE = "/mnt/sdcard/oleh/seawar.log";
	
//	private ParticleSystem particleSystem;
	
	private CameraShip ship;
	private Ship testo;

	private float totalDeltaTime = 0;
	private int totalFramesCount = 0;
	
	AssetManager assets;

	private SeawarRenderer renderer;
	private SeawarRenderer firstRenderer;
	
	@Override
	public void create(){
		
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		
//		createSeafight();
//		renderer = new NormalRenderer();
		
		renderer = new SeawarRenderer(){
			
			private int tmpWidth = 1, tmpHeight;
			private int needCreateSeafight = 0;
			
			private SpriteBatch spriteBatch = new SpriteBatch();
	        private BitmapFont font = new BitmapFont();
	        private String info = "LOADING...";
			//private StringBuffer info = new StringBuffer("LOADING");
	        private GlyphLayout glyphLayout = new GlyphLayout();
	        private OrthographicCamera frmCam = new OrthographicCamera(tmpWidth, tmpHeight);
			
			@Override
			public void render() {
				if(needCreateSeafight < 6){
					Gdx.gl.glClearColor(0.9f, 0.9f, 1f, 0.5f);
					Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
					
					font.setColor(Color.NAVY);
/*
					FileHandle dhandle = Gdx.files.absolute(JUST_LOG_FILE);

					if(dhandle.exists()){
						info = " File found";
					}else{
						info = " Not exist :( " + dhandle.exists();
						try {
							dhandle.writeString("initial", true);
						}catch(Exception e){
							info = " error ^(: " + e.getMessage();
						}

					}
*/
					glyphLayout.setText(font, info);
					//info.append('.');
					font.getData().setScale(tmpWidth / (2 * glyphLayout.width));
					
					spriteBatch.setProjectionMatrix(frmCam.combined);
					spriteBatch.begin();
					font.draw(spriteBatch, info, tmpWidth / 4, glyphLayout.height + tmpHeight / 2);
					spriteBatch.end();
					
					if(++needCreateSeafight == 6){
						if(DEBUG_ENABLED)
							log(JUST_LOG_FILE, "----------------------------", false);
						createSeafight();
						renderer = new NormalRenderer();
						if(DEBUG_ENABLED)
							log(JUST_LOG_FILE, "created normal renderer", true);
						WarGame.this.resize(tmpWidth, tmpHeight);
						if(DEBUG_ENABLED)
							log(JUST_LOG_FILE, "Seawar renderer complete", true);
						//pausa();
						//spriteBatch.dispose();
						//font.dispose();
						if(DEBUG_ENABLED)
							log(JUST_LOG_FILE, "------------------------------------------", true);
					}
				}
			}

			public void dispose(){
				spriteBatch.dispose();
				font.dispose();
			}

			@Override
			public void resize(int width, int height) {
				tmpWidth = width;
				tmpHeight = height;
				frmCam.viewportWidth = width;
				frmCam.viewportHeight = height;
				frmCam.translate(width / 2, height / 2);
				frmCam.update();
			}
			
		};

		firstRenderer = renderer;
	}
	
	private void createSeafight(){
		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "strat create fight", true);
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "before register", true);

		registerCachedClasses();

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "after register", true);
		
		assets = new AssetManager();
		environment = new Environment();
        
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
//        dirLight = new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f);
        dirLight = new DirectionalLight().set(0.8f, 0.8f, 0.8f, 1f, 0f, -0.4f);
        environment.add(dirLight);
        
//        Informator.init();
/*		try {
			Preferences.userRoot().clear();
		} catch (BackingStoreException e2) {
			this.logError(this.ERROR_LOG_FILE, e2, true);
		}*/

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "before prefs read", true);

		FileHandle fh = Gdx.files.internal("config/engine.prefs.xml");
		FileHandle confHandle = Gdx.files.internal("config/sf.config-defaults.xml");
		Preferences prefs = Preferences.userRoot().node("engine.settings");

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "settings read", true);
		try {
			prefs.clear();
		} catch (BackingStoreException e1) {
			Gdx.app.error("ERROR", "Could not clear preferences engine.settings");
		}
		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "before import preferences", true);
		try {
			Preferences.importPreferences(fh.read());
			Preferences.importPreferences(confHandle.read());
		} catch (IOException e) {
			Gdx.app.error("ERROR", "Could not read file " + fh.path(), e);
		} catch (InvalidPreferencesFormatException e) {
			Gdx.app.error("ERROR", "Could not parse file " + fh.path(), e);
		}
		MOTOR_SECURED = prefs.getBoolean("motor.secure.error", false);
		if(MOTOR_SECURED){
			Gdx.app.debug("Sea fight", "Motor secured....");
			//this.log(this.ERROR_LOG_FILE, "***************SEAWAR*STARTS*LOG*************************", true);
		}
		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "skinner init", true);
		Skinner.init();
		DustEffect.initialize();
		engine = new Engine(prefs);
//		engine.init(prefs);

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "engine created", true);

		assets.load("models/ship11.g3db", Model.class);
		assets.finishLoading();
		if(assets.update()){
			Gdx.app.debug("Sea fight", "Models are loaded");
		}

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "model loaded", true);
		ShipConstructor.setModel(new ModelInstance(assets.get("models/ship11.g3db", Model.class)));
		ModelStorage.instance().init(prefs.get("model.construct.tool", null));
		
		waver = new ShaderWaver();
		Motor m1 = engine.getLazyMotor();
		m1.addLiver(waver);

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "vawer loaded", true);

		initIslands();
		
//		olehTest();

//		Ball.setBlabKeeper(waver.blabKeeper);
		Ammunition.blabKeeper = waver.blabKeeper;
		

//		pausa();
		ship = new CameraShip(assets);
		//System.out.println("Ship origin x = " + ship.origin().source.getFX() + " , y = " + ship.origin().source.getFY());
		ship.registerEventChecker(new DropBubbleProcessor(waver.blabKeeper));
		
		Origin2D tmp2Origin = new Origin2D();
		tmp2Origin.set(3595, 6000);
		ship.translate(tmp2Origin);
		cameraSatellite = ship.initializeCamera();
		camera = cameraSatellite.camera;
		
//		initParticleSystem();
		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "before Particle system load", true);

		BricksParticleSystem.init();
		
		BricksParticleSystem.addBatch(new ModelInstanceParticleBatch());
		PointSpriteParticleBatch pointSpriteBatch = new PointSpriteParticleBatch();
		pointSpriteBatch.setTexture(DustEffect.dustTexture);
		pointSpriteBatch.setCamera(camera);
		BricksParticleSystem.addBatch(pointSpriteBatch);
		

		waver.setCamera(camera);
		ship.applyEngine(engine);

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "ship engine applied", true);
		
		initTouchInteract(ship, camera, engine.getWorld());
		
		interactiveController = new InteractiveController(SpaceInteract.instance());

		interactiveController.addPanel(new InvisiblePanel());

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "before camera set", true);
		
		CameraPanel cp = new CameraPanel(camera, cameraSatellite, "panel.defaults", "sf.camera.defaults");
		interactiveController.addPanel(cp);
	
		ShipMovePanel smp = new ShipMovePanel(ship);
		interactiveController.addPanel(smp);
		smp.setActive(true);

		modelBatch = new ModelBatch(new WaveShaderProvider(camera));
		//modelBatch = new ModelBatch(new DefaultShaderProvider());

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "set model batch camera", true);

		debug = new ShapeDebugger();
		engine.start();

//		pausa();
		initTestShip();
		initBotShips();
        Gdx.app.debug("Sea Fight", "game create passed");
        Gdx.app.debug("SEA FIGHT", "The density is: " + Gdx.graphics.getDensity());

		if(DEBUG_ENABLED)
			log(JUST_LOG_FILE, "finished create fight", true);
	}
	
	private void pausa(){
		try {
			Thread.currentThread().sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initTouchInteract(Ship ship, Camera camera, World world){
		StoneTouchHandler sth = new StoneTouchHandler(ship, camera.far);
		SpaceInteract.init(camera, world, sth);
		InteractiveHandler.init();
		InteractiveHandler.registerHandler(Stone.class, sth);
		InteractiveHandler.registerHandler(Ship.class, new ShipTouchHandler(ship));
	}
	
	private void initBotShips(){
/*		initRover(24000f, 24000f, waver.blabKeeper, new Fpoint(22000f, 22000f),
				new Fpoint(8000f, 19000f), new Fpoint(15000f, 9000f), new Fpoint(21000f, 15000f));*/
		initRover(24000f, 24000f, waver.blabKeeper, new Fpoint(22000f, 22000f),
				new Fpoint(10000f, 19000f), new Fpoint(14000f, -5300f), new Fpoint(25500f, 4000f));
//		pausa();
		initRover(24000f, 800f, waver.blabKeeper, new Fpoint(24000f, 2000f),
				new Fpoint(23500f, 9000f), new Fpoint(13000, 11000), new Fpoint(10000f, 8000f), new Fpoint(4000f, 3000f));

		initRover(-9100f, -8900f, waver.blabKeeper, new Fpoint(-9000f, -8500f),
				new Fpoint(-7050f, -3050f), new Fpoint(-15000, 33000), new Fpoint(-8050f, 5900f));
		
		initRover(22100f, -12740f, waver.blabKeeper, new Fpoint(25100f, -12740f),
				new Fpoint(43050f, 23900f), new Fpoint(-22100, 30100), new Fpoint(5000f, 20800f), new Fpoint(4000f, 0f));

		initRover(30000f, -18000f, waver.blabKeeper, new Fpoint(33000f, -18000f),
				new Fpoint(45000f, 0f), new Fpoint(8100f, 27100f), new Fpoint(-16400f, 3800f), new Fpoint(-2000f, -23000f));
		
		initRover(40000f, 29000f, waver.blabKeeper, new Fpoint(40000f, 33000f),
				new Fpoint(23000f, 42000f), new Fpoint(2100f, 33100f), new Fpoint(20400f, -5800f));
		
		initRover(-22000f, 35000f, waver.blabKeeper, new Fpoint(-17000f, 35000f),
				new Fpoint(3000f, 20000f), new Fpoint(810f, 900f), new Fpoint(-16400f, 2800f), new Fpoint(-22000f, 23000f));


	}
	
	private Ship initRover(final float tX, final float tY, final BlabKeeper bk, final Fpoint... route){
		Origin2D tmpOrigin = new Origin2D();
		Ship rover = new Ship(assets);
		tmpOrigin.set(tX, tY);
		rover.translate(tmpOrigin);
		rover.setHealth(100);
		rover.setToRotation((float) Math.PI * 5 / 4);
		rover.registerEventChecker(new AccelerateToSpeedProcessorChecker(50f, (float) (600f + 200f * Math.random())));
		
		rover.registerEventChecker(new DurableRouteChecker<Ship>((float) Math.PI/7, 700f, route));

		rover.applyEngine(engine);
		Thread.currentThread().yield();
		
		ShipGunHRollProcessor sgrp = new ShipGunHRollProcessor(rover, "pushka");
		sgrp.setButt(ship);
		rover.registerEventChecker(sgrp);
		
		ShipGunVRollProcessor sgvrp = new ShipGunVRollProcessor(rover, rover.mainGun, "stvol");
		sgvrp.setButt(ship);
		rover.registerEventChecker(sgvrp);
		
		ShipGunHRollProcessor sgrp_back = new ShipGunHRollProcessor(rover, "backPushka");
		sgrp_back.setButt(ship);
		rover.registerEventChecker(sgrp_back);
		
		ShipGunVRollProcessor sgvrp_back = new ShipGunVRollProcessor(rover, rover.backGun, "backStvol");
		sgvrp_back.setButt(ship);
		rover.registerEventChecker(sgvrp_back);
		
	
		ShipFightProcessor sfp = new ShipFightProcessor(new TimerApprover<Ship>(700), sgrp, sgvrp);
		rover.registerEventChecker(sfp);
	

		Processor<Ship> initNewRover = new SingleActProcessor<Ship>(){

			@Override
			protected boolean ready(Ship target, long processTime) {
				return true;
			}

			@Override
			protected void processSingle(Ship target, long processTime) {
				WarGame.this.initRover(tX, tY,  bk, route);
			}
			
		};
		rover.setBeforeGetOut(initNewRover);
		
		rover.registerEventChecker(new DropBubbleProcessor(bk));
		return rover;
	}
	
	private void initIslands(){
		
		Origin2D tmp2Origin = new Origin2D();

		initIsland(8000f, 10300f, 89, tmp2Origin);
		initIsland(17000f, 15000f, 180f, tmp2Origin);
		initIsland(20500f, 7600f, 254f, tmp2Origin);
		initIsland(0f, -12230f, 55f, tmp2Origin);
		initIsland(30230f, 42200f, 123f, tmp2Origin);
		initIsland(27230f, 31200f, 123f, tmp2Origin);
		initIsland(-7836, 36420f, 23f, tmp2Origin);
		initIsland(-17236, -11420f, 230f, tmp2Origin);
		initIsland(2236, -7420f, 241f, tmp2Origin);
		initIsland(-9236, 40420f, 341f, tmp2Origin);
		initIsland(7236, 32120f, 41f, tmp2Origin);
		initIsland(17736, 42420f, 56f, tmp2Origin);
		initIsland(15236, -13420f, 141f, tmp2Origin);
		
		initIsland(40000, -13420f, 39f, tmp2Origin);
		initIsland(45000, 11420f, 223f, tmp2Origin);
		initIsland(33600, 29120f, 311f, tmp2Origin);
		initIsland(-15600, 20120f, 11f, tmp2Origin);
		initIsland(-19670, -11120f, 250f, tmp2Origin);
		initIsland(-16670, 23120f, 250f, tmp2Origin);
	}
	
	private void initIsland(float x, float y, float rad, Origin2D tmp2Origin){
		Island island3 = Island.instance("island_1");
		tmp2Origin.source.set(x, y);
		island3.translate(tmp2Origin);
		ModelHelper.setToRotation(rad, island3);
		island3.applyEngine(engine);
	}
	
	private void initTestShip(){
		Origin2D tmpOrigin = new Origin2D();
		testo = new Ship(assets);
		tmpOrigin.set(15000, 6000);
		testo.translate(tmpOrigin);
		testo.setHealth(20);
//		testo.setToRotation((float) Math.PI * 5 / 4);
		testo.setToRotation((float) Math.PI / 2);
		testo.applyEngine(engine);
	}
	
	@Override
	public void render(){
		renderer.render();
	}

	boolean first = true;
	public void renderNormal(){

		
		if(first && DEBUG_ENABLED){
			log(JUST_LOG_FILE, "********************************************", true);
			log(JUST_LOG_FILE, "normal renderer started", true);
		}
		if(MOTOR_SECURED){
			Throwable cause = null;
			for(Throwable error: SecuredMotor.errors){
				cause = error;
			}
			if(cause != null){
				throw new RuntimeException("Game raised an error, see log: " + this.ERROR_LOG_FILE, cause);
			}
		}

		this.totalDeltaTime += Gdx.graphics.getDeltaTime();
		this.totalFramesCount++;

		if(first && DEBUG_ENABLED){
			log(JUST_LOG_FILE, "Delta time is set " + Gdx.graphics.getDeltaTime(), true);
		}

		Gdx.gl.glClearColor(0.9f, 0.9f, 1f, 0.5f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(Gdx.gl20.GL_LESS);
		
		Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
		Gdx.gl.glClearStencil(0);
		Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);
		
		tuneDirection();

		if(first && DEBUG_ENABLED){
			log(JUST_LOG_FILE, "Direction is tuned ", true);
		}

		CameraHelper.tuneWorldCamera(engine.getWorld(), camera);
		Iterable<RenderableProvider> entitiesPool = CameraHelper.getCameraRenderables();
		waver.setRenderDistricts(CameraHelper.getInCameraDistricts());
		modelBatch.begin(camera);

		if(first && DEBUG_ENABLED){
			log(JUST_LOG_FILE, "Camera is tuned", true);
		}

		try {
			modelBatch.render(waver, environment);
		}catch(Exception e){
			if(first && DEBUG_ENABLED)
				log(JUST_LOG_FILE, "vawer error: " + e.getMessage(), true);
			if(first && DEBUG_ENABLED)
				logError(ERROR_LOG_FILE, e, true);
			throw e;
		}
		try{
			BricksParticleSystem.begin();
			BricksParticleSystem.drawEffects(CameraHelper.getCameraEffects());
			BricksParticleSystem.end();
			modelBatch.render(BricksParticleSystem.particleSystem(), environment);
			BricksParticleSystem.freeEffects(CameraHelper.getCameraEffects());
		}catch(Exception e){
			if(first && DEBUG_ENABLED)
				log(JUST_LOG_FILE, "particles error: " + e.getMessage(), true);
			logError(ERROR_LOG_FILE, e, true);
			throw e;
		}

		if(first && DEBUG_ENABLED){
			log(JUST_LOG_FILE, "Particle render", true);
		}
		for(RenderableProvider entity : entitiesPool){
			try {
				modelBatch.render(entity, environment);
			}catch (Exception e){
				if(first && DEBUG_ENABLED)
					log(JUST_LOG_FILE, "entity render error: " + entity.getClass().getCanonicalName(), true);
				if(first && DEBUG_ENABLED)
					logError(ERROR_LOG_FILE, e, true);
				throw e;
			}
		}
		this.cameraSatellite.checkUpdate();
		if(SPACE_DEBUG_ENABLED){
			debug.drawSpaceShapes(modelBatch, entitiesPool);
		}
//		cameraSatellite.applyUpdates();
		modelBatch.end();
		if(first && DEBUG_ENABLED){
			log(JUST_LOG_FILE, "models are rendered", true);
		}
		if(DEBUG_ENABLED){
			debug.drawEntityShapes(entitiesPool, camera.combined);
			debug.drawSectors(engine, camera.combined, CameraHelper.getInCameraDistricts());
		}
		CameraHelper.end();
		interactiveController.render(Gdx.graphics.getDeltaTime());
		if(first && DEBUG_ENABLED){
			log(JUST_LOG_FILE, "Normal Render finished", true);
			log(JUST_LOG_FILE, "********************************************", true);
			//first = false;
		}
		if(first) {
			firstRenderer.dispose();
			first = false;
		}
//		Informator.render(camera);
	}
	
	@Override
	public void resize(int width, int height) {
		renderer.resize(width, height);
	}
	
	@Override
	public void dispose(){
		engine.stop();
		modelBatch.dispose();
		debug.dispose();
		ModelStorage.instance().dispose();
		Skinner.instance().dispose();
		interactiveController.dispose();
		Gdx.app.debug("OLEH-TEST", "Average frame time: " + (this.totalDeltaTime / this.totalFramesCount));
		Gdx.app.debug("OLEH-TEST", "Created " + Ammunition.counter.get() + " ammunition instances");
		Gdx.app.debug("OLEH-TEST", "Dust instances: " + Ammunition.dustInstances.get());
//		this.log(this.ERROR_LOG_FILE, "	end disposeing", true);
//		this.log(this.ERROR_LOG_FILE, System.currentTimeMillis() + "--disposeing ", true);
	}

	private void registerCachedClasses(){

		Cache.registerCache(Vector3.class, new Cache.DataProvider<Vector3>(){
			@Override
			public Vector3 provideNew() {
				return new Vector3();
			}
			
		});
		
	}
	private void tuneDirection(){
		tmpDirection.set(camera.direction.x, camera.direction.y, 0f);
		tmpDirection.nor();
		tmpDirection.z = dirLight.direction.z;
		tmpDirection.nor();
		dirLight.direction.set(tmpDirection);
	}

	private interface SeawarRenderer{
		public void render();
		public void resize(int width, int height);
		public void dispose();
	}
	
	private class NormalRenderer implements SeawarRenderer{

		@Override
		public void render() {
			renderNormal();
		}

		@Override
		public void resize(int width, int height) {
			camera.viewportWidth = width;
			camera.viewportHeight = height;
			camera.update();
			interactiveController.resizeViewport(width, height);
		}

		@Override
		public void dispose() {

		}

	}


	//DEBUGS:
	private void log(String filename, String text, boolean append){
		FileHandle fh = Gdx.files.absolute(filename);
		fh.writeString(text + "\n", append);
	}

	private void logError(String filename, Throwable e){
		logError(filename, e, false);
	}

	private void logError(String filename, Throwable e, boolean append){
		FileHandle fh = Gdx.files.absolute(filename);
		String text = e.getLocalizedMessage() + "\n";
		for(StackTraceElement stm : e.getStackTrace()){
			text += stm.getClassName() + "." + stm.getFileName() + ":" + stm.getLineNumber() + "\n";
		}
		fh.writeString(text, append);
	}
}
