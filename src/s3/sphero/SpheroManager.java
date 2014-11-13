package s3.sphero;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import se.nicklasgavelin.bluetooth.*;
import se.nicklasgavelin.bluetooth.Bluetooth.EVENT;
import se.nicklasgavelin.sphero.Robot;

public class SpheroManager implements BluetoothDiscoveryListener
{	
	private static Timer timer = new Timer(true);
	private static SpheroManager instance = null;
	
	private Bluetooth bt;
	public Logger logger;
	private volatile boolean searchInProgress = false;
	
	public static SpheroManager getSpheroManager()
	{		
		if (instance == null)
		{
			instance = new SpheroManager();
			instance.init();
		}
		
		return instance;
	}
	
	private SpheroManager()
	{
		logger = Logger.getLogger(this.getClass().getName());
		bt = new Bluetooth(this, Bluetooth.SERIAL_COM);
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
		// TODO Auto-generated method stub
		System.out.printf("%s %5b \"%s\"\n", d.getAddress(), Robot.isValidDevice(d), d.getName());
		
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
	}
	
}
