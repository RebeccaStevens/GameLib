package gamelib;

import java.util.ArrayList;
import java.util.List;

public class Key {

	static final List<Key> keys = new ArrayList<Key>();
	
	private boolean isPressed;
	private int keyCode;
	private Runnable pressedEvent;
	private Runnable releasedEvent;

	public Key(int keyCode){
		this.isPressed = false;
		this.keyCode = keyCode;
		this.pressedEvent = null;
		this.releasedEvent = null;
		keys.add(this);
	}

	public Key(int keyCode, Runnable pressedEvent){
		this.isPressed = false;
		this.keyCode = keyCode;
		this.pressedEvent = pressedEvent;
		this.releasedEvent = null;
		keys.add(this);
	}

	public Key(int keyCode, Runnable pressedEvent, Runnable releasedEvent){
		this.isPressed = false;
		this.keyCode = keyCode;
		this.pressedEvent = pressedEvent;
		this.releasedEvent = releasedEvent;
		keys.add(this);
	}

	public boolean isPressed(){
		return this.isPressed;
	}

	public void setPressedEvent(Runnable event){
		this.pressedEvent = event;
	}

	public void setReleasedEvent(Runnable event){
		this.releasedEvent = event;
	}

	public void removePressedEvent(){
		this.pressedEvent = null;
	}

	public void removeReleasedEvent(){
		this.releasedEvent = null;
	}

	void update(int keyCode, boolean pressed){
		if(this.keyCode == keyCode){
			if(pressedEvent != null && pressed && !this.isPressed){
				pressedEvent.run();
			}
			if(releasedEvent != null && !pressed && this.isPressed){
				releasedEvent.run();
			}

			this.isPressed = pressed;
		}
	}
}
