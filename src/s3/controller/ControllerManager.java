package s3.controller;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Component.Identifier;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;

public class ControllerManager implements ControllerListener
{	
	private static Timer timer = new Timer();
	private static ControllerManager instance = null;
	
	private Set<Controller> controllers;
	
	/**
	 * Get an instance of ControllerManager
	 * @return an instance of ControllerManager
	 */
	public static synchronized ControllerManager getControllerManager()
	{
		if (instance == null)
		{
			instance = new ControllerManager();
			
			//environment.addControllerListener(instance); // doesn't even work
		}
		
		return instance;
	}
	
	private ControllerManager()
	{
		controllers = new TreeSet<Controller>(ctrlComparator);
		
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run()
			{
				final Controller[] poll = getEnvironment().getControllers();
				final Set<Controller> current = new TreeSet<Controller>(ctrlComparator);
				
				for (Controller c : poll)
				{
					if (isValidController(c))
					{
						current.add(c);
					}
				}
				
				// check if controllers have gone missing
				Iterator<Controller> iter = controllers.iterator();
				while (iter.hasNext())
				{
					final Controller c = iter.next();
					if (!current.contains(c))
					{
						controllerRemoved(new ControllerEvent(c));
						iter.remove();
					}
				}
				
				// check if controllers have been added
				for (Controller c : current)
				{
					if (controllers.add(c))
					{
						controllerAdded(new ControllerEvent(c));
					}
				}
			}
			
		}, 0, 100);
	}
	
	final private static Component.Identifier[] xboxComponents = {
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
	private static boolean isValidController(Controller c)
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
	
	private ControllerEnvironment getEnvironment()
	{
		try
		{
			@SuppressWarnings("unchecked")
			Class<ControllerEnvironment> clazz = (Class<ControllerEnvironment>) Class.forName("net.java.games.input.DefaultControllerEnvironment");
			Constructor<ControllerEnvironment> con = clazz.getDeclaredConstructor();
			con.setAccessible(true);
			return con.newInstance();
		}
		catch (Exception e) // Reflection has way too many things that can go wrong
		{
			// Checked exceptions are a bad idea anyways
			throw new RuntimeException(e);
		}		
	}

	@Override
	public void controllerAdded(ControllerEvent ev)
	{
		// TODO Auto-generated method stub
		System.out.printf("Added: %s\n", ev.getController().getName());
	}

	@Override
	public void controllerRemoved(ControllerEvent ev)
	{
		// TODO Auto-generated method stub
		System.out.printf("Removed: %s\n", ev.getController().getName());
	}
	
	final static private Comparator<Controller> ctrlComparator = new Comparator<Controller>() {
		@Override
		public int compare(Controller o1, Controller o2)
		{
			return o1.getPortNumber() - o2.getPortNumber();
		}
	};
}
