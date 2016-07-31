package robot;

import java.util.concurrent.ConcurrentHashMap;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import robot.RobotAction;

public class RobotActionPi implements RobotActionInterface{

	
	private ConcurrentHashMap<RobotAction, GpioPinDigitalOutput> actionToGpioMap;
	private GpioController gpioController;
	
	RobotActionPi(){
		actionToGpioMap = new ConcurrentHashMap<RobotAction, GpioPinDigitalOutput>();
		try{
			gpioController = GpioFactory.getInstance();
			actionToGpioMap.put(RobotAction.MoveForward, gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_02,"My LED",PinState.LOW));
			actionToGpioMap.put(RobotAction.MoveBackward, gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_03,"My LED",PinState.LOW));
			actionToGpioMap.put(RobotAction.MoveRight, gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_04,"My LED",PinState.LOW));
			actionToGpioMap.put(RobotAction.MoveLeft, gpioController.provisionDigitalOutputPin(RaspiPin.GPIO_17,"My LED",PinState.LOW));
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
		GpioPinDigitalOutput gpioOutput = actionToGpioMap.get(action);
		if(gpioOutput!=null){
			gpioOutput.high();
		    long start = System.nanoTime();
		    long end=0;
		    do{
		        end = System.nanoTime();
		    }while(start + micro >= end);
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
