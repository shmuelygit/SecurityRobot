package robot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;


public class Robot implements Runnable{

	private static Robot instance = null;
	private RobotActionInterface robotActionInterface; 
	private ConcurrentHashMap<RobotAction, Long> actionToUpTimeMap; //this map will hold for each action, the end time of the action 
	private long mainLoopSleepMillis;
	private long actionLenMillis;
	private long servo000Micro;
	private long servo180Micro;
	private final boolean isPiEnv = false;
	//for camera rotate
	private long cameraRotateHorTimestamp;
	private long cameraRotateVerTimestamp;
	private long cameraRotateDelay;
	private int camRotateStepDeg;
	private int curHorizontalDeg; //hold the current horizontal camera rotate degree
	private int curverticalDeg;  //hold the current vertical camera rotate degree

	//private constructor, so we would use only one instance
	private Robot(){
		//load config from file
		Properties prop = new Properties();
		InputStream input;
		try {
			//get properties file location
			URL location = Robot.class.getProtectionDomain().getCodeSource().getLocation();
					     System.out.println(location.getFile());
					     System.out.println("xxx");
						input = new FileInputStream(location.getFile()+"\\config");
//			input = new FileInputStream("/home/config");

			//load file into prop object
			prop.load(input);
		} catch (FileNotFoundException e) {
			setDefaultProperties(prop);
			e.printStackTrace();
		} catch (IOException e) {
			setDefaultProperties(prop);
			e.printStackTrace();
		}
		//read variable value from prop object
		mainLoopSleepMillis = Long.parseLong(prop.getProperty("mainLoopSleepMillis"));
		actionLenMillis = Long.parseLong(prop.getProperty("actionLenMillis"));
		cameraRotateDelay = Long.parseLong(prop.getProperty("cameraRotateDelay"));
		servo000Micro = Long.parseLong(prop.getProperty("servo000Micro"));
		servo180Micro = Long.parseLong(prop.getProperty("servo180Micro"));
		camRotateStepDeg = Integer.parseInt(prop.getProperty("camRotateStepDeg"));

		//init action end time map
		actionToUpTimeMap = new ConcurrentHashMap<RobotAction, Long>();

		//create the robot action interface, acording to real env or test env
		if (isPiEnv) {
			robotActionInterface = new RobotActionPi();
		}
		else{
			robotActionInterface = new RobotActionWindows();
		}

		//create new thread for this robot
		new Thread(this).start();
	}

	//get the single instance of Robot class
	public static Robot getInstance(){
		if (instance == null) {
			instance = new Robot();
		}
		return instance;
	}

	public Object handle(RobotAction robotAction, long timeInitiated){
		//TODO use timeInitiated
		//update up time
		Long actionUpTime = System.currentTimeMillis()+actionLenMillis;
		//insert it to map
		actionToUpTimeMap.put(robotAction, actionUpTime);
		return null;
	}

