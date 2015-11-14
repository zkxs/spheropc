package s3.sphero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import net.java.games.input.Controller;
import s3.SpheroApp;
import s3.util.Option;
import se.nicklasgavelin.bluetooth.*;
import se.nicklasgavelin.bluetooth.Bluetooth.EVENT;
import se.nicklasgavelin.sphero.Robot;
import se.nicklasgavelin.sphero.command.CommandMessage;
import se.nicklasgavelin.sphero.exception.InvalidRobotAddressException;
import se.nicklasgavelin.sphero.exception.RobotBluetoothException;

public class SpheroManager implements BluetoothDiscoveryListener
{	
	private static Timer timer = new Timer(true);
	private static SpheroManager instance = null;
	
	private SpheroApp spheroApp;
	
	private Bluetooth bt;
	public Logger logger;
	private volatile boolean searchInProgress = false;
	private ArrayList<Sphero> spheros;
	
	
	public static SpheroManager getSpheroManager(SpheroApp spheroApp)
	{		
		if (instance == null)
		{
			instance = new SpheroManager(spheroApp);
			instance.init();
		}
		
		return instance;
	}
	
	/**
	 * Get the array of Spheros. Don't modify it
	 * @return the array of Spheros
	 */
	public ArrayList<Sphero> getSpheros()
	{
		return spheros;
	}
	
	private SpheroManager(SpheroApp spheroApp)
	{
		this.spheroApp = spheroApp;
		logger = Logger.getLogger(this.getClass().getName());
		bt = new Bluetooth(this, Bluetooth.SERIAL_COM);
		spheros = new ArrayList<Sphero>();
	}
	
	private void init()
	{
		//TODO: should this be done constantly?
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run()
			{
				// doing multiple searches at once is bad
				if (!searchInProgress)
				{
					bt.discover();
				}
			}
			
		}, 1000, 30000); // 30s rescan delay
	}
	
	@Override
	public void deviceDiscovered(BluetoothDevice d)
	{
		if (Robot.isValidDevice(d))
		{
			System.out.println(
				String.format("Discovered bluetooth device: {addr: %s, sphero: %5b, name: \"%s\"}",
					d.getAddress(), Robot.isValidDevice(d), d.getName()));
			
			try
			{
				Sphero s = new Sphero(spheroApp, new Robot(d));
				
				int idx = spheros.indexOf(s);
				if (idx == -1) // if not contained already
				{
					spheros.add(s); // add it
				}
				else // else mark it as alive
				{
					spheros.get(idx).setDead(false);
				}
			}
			catch (InvalidRobotAddressException | RobotBluetoothException e)
			{
				throw new RuntimeException(e);
			}
		}	
	}
	
	@Override
	public void deviceSearchStarted()
	{
		searchInProgress = true;
		System.out.println("Discover starting...");
	}

	@Override
	public void deviceSearchCompleted(Collection<BluetoothDevice> devices)
	{
		searchInProgress = false;
		System.out.println("Discover finished");
		
		int counter = 0;
		StringBuilder sbLiveSpheros = new StringBuilder();
		sbLiveSpheros.append("Live spheros: ");
		StringBuilder sbDeadSpheros = new StringBuilder();
		sbDeadSpheros.append("Dead spheros: ");
		for (Sphero s : spheros)
		{
			if (!s.isDead())
			{
				sbLiveSpheros.append(s.getRobot().getName());
				sbLiveSpheros.append(", ");
			}
			else
			{
				sbDeadSpheros.append(s.getRobot().getName());
				sbDeadSpheros.append(", ");
			}
		}
		System.out.println(sbLiveSpheros.toString());
		System.out.println(sbDeadSpheros.toString());
		
		final Map<Controller, Option<Sphero>> cMap = spheroApp.getControllerManager().getControllerMap();
		final Set<Controller> controllers = cMap.keySet();
		
		StringBuilder sb2 = new StringBuilder();
		sb2.append("Searching controllers: ");
		
		for (Controller c : controllers)
		{
			Option<Sphero> option = cMap.get(c);
			if (option.isNull())
			{
				sb2.append(c.getName() + "@" + c.hashCode());
				sb2.append(", ");
			}
			else
			{
				Robot r = option.get().getRobot();
				if (r.isConnected())
				{
					counter++;
					//r.sendCommand(new CommandMessage(CommandMessage.COMMAND_MESSAGE_TYPE.GET_CONFIGURATION_BLOCK));
				}
			}
		}
		System.out.println("Bound controllers: " + counter);
		System.out.println(sb2.toString());
		System.out.println(); // blank line
	}

	@Override
	public void deviceSearchFailed(EVENT error)
	{
		logger.warning("Discover Error: "+ error.getErrorMessage());
	}

	
	public void close()
	{
		timer.cancel();
		bt.cancelDiscovery();
		
		for (Sphero s : spheros)
		{
			Robot r = s.getRobot();
			if (r.isConnected())
			{
				r.disconnect();
			}
		}
		
	}
	
}
