package robot;

public interface RobotActionInterface {
	void low(RobotAction action);
	void high(RobotAction action);
	void pulse(RobotAction action, long millis);
	void pulseMicro(RobotAction action, long millis);
	boolean isLow(RobotAction action);
	boolean isHigh(RobotAction action);
}
