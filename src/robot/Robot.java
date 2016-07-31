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
	private long servo000Nano;
	private long servo180Nano;
	private final boolean isPiEnv = false;
	//for camera rotate
	private long cameraRotateDelay = 50;
	
	private Robot(){
		//load config
		Properties prop = new Properties();
		InputStream input;
		try {
			 URL location = Robot.class.getProtectionDomain().getCodeSource().getLocation();
		     System.out.println(location.getFile());
			input = new FileInputStream("config");
			prop.load(input);
		} catch (FileNotFoundException e) {
			setDefaultProperties(prop);
			e.printStackTrace();
		} catch (IOException e) {
			setDefaultProperties(prop);
			e.printStackTrace();
		}
		mainLoopSleepMillis = Long.parseLong(prop.getProperty("mainLoopSleepMillis"));
		actionLenMillis = Long.parseLong(prop.getProperty("actionMinWaitTimeMillis"));
		cameraRotateDelay = Long.parseLong(prop.getProperty("cameraRotateDelay"));
		servo000Nano = Long.parseLong(prop.getProperty("servo000Nano"));
		servo180Nano = Long.parseLong(prop.getProperty("servo180Nano"));
		
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
						System.out.println("currTime\t"+curTime+"\trobotAction\t"+robotAction+"\tendTime\t"+t+"\tset ON"+"\tis low\t"+robotActionInterface.isLow(robotAction));
						if(robotAction == RobotAction.CamRotateLeft){
							robotActionInterface.pulseMicro(RobotAction.CamRotateLeft, servo000Nano);
						}
						else if(robotAction == RobotAction.CamRotateRight){
							robotActionInterface.pulseMicro(RobotAction.CamRotateRight, servo180Nano);
						}
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
		return (robotAction == RobotAction.CamRotateRight || robotAction == RobotAction.CamRotateLeft);
	}
	
	private void setDefaultProperties(Properties prop){
		//TODO
	}
}
