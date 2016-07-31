package robot;

import java.util.HashMap;
import java.util.Map;

public class RobotActionWindows implements RobotActionInterface {

	private Map<RobotAction,Long> actionHighTime;
	private final Long ON = (long) -1;
	private final Long OFF = (long) -2;
	
	RobotActionWindows(){
		actionHighTime = new HashMap<>();
	}
	
	public void low(RobotAction action) {
		actionHighTime.put(action, OFF);
	}

	public void high(RobotAction action) {
		actionHighTime.put(action, ON);

	}

	public void pulse(RobotAction action, long millis) {
		actionHighTime.put(action, System.currentTimeMillis()+millis);
	}

	public void pulseMicro(RobotAction action, long millis) {
		//TODO
	}
	
	public boolean isLow(RobotAction action) {
		Long t = actionHighTime.get(action);
		Long cur = System.currentTimeMillis();
		if(t == null)
			return true;
		if(t == ON)
			return false;
		if(t == OFF)
			return true;
		if(t > cur)
			return false;
		
		actionHighTime.put(action, OFF);
		return true;
	}

	public boolean isHigh(RobotAction action) {
		return !isLow(action);
	}

}
