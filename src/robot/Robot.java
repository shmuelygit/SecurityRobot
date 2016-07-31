package robot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
	private long cameraRotateTime;
	private long mainLoopSleepMillis;
	private long actionLenMillis;
	private long servo000Micro;
	private long servo180Micro;
	private final boolean isPiEnv = true;
	//for camera rotate
	private long cameraRotateDelay = 50;
	private int camRotateStepDeg;
	private int curHorizontalDeg = 90;
	private int curverticalDeg = 90;
	
	private Robot(){
		//load config
		Properties prop = new Properties();
		InputStream input;
		try {
			 URL location = Robot.class.getProtectionDomain().getCodeSource().getLocation();
		     System.out.println(location.getFile());
		     System.out.println("xxx");
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
			for (RobotAction robotAction : RobotAction.values()) {
				Long t = actionToUpTimeMap.get(robotAction);
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
					
					if (t > curTime && curTime > cameraRotateTime + cameraRotateDelay) {
						long pulseFreq = 0;
						if(robotAction == RobotAction.CamRotateLeft){
							curHorizontalDeg -= camRotateStepDeg;
							curHorizontalDeg = Math.max(0, curHorizontalDeg);
							pulseFreq = (long) (servo000Micro+(servo180Micro-servo000Micro)*(curHorizontalDeg/180d));
//							pulseFreq = servo000Nano;
							robotActionInterface.pulseMicro(RobotAction.CamRotateLeft, pulseFreq);
						}
						else if(robotAction == RobotAction.CamRotateRight){
							curHorizontalDeg += camRotateStepDeg;
							curHorizontalDeg = Math.min(180, curHorizontalDeg);
							pulseFreq = (long) (servo000Micro+(servo180Micro-servo000Micro)*(curHorizontalDeg/180d));
//							pulseFreq = servo180Nano;
							robotActionInterface.pulseMicro(RobotAction.CamRotateRight, pulseFreq);
						}
						else if(robotAction == RobotAction.CamRotateUp){
							curverticalDeg -= camRotateStepDeg;
							curverticalDeg = Math.max(0, curverticalDeg);
							pulseFreq = (long) (servo000Micro+(servo180Micro-servo000Micro)*(curverticalDeg/180d));
//							pulseFreq = servo000Nano;
							robotActionInterface.pulseMicro(RobotAction.CamRotateUp, pulseFreq);
						}
						else if(robotAction == RobotAction.CamRotateDown){
							curverticalDeg += camRotateStepDeg;
							curverticalDeg = Math.min(180, curverticalDeg);
							pulseFreq = (long) (servo000Micro+(servo180Micro-servo000Micro)*(curverticalDeg/180d));
//							pulseFreq = servo180Nano;
							robotActionInterface.pulseMicro(RobotAction.CamRotateDown, pulseFreq);
						}
						System.out.println("currTime\t"+curTime+"\trobotAction\t"+robotAction+"\tendTime\t"+t+"\tpulse freq\t"+pulseFreq+"\tcurHorizontalDeg\t"+curHorizontalDeg+"\tcurverticalDeg\t"+curverticalDeg);
						cameraRotateTime = System.currentTimeMillis();
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
		return (robotAction == RobotAction.CamRotateRight || robotAction == RobotAction.CamRotateLeft||robotAction == RobotAction.CamRotateUp || robotAction == RobotAction.CamRotateDown);
	}
	
	private void setDefaultProperties(Properties prop){
		//TODO
	}
}
