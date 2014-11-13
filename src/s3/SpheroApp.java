package s3;

import java.util.Scanner;

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
