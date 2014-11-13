package s3;

import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import s3.controller.ControllerManager;
import s3.sphero.SpheroManager;


public class SpheroApp
{	
	public static void main(String[] args)
	{
		SpheroApp app = new SpheroApp();
		app.init();
	}
	
	ControllerManager cManager;
	SpheroManager sManager;
	
	private SpheroApp()
	{
		setLogHandlerLevel(Level.ALL);
		
		// I don't know why I can't set the log level like this
//		Logger.getLogger("net.java.games.input.ControllerEnvironment").setLevel(Level.WARNING);
//		Logger.getLogger("s3.sphero.SpheroManager").setLevel(Level.WARNING);
		
		cManager = ControllerManager.getControllerManager();
		sManager = SpheroManager.getSpheroManager();
	}
	
	private void init()
	{
		Scanner console = new Scanner(System.in);
		console.nextLine();
		console.close();
		
		
		//TODO: make a thread to enforce shutdown after a timeout
		
		System.out.println("Closing sphero manager");
		sManager.close();
		System.out.println("Ending main method");
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
}
