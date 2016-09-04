package gamelib;

import processing.core.PApplet;

public class Time {
	
	private static Time me;
	
	private float timeStep, timeFrame, timeFactor;
	private float timeStamp;

	Time(PApplet papplet){
		me = this;
		timeStamp = papplet.millis();
		timeStep = 0;
		timeFrame = 0;
		timeFactor = 1;
	}
	
	static void update(PApplet papplet){
		float newTimeStamp = papplet.millis();
		me.timeFrame = newTimeStamp - me.timeStamp;
		me.timeStep = me.timeFrame * me.timeFactor;
		me.timeStamp = newTimeStamp;
	}
	
	public static float getTimeStep(){
		return me.timeStep / 1000;
	}
	
	public static float getTimeFrame(){
		return me.timeFrame / 1000;
	}
	
	public static float getTimeFactor(){
		return me.timeFactor;
	}
	
	public static float getTimeStamp(){
		return me.timeStamp / 1000;
	}
	
	public static void setTimeFactor(float timeFactor){
		me.timeFactor = timeFactor;
	}
}
