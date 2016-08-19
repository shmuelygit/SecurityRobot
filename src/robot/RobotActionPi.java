package robot;

import java.util.concurrent.ConcurrentHashMap;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import robot.RobotAction;

public class RobotActionPi implements RobotActionInterface{

	//map from robot action to gpio output
	private ConcurrentHashMap<RobotAction, GpioPinDigitalOutput> actionToGpioMap;
	// gpio controller
	private GpioController gpioController;
	
	RobotActionPi(){
		//create the map
		actionToGpioMap = new ConcurrentHashMap<RobotAction, GpioPinDigitalOutput>();
		try{
			//create the controller
			gpioController = GpioFactory.getInstance();
			//init map - for each movement robot action create a new gpio pin
			actionToGpioMap.put(RobotAction.MoveForward, gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_01,"My LED",PinState.LOW));
			actionToGpioMap.put(RobotAction.MoveBackward, gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_04,"My LED",PinState.LOW));
			actionToGpioMap.put(RobotAction.MoveRight, gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_05,"My LED",PinState.LOW));
			actionToGpioMap.put(RobotAction.MoveLeft, gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_06,"My LED",PinState.LOW));
			
			//init gpio for camera
			GpioPinDigitalOutput camHorizontal = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02,"My LED",PinState.LOW);
			GpioPinDigitalOutput camVertical = gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_03,"My LED",PinState.LOW);
			
			actionToGpioMap.put(RobotAction.CamRotateLeft, camHorizontal);
			actionToGpioMap.put(RobotAction.CamRotateRight, camHorizontal);
			actionToGpioMap.put(RobotAction.CamRotateUp, camVertical);
			actionToGpioMap.put(RobotAction.CamRotateDown, camVertical);
			
		}catch(Exception e){
			e.printStackTrace();
		}catch(Error e){
			e.printStackTrace();
		}
	}
	
	public void low(RobotAction action) {
		GpioPinDigitalOutput gpioOutput = actionToGpioMap.get(action);
		if(gpioOutput!=null){
			gpioOutput.low();
		}
	}

	public void high(RobotAction action) {
		GpioPinDigitalOutput gpioOutput = actionToGpioMap.get(action);
		if(gpioOutput!=null){
			gpioOutput.high();
		}
	}

	public void pulse(RobotAction action, long millis) {
		GpioPinDigitalOutput gpioOutput = actionToGpioMap.get(action);
		if(gpioOutput!=null){
			gpioOutput.pulse(millis);
		}		
	}

	public void pulseMicro(RobotAction action, long micro) {
		//for long pulse use regular pulse, without busy waiting
		if(micro > 10000){
			pulse(action, micro/1000);
			return;
		}
		
		GpioPinDigitalOutput gpioOutput = actionToGpioMap.get(action);
		if(gpioOutput!=null){
			long nanos = micro*1000;
			gpioOutput.high();
		    long start = System.nanoTime();
		    long end=0;
		    do{
		        end = System.nanoTime();
		    }while(start + nanos >= end);
			gpioOutput.low();
		}		
	}
	
	public boolean isLow(RobotAction action) {
		GpioPinDigitalOutput gpioOutput = actionToGpioMap.get(action);
		if(gpioOutput!=null){
			return gpioOutput.isLow();
		}
		return false;
	}

	public boolean isHigh(RobotAction action) {
		GpioPinDigitalOutput gpioOutput = actionToGpioMap.get(action);
		if(gpioOutput!=null){
			return gpioOutput.isHigh();
		}
		return false;
	}
	
}
