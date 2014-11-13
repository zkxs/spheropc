package s3;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;
import s3.controller.ControllerManager;
import se.nicklasgavelin.bluetooth.*;
import se.nicklasgavelin.bluetooth.Bluetooth.EVENT;
import se.nicklasgavelin.sphero.*;


public class SpheroApp implements BluetoothDiscoveryListener
{	
	public static void main(String[] args)
	{
		new SpheroApp();
		Scanner in = new Scanner(System.in);
		in.nextLine();
	}
	
//	Set<Robot> spheros;
//	Set<Controller> controllers;
	ControllerManager cManager;
	
	private SpheroApp()
	{
//		ControllerEnvironment defaultEnvoirnment = ControllerEnvironment.getDefaultEnvironment();
//		
//		Controller[] allControllers = defaultEnvoirnment.getControllers();
		
		
//		System.out.println("Game controllers:");
//		for (Controller ctrl : allControllers)
//		{
//			if (ctrl.getType() == Controller.Type.STICK || ctrl.getType() == Controller.Type.GAMEPAD)
//			{
//				System.out.printf("\t%-40s  %5b\n",
//						ctrl.getName(),
//						isValidController(ctrl));
//			}
//		}
		
		
//		Bluetooth bt = new Bluetooth(this, Bluetooth.SERIAL_COM);
//		bt.discover();
		
		
		cManager = ControllerManager.getControllerManager();
	}
	
	
	
	@Override
	public void deviceSearchCompleted(Collection<BluetoothDevice> devices)
	{
		for (BluetoothDevice d : devices)
		{
			System.out.printf("%s %5b \"%s\"\n", d.getAddress(), Robot.isValidDevice(d), d.getName());
		}
	}

	@Override
	public void deviceDiscovered(BluetoothDevice device)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deviceSearchFailed(EVENT error)
	{
		System.err.println(error.getErrorMessage());
	}

	@Override
	public void deviceSearchStarted()
	{
		// TODO Auto-generated method stub
		
	}
}
