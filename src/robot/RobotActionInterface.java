package robot;

//the interface for the robot action - for actual performance of actions
public interface RobotActionInterface {
	void low(RobotAction action); //set action to low
	void high(RobotAction action); // set action to high
	void pulse(RobotAction action, long millis); // pulse
	void pulseMicro(RobotAction action, long millis);
	boolean isLow(RobotAction action); //is low
	boolean isHigh(RobotAction action); // is high
}
