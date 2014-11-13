package s3.controller;

/* TODO: 
 * When a controller is unplugged:
 *     remove it from the map
 *     disconnect the sphero
 */


import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerEvent;
import net.java.games.input.ControllerListener;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import s3.sphero.Sphero;
import s3.util.Option;

public class ControllerManager implements ControllerListener
{	
	private static Timer timer = new Timer(true);
	private static ControllerManager instance = null;
	
	/**
	 * These sets are modified in a timer thread, so don't ever iterate over them elsewhere
	 * without synchronization
	 */
	private SortedSet<Controller> oldControllers, newControllers;
	
	private SortedSet<Controller> currentControllers;
	
	private LinkedList<ControllerListener> listeners;
	
	/** Putting nulls into this would be confusing, so I used Option, similar to how Scala does it */
	private TreeMap<Controller, Option<Sphero>> controllerMap;
	
	/** Temporary event object to reuse in EventeQueues */
	private Event event;
	
	private Logger logger;
	
	/**
	 * Get an instance of ControllerManager
	 * @return an instance of ControllerManager
	 */
	public static synchronized ControllerManager getControllerManager()
	{
		if (instance == null)
		{
			instance = new ControllerManager();
			instance.init();
			
			//environment.addControllerListener(instance); // doesn't even work
		}
		
		return instance;
	}
	
	private ControllerManager()
	{
		oldControllers = new TreeSet<Controller>(ctrlComparator);
		newControllers = new TreeSet<Controller>(ctrlComparator);
		currentControllers = new ConcurrentSkipListSet<Controller>(ctrlComparator);
		listeners = new LinkedList<ControllerListener>();
		controllerMap = new TreeMap<Controller, Option<Sphero>>(ctrlComparator);
		event = new Event();
		logger = Logger.getLogger(this.getClass().getName());
	}
	
	private void init()
	{
		// see if unbound controllers have pressed start
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run()
			{
				for (Controller c : currentControllers)
				{
					if (!controllerMap.containsKey(c))
					{	// if unbound controller
						// see if start has been pressed
						if (c.poll())
						{
							EventQueue queue = c.getEventQueue();
							while (queue.getNextEvent(event))
							{
								if (event.getComponent().getIdentifier().equals(Identifier.Button._7)
										&& event.getValue() == 1.0f)
								{
									// then unbound controller c has pressed start
									controllerMap.put(c, (Option<Sphero>) Option.EMPTY);
									logger.info(c.getName() + " @ " + c.getPortNumber() + " is now bound");
								}
							}
						}
						else
						{
							// something is very wrong
							logger.log(Level.WARNING, "existing controller failed to poll");
						}
					}
				}
			}
		}, 0, 100); // 100ms delay
		
		// keep track of added and removed controllers
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run()
			{
				final Controller[] poll = getEnvironment().getControllers();
				newControllers.clear();
				
				for (Controller c : poll)
				{
					if (isValidController(c))
					{
						newControllers.add(c);
					}
				}
				
				// this block would need to be synchronized if oldControllers were accessed elsewhere
				{
					// check if controllers have gone missing
					Iterator<Controller> iter = oldControllers.iterator();
					while (iter.hasNext())
					{
						final Controller c = iter.next();
						if (!newControllers.contains(c))
						{
							controllerRemoved(new ControllerEvent(c));
							iter.remove();
						}
					}
					
					// check if controllers have been added
					for (Controller c : newControllers)
					{
						if (oldControllers.add(c))
						{
							controllerAdded(new ControllerEvent(c));
						}
					}
				}
			}
			
		}, 0, 100); // 100ms rescan delay
	}
	
	public void close()
	{
		timer.cancel();
	}
	
	public TreeMap<Controller, Option<Sphero>> getControllerMap()
	{
		return controllerMap;
	}
	
	public SortedSet<Controller> getConnectedControllers()
	{
		return currentControllers;
	}
	
	public boolean addListener(ControllerListener cl)
	{
		return listeners.add(cl);
	}
	
	public boolean removeListener(ControllerListener cl)
	{
		return listeners.remove(cl);
	}
	
	final private static Component.Identifier[] xboxComponents = {
		Identifier.Axis.Y, // Left Y
		Identifier.Axis.X, // Left X
		Identifier.Axis.RY, // Right Y
		Identifier.Axis.RX, // Right X
		Identifier.Axis.Z, // analog triggers
		Identifier.Button._0, // A
		Identifier.Button._1, // B
		Identifier.Button._2, // X
		Identifier.Button._3, // Y
		Identifier.Button._4, // L1 (bumper)
		Identifier.Button._5, // R1 (bumper)
		Identifier.Button._6, // back
		Identifier.Button._7, // start
		Identifier.Button._8, // L3 (stick push)
		Identifier.Button._9, // R3 (stick push)
		Identifier.Axis.POV // D-Pad
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
		return ControllerEnvironment.getDefaultEnvironment();
//		try
//		{
//			@SuppressWarnings("unchecked")
//			Class<ControllerEnvironment> clazz = (Class<ControllerEnvironment>) Class.forName("net.java.games.input.DefaultControllerEnvironment");
//			Constructor<ControllerEnvironment> con = clazz.getDeclaredConstructor();
//			con.setAccessible(true);
//			return con.newInstance();
//		}
//		catch (Exception e) // Reflection has way too many things that can go wrong
//		{
//			// Checked exceptions are a bad idea anyways
//			throw new RuntimeException(e);
//		}		
	}

	@Override
	public void controllerAdded(ControllerEvent ev)
	{
		logger.info("Controller Added: " + ev.getController().getName() + " @ " + ev.getController().getPortNumber());
		
		currentControllers.add(ev.getController());
		
		for (ControllerListener cl : listeners)
		{
			cl.controllerAdded(ev);
		}
	}

	@Override
	public void controllerRemoved(ControllerEvent ev)
	{
		logger.info("Controller Removed: " + ev.getController().getName() + " @ " + ev.getController().getPortNumber());
		
		currentControllers.remove(ev.getController());
		
		for (ControllerListener cl : listeners)
		{
			cl.controllerRemoved(ev);
		}
	}
	
	final static private Comparator<Controller> ctrlComparator = new Comparator<Controller>() {
		@Override
		public int compare(Controller o1, Controller o2)
		{
			// apparently the controllers have NOTHING unique about them
			return o1.hashCode() - o2.hashCode();
		}
	};
}