	//this is the entry point for the new thread for this robot
	//it will handle the user commands, and activate the robot
	public void run(){
		//run forever
		while(true){
			//get current time
			long curTime = System.currentTimeMillis();

			//create copy of the map of actions up time, so that changes to this map won't affect now
			Map<RobotAction, Long> actionToUpTimeMapCopy = new HashMap<>();
			for (RobotAction robotAction : RobotAction.values()) {
				Long t = actionToUpTimeMap.get(robotAction);
				if(t != null){
					actionToUpTimeMapCopy.put(robotAction, t);
				}
			}

			//for each robot action
			for (RobotAction robotAction : RobotAction.values()) {
				//get the up time value
				Long actionUpTime = actionToUpTimeMapCopy.get(robotAction);
				
				//in case null, ignore
				if(actionUpTime == null){
					continue;
				}
				
				//handle the action
				
				//in case this is a move action
				if(isMovementAction(robotAction)){
					//in case the action is time relevant, and action is disable in the interface
					if (actionUpTime > curTime && robotActionInterface.isLow(robotAction)) {
						System.out.println("currTime\t"+curTime+"\trobotAction\t"+robotAction+"\tendTime\t"+actionUpTime+"\tset ON"+"\tis low\t"+robotActionInterface.isLow(robotAction));
						//enable the action in the interface
						robotActionInterface.high(robotAction);
					}
					//in case the relevant time for the action has passed, and the action is still enabled in the interface
					else if(actionUpTime < curTime && robotActionInterface.isHigh(robotAction)){
						System.out.println("currTime\t"+curTime+"\trobotAction\t"+robotAction+"\tendTime\t"+actionUpTime+"\tset ON"+"\tis low\t"+robotActionInterface.isLow(robotAction));
						//disable the action in the interface
						robotActionInterface.low(robotAction);
					}
				}
				//in case this is a camera action
				else if(isCameraAction(robotAction)){
					//if the action time is still relevant
					if (actionUpTime > curTime) {
						long pulseFreq = 0;
						//check if passed enough time from last rotation - at least cameraRotateDelay millis
						if(robotAction == RobotAction.CamRotateLeft && curTime > cameraRotateHorTimestamp + cameraRotateDelay){
							//change rotate degree
							curHorizontalDeg -= camRotateStepDeg;
							curHorizontalDeg = Math.max(0, curHorizontalDeg);
							//calculate the pwm frequency for the servo motor
							pulseFreq = getPulseFreqByDeg(curHorizontalDeg);
							//perform the rotate action, and update last timestamp
							robotActionInterface.pulseMicro(RobotAction.CamRotateLeft, pulseFreq);
							cameraRotateHorTimestamp = System.currentTimeMillis();
						}
						//check if passed enough time from last rotation - at least cameraRotateDelay millis
						else if(robotAction == RobotAction.CamRotateRight && curTime > cameraRotateHorTimestamp + cameraRotateDelay){
							//change rotate degree
							curHorizontalDeg += camRotateStepDeg;
							curHorizontalDeg = Math.min(180, curHorizontalDeg);
							//calculate the pwm frequency for the servo motor
							pulseFreq = getPulseFreqByDeg(curHorizontalDeg);
							//perform the rotate action, and update last timestamp
							robotActionInterface.pulseMicro(RobotAction.CamRotateRight, pulseFreq);
							cameraRotateHorTimestamp = System.currentTimeMillis();
						}
						//check if passed enough time from last rotation - at least cameraRotateDelay millis
						else if(robotAction == RobotAction.CamRotateUp && curTime > cameraRotateVerTimestamp + cameraRotateDelay){
							//change rotate degree
							curverticalDeg -= camRotateStepDeg;
							curverticalDeg = Math.max(0, curverticalDeg);
							//calculate the pwm frequency for the servo motor
							pulseFreq = getPulseFreqByDeg(curverticalDeg);
							//perform the rotate action, and update last timestamp
							robotActionInterface.pulseMicro(RobotAction.CamRotateUp, pulseFreq);
							cameraRotateVerTimestamp = System.currentTimeMillis();
						}
						//check if passed enough time from last rotation - at least cameraRotateDelay millis
						else if(robotAction == RobotAction.CamRotateDown && curTime > cameraRotateVerTimestamp + cameraRotateDelay){
							//change rotate degree
							curverticalDeg += camRotateStepDeg;
							curverticalDeg = Math.min(180, curverticalDeg);
							//calculate the pwm frequency for the servo motor
							pulseFreq = getPulseFreqByDeg(curverticalDeg);
							//perform the rotate action, and update last timestamp
							robotActionInterface.pulseMicro(RobotAction.CamRotateDown, pulseFreq);
							cameraRotateVerTimestamp = System.currentTimeMillis();
						}
						else if(robotAction == RobotAction.CamRotateInit){
							//set degree to middle - 90 and perform the action 
							curHorizontalDeg = 90;
							pulseFreq = getPulseFreqByDeg(curHorizontalDeg);
							robotActionInterface.pulseMicro(RobotAction.CamRotateRight, pulseFreq);
							cameraRotateHorTimestamp = System.currentTimeMillis();
							//set degree to middle - 90 and perform the action
							curverticalDeg = 90;
							pulseFreq = getPulseFreqByDeg(curverticalDeg);
							robotActionInterface.pulseMicro(RobotAction.CamRotateDown, pulseFreq);
							cameraRotateVerTimestamp = System.currentTimeMillis();
						}
						System.out.println("currTime\t"+curTime+"\trobotAction\t"+robotAction+"\tendTime\t"+actionUpTime+"\tpulse freq\t"+pulseFreq+"\tcurHorizontalDeg\t"+curHorizontalDeg+"\tcurverticalDeg\t"+curverticalDeg);
					}
				}
			}
			try {
				//sleep
				Thread.sleep(mainLoopSleepMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isMovementAction(RobotAction robotAction){
		return (robotAction == RobotAction.MoveForward || robotAction == RobotAction.MoveBackward || robotAction == RobotAction.MoveLeft || robotAction == RobotAction.MoveRight);
	}

	private boolean isCameraAction(RobotAction robotAction){
		return (robotAction == RobotAction.CamRotateRight || robotAction == RobotAction.CamRotateLeft || robotAction == RobotAction.CamRotateUp || robotAction == RobotAction.CamRotateDown || robotAction == RobotAction.CamRotateInit);
	}

	//calc the pwm pulse frequency by the degree
	private long getPulseFreqByDeg(int deg){
		return (long) (servo000Micro+(servo180Micro-servo000Micro)*(deg/180d));
	}
	
	private void setDefaultProperties(Properties prop){
		//TODO
	}
}
