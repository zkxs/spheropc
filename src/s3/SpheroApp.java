package s3;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.java.games.input.Controller;
import s3.controller.ControllerManager;
import s3.sphero.Sphero;
import s3.sphero.SpheroManager;
import s3.util.Option;


public class SpheroApp
{	
	public final static long UPDATE_PERIOD = 75;
	
	
	public static void main(String[] args)
	{
		System.out.println("Program Started.");
		System.out.println("Press enter to stop. Stopping may take a few seconds.");
		System.out.println();
		SpheroApp app = new SpheroApp();
		app.init();
	}
	
	private ControllerManager cManager;
	private SpheroManager sManager;
	private Timer updateTimer;
	
	/** This will be replaced with a gui later */
	Timer temporaryConnectorTimer;
	
	private SpheroApp()
	{
		setLogHandlerLevel(Level.ALL);
		
		// I don't know why I can't set the log level like this
//		Logger.getLogger("net.java.games.input.ControllerEnvironment").setLevel(Level.WARNING);
//		Logger.getLogger("s3.sphero.SpheroManager").setLevel(Level.WARNING);
		
		cManager = ControllerManager.getControllerManager(this);
		sManager = SpheroManager.getSpheroManager(this);
		
		updateTimer = new Timer(true);
		temporaryConnectorTimer = new Timer(true);
	}
	
	private void init()
	{
		temporaryConnectorTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run()
			{
				final TreeMap<Controller, Option<Sphero>> cMap = cManager.getControllerMap();
				final Set<Controller> controllers = cMap.keySet();
				final ArrayList<Sphero> spheros = sManager.getSpheros();
				
				for (Controller c : controllers)
				{
					Option<Sphero> option = cMap.get(c);
					if (option.isNull())
					{
						// we should assign a controller
						for (Sphero s : spheros)
						{
							if (!s.hasController() && !s.getRobot().isConnected())
							{
								s.setController(c);
								if (s.startUpdating(updateTimer))
								{
									assert c != null;
									cMap.put(c, new Option<Sphero>(s));
									break;
								}
							}
						}
					}
				}
			}
			
		}, 0, 100);
		
		
		
		
		Scanner console = new Scanner(System.in);
		console.nextLine();
		console.close();
		
		updateTimer.cancel();
		temporaryConnectorTimer.cancel();
		
		
		//TODO: make a thread to enforce shutdown after a timeout
		
		System.out.println("Closing sphero manager");
		sManager.close();
		cManager.close();
		
		System.out.println("Ending main method");
		System.out.println();
		System.out.println("Active threads: ");
		Thread killer1 = new Thread(killerRunnable1, "killer1");
		killer1.setDaemon(true);
		killer1.start();
		Thread killer2 = new Thread(killerRunnable2, "killer2");
		killer2.setDaemon(true);
		killer2.start();
		Thread[] activeThreads = new Thread[Thread.activeCount()];
		Thread.enumerate(activeThreads);
		for (Thread thread : activeThreads)
		{
			System.out.printf("\t%-40s Daemon: %b\n", thread.getName(), thread.isDaemon());
		}
		System.out.println();
	}
	
	public ControllerManager getControllerManager()
	{
		return cManager;
	}
	
	public SpheroManager getSpheroManager()
	{
		return sManager;
	}
	
	private void setLogHandlerLevel(Level level)
	{
		//get the top Logger:
	    Logger topLogger = Logger.getLogger("");

	    // Handler for console (reuse it if it already exists)
	    Handler consoleHandler = null;
	    //see if there is already a console handler
	    for (Handler handler : topLogger.getHandlers()) {
	        if (handler instanceof ConsoleHandler) {
	            //found the console handler
	            consoleHandler = handler;
	            break;
	        }
	    }


	    if (consoleHandler == null) {
	        //there was no console handler found, create a new one
	        consoleHandler = new ConsoleHandler();
	        topLogger.addHandler(consoleHandler);
	    }
	    //set the console handler to fine:
	    consoleHandler.setLevel(level);
	}
	
	private Runnable killerRunnable1 = new Runnable(){

		@Override
		public void run()
		{
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			
			System.err.print("Killed");
			System.exit(1);
		}
		
	};
	
	private Runnable killerRunnable2 = new Runnable(){

		@Override
		public void run()
		{
			try
			{
				Thread.sleep(6000);
				System.err.print(".");
				Thread.sleep(1000);
				System.err.print(".");
				Thread.sleep(1000);
				System.err.print(".");
				Thread.sleep(2000);
				System.err.print(" Apparently not.");
				
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			
			System.err.println("\nLets try again, but a little harder this time.");
			Runtime.getRuntime().halt(2);
		}
		
	};
}
