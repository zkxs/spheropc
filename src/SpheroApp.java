import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import net.java.games.input.*;
import net.java.games.input.Component.Identifier;
import se.nicklasgavelin.bluetooth.*;
import se.nicklasgavelin.bluetooth.Bluetooth.EVENT;
import se.nicklasgavelin.sphero.*;


public class SpheroApp implements BluetoothDiscoveryListener
{	
	public static void main(String[] args)
	{
		new SpheroApp();
	}
	
	Set<Robot> spheros;
	Set<Controller> controllers;
	
	
	private SpheroApp()
	{
		ControllerEnvironment defaultEnvoirnment = ControllerEnvironment.getDefaultEnvironment();
		
		Controller[] allControllers = defaultEnvoirnment.getControllers();
		
		
		System.out.println("Game controllers:");
		for (Controller ctrl : allControllers)
		{
			if (ctrl.getType() == Controller.Type.STICK || ctrl.getType() == Controller.Type.GAMEPAD)
			{
				System.out.printf("\t%-40s  %5b\n",
						ctrl.getName(),
						isValidController(ctrl));
			}
		}
		
		
		Bluetooth bt = new Bluetooth(this, Bluetooth.SERIAL_COM);
		bt.discover();
	}
	
	
	final static Component.Identifier[] xboxComponents = {
		Identifier.Axis.Y,
		Identifier.Axis.X,
		Identifier.Axis.RY,
		Identifier.Axis.RX,
		Identifier.Axis.Z,
		Identifier.Button._0,
		Identifier.Button._1,
		Identifier.Button._2,
		Identifier.Button._3,
		Identifier.Button._4,
		Identifier.Button._5,
		Identifier.Button._6,
		Identifier.Button._7,
		Identifier.Button._8,
		Identifier.Button._9,
		Identifier.Axis.POV
	};
	public static boolean isValidController(Controller c)
	{
		// eliminate non-controller inputs
		Controller.Type t = c.getType();
		if ( !( t.equals(Controller.Type.STICK) || t.equals(Controller.Type.GAMEPAD) ) ) return false;
		
		// check if name is whitelisted
//		String name = c.getName().toLowerCase();
//		if (name.contains("xbox") || name.contains("gamepad f310")) return true;
		
		// finally, examine components to see if it "looks" like an XBOX controller
		Component[] comps = c.getComponents();
		if (xboxComponents.length != comps.length) return false;
		for (int i = 0; i < comps.length; i++)
		{
			Identifier id1 = xboxComponents[i];
			Identifier id2 = comps[i].getIdentifier();
			
			if (!id1.equals(id2)) return false;
		}
		return true;
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
