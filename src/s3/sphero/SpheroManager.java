package s3.sphero;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import se.nicklasgavelin.bluetooth.*;
import se.nicklasgavelin.bluetooth.Bluetooth.EVENT;
import se.nicklasgavelin.sphero.Robot;
import se.nicklasgavelin.sphero.exception.InvalidRobotAddressException;
import se.nicklasgavelin.sphero.exception.RobotBluetoothException;

public class SpheroManager implements BluetoothDiscoveryListener
{	
	private static Timer timer = new Timer(true);
	private static SpheroManager instance = null;
	
	private Bluetooth bt;
	public Logger logger;
	private volatile boolean searchInProgress = false;
	private ArrayList<Sphero> spheros;
	
	
	public static SpheroManager getSpheroManager()
	{		
		if (instance == null)
		{
			instance = new SpheroManager();
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
	
	private SpheroManager()
	{
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
		logger.info(
			String.format("addr: %s, sphero: %5b, name: \"%s\"",
				d.getAddress(), Robot.isValidDevice(d), d.getName()));
		
		if (Robot.isValidDevice(d))
		{
			try
			{
				Sphero s = new Sphero(new Robot(d));
				if (!spheros.contains(s))
				{
					spheros.add(s);
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
		logger.info("Discover starting...");
	}

	@Override
	public void deviceSearchCompleted(Collection<BluetoothDevice> devices)
	{
		searchInProgress = false;
		logger.info("Discover finished");
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
