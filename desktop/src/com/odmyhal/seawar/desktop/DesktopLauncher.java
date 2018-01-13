package com.odmyhal.seawar.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.odmyhal.seawar.WarGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Sea Fight";
		config.width = 420;
		config.height = 700;
		config.samples = 3;
		config.stencil = 8;
		new LwjglApplication(new WarGame(), config);
	}
}
