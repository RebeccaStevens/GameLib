package gamelib;

/**
 * Manages the game's in-game time.
 *
 * @author Rebecca Stevens
 */
public final class Time {
	
	/**
	 * The timeFactor is the relation between the timeFrame and the timeStep.
	 * timeFrame * timeFactor = timeStep
	 */
	private float timeFactor = 1F;
	
	/**
	 * The timeFrame is the amount of actual time that has passed since the last frame.
	 */
	private float timeFrame = 0F;
	
	/**
	 * The timeStep is the amount of in game time that has passed since the last frame.
	 */
	private float timeStep = 0F;
	
	/**
	 * The time in milliseconds. Used to calculate timeFrame and timeStep
	 */
	private long timeStamp = 0L;
	
	/**
	 * The amount of in-game time that has passed since the last frame.
	 * 
	 * @return The timeStep 
	 */
	public float getTimeStep() {
		return timeStep;
	}

	/**
	 * The amount of actual time that has passed since the last frame.
	 * 
	 * @return The timeFrame 
	 */
	public float getTimeFrame() {
		return timeFrame;
	}

	/**
	 * The rate at which in-game time passes relative to actual time.
	 * 
	 * @return the timeFactor
	 */
	public float getTimeFactor() {
		return timeFactor;
	}

	/**
	 * Set the rate at which in-game time passes relative to actual time.
	 * 
	 * @param timeFactor
	 */
	public void setTimeFactor(float timeFactor) {
		this.timeFactor = timeFactor;
	}
	
	/**
	 * Update the values of timeFrame and timeStep.
	 * This method should be called once every frame.
	 */
	void update() {
		long newTimeStamp = System.currentTimeMillis();
		timeFrame = (newTimeStamp - timeStamp) / 1000F;
		timeStep = timeFactor * timeFrame;
		timeStamp = newTimeStamp;
	}
}
