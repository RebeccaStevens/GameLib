package gamelib.game;

import gamelib.GameManager;


public abstract class Level3D extends Level {

	public Level3D(){
		if(!GameManager.getMe().getGraphics().is3D()) throw new RuntimeException("Cannot make a 3D level using 2D graphics.");
	}

	@Override
	public boolean is3D() {
		return true;
	}
}
