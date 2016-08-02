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
	private ConcurrentHashMap<RobotAction, Long> actionToUpTimeMap;
	private long mainLoopSleepMillis;
	private long actionLenMillis;
	private long servo000Micro;
	private long servo180Micro;
	private final boolean isPiEnv = true;
	//for camera rotate
	private long cameraRotateHorTimestamp;
	private long cameraRotateVerTimestamp;
	private long cameraRotateDelay;
	private int camRotateStepDeg;
	private int curHorizontalDeg;
	private int curverticalDeg;

	private Robot(){
		//load config
		Properties prop = new Properties();
		InputStream input;
		try {
			URL location = Robot.class.getProtectionDomain().getCodeSource().getLocation();
			//		     System.out.println(location.getFile());
			//		     System.out.println("xxx");
			//			input = new FileInputStream(location.getFile()+"\\config");
			input = new FileInputStream("/home/config");

			prop.load(input);
		} catch (FileNotFoundException e) {
			setDefaultProperties(prop);
			e.printStackTrace();
		} catch (IOException e) {
			setDefaultProperties(prop);
			e.printStackTrace();
		}
		mainLoopSleepMillis = Long.parseLong(prop.getProperty("mainLoopSleepMillis"));
		actionLenMillis = Long.parseLong(prop.getProperty("actionLenMillis"));
		cameraRotateDelay = Long.parseLong(prop.getProperty("cameraRotateDelay"));
		servo000Micro = Long.parseLong(prop.getProperty("servo000Micro"));
		servo180Micro = Long.parseLong(prop.getProperty("servo180Micro"));
		camRotateStepDeg = Integer.parseInt(prop.getProperty("camRotateStepDeg"));

		actionToUpTimeMap = new ConcurrentHashMap<RobotAction, Long>();

		if (isPiEnv) {
			robotActionInterface = new RobotActionPi();
		}
		else{
			robotActionInterface = new RobotActionWindows();
		}

		new Thread(this).start();
	}

	public static Robot getInstance(){
		if (instance == null) {
			instance = new Robot();
		}
		return instance;
	}

	public void handle(RobotAction robotAction, long timeInitiated){
		//TODO use timeInitiated
		Long t = actionToUpTimeMap.get(robotAction);
		Long nt = System.currentTimeMillis()+actionLenMillis;
		actionToUpTimeMap.put(robotAction, nt);
	}

	/*
	 * GPIO loop
	 */
	public void run(){
		System.out.println("run");
		while(true){
			long curTime = System.currentTimeMillis();

			Map<RobotAction, Long> actionToUpTimeMapCopy = new HashMap<>();
			for (RobotAction robotAction : RobotAction.values()) {
				Long t = actionToUpTimeMap.get(robotAction);
				if(t != null){
					actionToUpTimeMapCopy.put(robotAction, t);
				}
			}

			for (RobotAction robotAction : RobotAction.values()) {
				Long t = actionToUpTimeMapCopy.get(robotAction);
				if(t == null){
					continue;
				}
				if(isMovementAction(robotAction)){
					if (t > curTime && robotActionInterface.isLow(robotAction)) {
						System.out.println("currTime\t"+curTime+"\trobotAction\t"+robotAction+"\tendTime\t"+t+"\tset ON"+"\tis low\t"+robotActionInterface.isLow(robotAction));
						robotActionInterface.high(robotAction);
					}
					else if(t < curTime && robotActionInterface.isHigh(robotAction)){
						System.out.println("currTime\t"+curTime+"\trobotAction\t"+robotAction+"\tendTime\t"+t+"\tset ON"+"\tis low\t"+robotActionInterface.isLow(robotAction));
						robotActionInterface.low(robotAction);
					}
				}
				else if(isCameraAction(robotAction)){

					if (t > curTime) {
						long pulseFreq = 0;
						if(robotAction == RobotAction.CamRotateLeft && curTime > cameraRotateHorTimestamp + cameraRotateDelay){
							curHorizontalDeg -= camRotateStepDeg;
							curHorizontalDeg = Math.max(0, curHorizontalDeg);
							pulseFreq = getPulseFreqByDeg(curHorizontalDeg);
							robotActionInterface.pulseMicro(RobotAction.CamRotateLeft, pulseFreq);
							cameraRotateHorTimestamp = System.currentTimeMillis();
						}
						else if(robotAction == RobotAction.CamRotateRight && curTime > cameraRotateHorTimestamp + cameraRotateDelay){
							curHorizontalDeg += camRotateStepDeg;
							curHorizontalDeg = Math.min(180, curHorizontalDeg);
							pulseFreq = getPulseFreqByDeg(curHorizontalDeg);
							robotActionInterface.pulseMicro(RobotAction.CamRotateRight, pulseFreq);
							cameraRotateHorTimestamp = System.currentTimeMillis();
						}
						else if(robotAction == RobotAction.CamRotateUp && curTime > cameraRotateVerTimestamp + cameraRotateDelay){
							curverticalDeg -= camRotateStepDeg;
							curverticalDeg = Math.max(0, curverticalDeg);
							pulseFreq = getPulseFreqByDeg(curverticalDeg);
							robotActionInterface.pulseMicro(RobotAction.CamRotateUp, pulseFreq);
							cameraRotateVerTimestamp = System.currentTimeMillis();
						}
						else if(robotAction == RobotAction.CamRotateDown && curTime > cameraRotateVerTimestamp + cameraRotateDelay){
							curverticalDeg += camRotateStepDeg;
							curverticalDeg = Math.min(180, curverticalDeg);
							pulseFreq = getPulseFreqByDeg(curverticalDeg);
							robotActionInterface.pulseMicro(RobotAction.CamRotateDown, pulseFreq);
							cameraRotateVerTimestamp = System.currentTimeMillis();
						}
						else if(robotAction == RobotAction.CamRotateInit){
							curHorizontalDeg = 90;
							pulseFreq = getPulseFreqByDeg(curHorizontalDeg);
							robotActionInterface.pulseMicro(RobotAction.CamRotateRight, pulseFreq);
							cameraRotateHorTimestamp = System.currentTimeMillis();
							curverticalDeg = 90;
							pulseFreq = getPulseFreqByDeg(curverticalDeg);
							robotActionInterface.pulseMicro(RobotAction.CamRotateDown, pulseFreq);
							cameraRotateVerTimestamp = System.currentTimeMillis();
						}
						System.out.println("currTime\t"+curTime+"\trobotAction\t"+robotAction+"\tendTime\t"+t+"\tpulse freq\t"+pulseFreq+"\tcurHorizontalDeg\t"+curHorizontalDeg+"\tcurverticalDeg\t"+curverticalDeg);
					}
				}
			}
			try {
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

	private long getPulseFreqByDeg(int deg){
		return (long) (servo000Micro+(servo180Micro-servo000Micro)*(deg/180d));
	}
	
	private void setDefaultProperties(Properties prop){
		//TODO
	}
}
