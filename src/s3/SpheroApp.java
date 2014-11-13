package s3;

import java.util.Scanner;
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
		Logger.getLogger("net.java.games.input.ControllerEnvironment").setLevel(Level.WARNING);
		
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
}
