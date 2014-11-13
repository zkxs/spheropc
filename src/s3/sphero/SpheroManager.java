package s3.sphero;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import se.nicklasgavelin.bluetooth.*;
import se.nicklasgavelin.bluetooth.Bluetooth.EVENT;
import se.nicklasgavelin.sphero.Robot;

public class SpheroManager implements BluetoothDiscoveryListener
{	
	private static Timer timer = new Timer(true);
	private static SpheroManager instance = null;
	
	private Bluetooth bt;
	
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
		bt = new Bluetooth(this, Bluetooth.SERIAL_COM);
	}
	
	private void init()
	{
		timer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run()
			{
				bt.discover();
			}
			
		}, 0, 10000); // 100 ms rescan delay
	}

	@Override
	public void deviceSearchCompleted(Collection<BluetoothDevice> devices)
	{
		// TODO Auto-generated method stub
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
		System.err.printf("Discover Error: %s\n", error.getErrorMessage());
		
	}

	@Override
	public void deviceSearchStarted()
	{
		// TODO Auto-generated method stub
		System.out.println("Discover starting...");
	}
	
	public void close()
	{
		timer.cancel();
		bt.cancelDiscovery();
	}
	
}
