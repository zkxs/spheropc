package s3.sphero;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import s3.SpheroApp;
import se.nicklasgavelin.sphero.Robot;
import se.nicklasgavelin.sphero.command.CalibrateCommand;
import se.nicklasgavelin.sphero.command.RollCommand;
import se.nicklasgavelin.sphero.exception.RobotInitializeConnectionFailed;
import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Component.Identifier;

public class Sphero implements Comparable<Sphero>
{	
	private static final Logger logger = Logger.getLogger(Sphero.class.getName());
	private Robot sphero;
	private Controller controller;
	private Timer timer;
	
	public Sphero(Robot sphero)
	{
		this.sphero = sphero;
	}
	
	/**
	 * Set the controller. Set to <code>null</code> to clear the hasController state
	 * @param controller the controller
	 */
	public void setController(Controller controller)
	{
		if (controller != null)
		{
			logger.info(getRobot().getName() + " is now controlled by " + controller.getName() + " @ " + controller.hashCode());
		}
		this.controller = controller;
	}
	
	public boolean hasController()
	{
		return controller != null;
	}
	
	public TimerTask getTimerTask()
	{
		return timerTask;
	}
	
	/**
	 * 
	 * @param timer
	 * @return True if successful
	 */
	public boolean startUpdating(Timer timer)
	{
		this.timer = timer;
		
		Robot sphero = getRobot();
		if (!sphero.isConnected())
		{
			try
			{
				sphero.connect(true);
			}
			catch (RobotInitializeConnectionFailed e)
			{
				setController(null);
				logger.warning(e.getMessage());
				return false;
			}
		}
		
		timer.scheduleAtFixedRate(getTimerTask(), 0, SpheroApp.UPDATE_PERIOD);
		return true;
	}
	
	public Robot getRobot()
	{
		return sphero;
	}
	
	// drive
	private static float DRIVE_MAX = 0.5f; // limit the max speed (unless boosting)
	private static float DRIVE_TURNZONE = 0.45f; // only turns if input less than this magnitude
	private static float DRIVE_DEADZONE = 0.15f; // does nothing if input less than this magnitude
	private static boolean DRIVE_BOOST_ENABLED = true; // disables the boost mechanic
	
	// color
	private static float COLOR_DEADZONE = 0.9f; // does nothing if input less than this magnitude
	
	// calibration
	private static float CALIBRATION_SCALE_FACTOR = 15f; // how fast to turn
	private static float CALIBRATION_DEADZONE = 0.25f;
	private static long CALIBRATION_LED_TIMEOUT = 5000;
	private static float ALIGNMENT_TOLERANCE = 8f; // +- degrees off considered aligned
	
	// don't change these
	private static float ZERO = 0.0f;
	private static float ONE = 1.0f;
	private static float INITIAL = -1.0f;
	private static float DRIVE_NOTTURNZONE = ONE - DRIVE_TURNZONE;
	private static float CALIBRATION_LIVEZONE = ONE - CALIBRATION_DEADZONE;
	private static float CALIBRATION_MOTOR_MAX = 359f;
	
	// components
	private Component xAxis1;
	private Component yAxis1;
	private Component xAxis2 ;
	private Component yAxis2 ;
	private Component axis1Press; 
	private Component axis2Press;
	private Component triggerAxis;
	
	// state
	private boolean backLED = true;
	private long lastCalibrated = -1;
	private double heading; // ranges from -PI to PI
	private float stickLeftHeading = INITIAL; // ranges from 0 to 360
	private float stickLeftMagnitude = INITIAL;
	private float stickRightHeading = INITIAL; // ranges from 0 to 1
	private float stickRightMagnitude = INITIAL;
	private float calibration = ZERO;
	private float motorOut = INITIAL;
	private boolean boost;
	private boolean nextPressIsStealth = true;
	private boolean lastAxis2Press = false;
	private Color color;
	private long loopNumber = 0;
	private boolean calibrating = false;
	private boolean wasCalibrating = false;
	private long litLED = 0;
	private long beganCalibration = 0;
	private boolean moving = false;
	private boolean aligned = false;
	
	/**
	 * Control the Sphero. This is called periodically from a timer
	 * @param controller The controller to use for input
	 */
	public void update(Controller controller)
	{
		if (!controller.poll())
		{
			// TODO: do something
			return;
		}
		
		if (!sphero.isConnected())
		{
			// TODO: do something
			return;
		}
		
		xAxis1 = controller.getComponent(Identifier.Axis.X);
		yAxis1 = controller.getComponent(Identifier.Axis.Y);
		xAxis2 = controller.getComponent(Identifier.Axis.RX);
		yAxis2 = controller.getComponent(Identifier.Axis.RY);
		axis1Press = controller.getComponent(Identifier.Button._8); // starts from 0, so subtract 1
		axis2Press = controller.getComponent(Identifier.Button._9);
		triggerAxis = controller.getComponent(Identifier.Axis.Z);
		

		boost = axis1Press.getPollData() == ONE;
		heading = Math.atan2(-xAxis1.getPollData(), yAxis1.getPollData());
		stickLeftMagnitude  = (float)(Math.hypot(xAxis1.getPollData(), yAxis1.getPollData()));
		stickRightMagnitude = (float) Math.hypot(xAxis2.getPollData(), yAxis2.getPollData());
		
		/*out.printf("lh=%5.2f lm=%5.2f aMax=%5.2f h=%5.2f mtr=%5.2f rh=%5.2f rm=%5.2f boost=%5b cal=%5.2f\n",
				stickLeftHeading,
				stickLeftMagnitude,
				1/inverseAngleMax(heading),
				heading,
				motorOut,
				stickRightHeading,
				stickRightMagnitude,
				boost,
				(calibration - CALIBRATION_DEADZONE) / CALIBRATION_LIVEZONE);*/
		
		
		if (lastCalibrated == -1) lastCalibrated = System.currentTimeMillis();
		
		// color
		if (stickRightMagnitude > COLOR_DEADZONE)
		{
			stickRightHeading = (float)((Math.atan2(-xAxis2.getPollData(), yAxis2.getPollData()) + Math.PI) / (2*Math.PI));
			color = Color.getHSBColor(stickRightHeading, ONE, ONE);
			sphero.setRGBLedColor(color);
		}
		else if (axis2Press.getPollData() == ONE)
		{
			lastAxis2Press = true;
		}
		else // not pressed
		{
			if (lastAxis2Press)
			{	// was pressed
				if (nextPressIsStealth)
				{	// stealth
					color = Color.getHSBColor(stickRightHeading, ONE, ZERO);
				}
				else
				{	// white
					color = Color.getHSBColor(stickRightHeading, ZERO, ONE);
				}
				nextPressIsStealth = !nextPressIsStealth;
				sphero.setRGBLedColor(color);
			}
			
			lastAxis2Press = false;
			
		}
		
		// drive
		if (stickLeftMagnitude > DRIVE_DEADZONE)
		{
			stickLeftHeading = (float)Math.toDegrees(heading + Math.PI);
			if (ALIGNMENT_TOLERANCE > stickLeftHeading || stickLeftHeading > 360 - ALIGNMENT_TOLERANCE)
			{
				aligned = true;
				logger.log(Level.FINE, "aligned=" + aligned);
			}
			else
			{
				aligned = false;
			}
		}
		
		if (stickLeftMagnitude > DRIVE_TURNZONE)
		{	// go
			if (boost && DRIVE_BOOST_ENABLED)
			{
				sphero.sendCommand(new RollCommand(stickLeftHeading, ONE, false));
			}
			else
			{
				motorOut = (float)(inverseAngleMax(heading) * DRIVE_MAX * (stickLeftMagnitude - DRIVE_TURNZONE) / DRIVE_NOTTURNZONE);
				if (motorOut > 1) motorOut = 1;
				sphero.sendCommand(new RollCommand(stickLeftHeading, motorOut, false));
			}
			moving = true;
		}
		else if (stickLeftMagnitude > DRIVE_DEADZONE)
		{	// rotate
			sphero.sendCommand(new RollCommand(stickLeftHeading, ZERO, true));
			//sphero.rotate(stickLeftHeading);
		}
		else
		{	// stop
			//sphero.sendCommand(new se.nicklasgavelin.sphero.command.RawMotorCommand(MOTOR_MODE.REVERSE, 0, MOTOR_MODE.REVERSE, 0));
			//sphero.sendCommand(new RollCommand(ZERO, ONE, true));
			sphero.stopMotors(); // ?
			moving = false;
		}
		
		// calibration
		calibration = -triggerAxis.getPollData(); // backwards
		if (Math.abs(calibration) > CALIBRATION_DEADZONE)
		{
			calibration = Math.signum(calibration) * CALIBRATION_SCALE_FACTOR * ( Math.abs(calibration) - CALIBRATION_DEADZONE) / CALIBRATION_LIVEZONE;
			if (calibration < 0) calibration += CALIBRATION_MOTOR_MAX;
			
			if (!backLED || System.currentTimeMillis() - litLED > CALIBRATION_LED_TIMEOUT - 100)
			{
				sphero.setFrontLEDBrightness(ONE);
				backLED = true;
				litLED = System.currentTimeMillis();
				logger.log(Level.FINE, "Led Renew");
			}
			
			if (!moving)
			{
				if (!wasCalibrating)
				{
					beganCalibration = System.currentTimeMillis();
				}
				
				if (!aligned && System.currentTimeMillis() - beganCalibration < 300)
				{
					lastCalibrated = System.currentTimeMillis();
					sphero.rotate(ZERO);
					//sphero.sendCommand(new RollCommand(ZERO, ZERO, true));
				}
				else
				{
					aligned = true;
					lastCalibrated = System.currentTimeMillis();
					sphero.sendCommand(new CalibrateCommand(calibration));
				}
			}
			else
			{
				aligned = true;
				lastCalibrated = System.currentTimeMillis();
				sphero.sendCommand(new CalibrateCommand(calibration));
			}
			
			wasCalibrating = true;
		}
		else
		{
			wasCalibrating = false;
		}
		
		if (backLED && System.currentTimeMillis() - lastCalibrated >= CALIBRATION_LED_TIMEOUT)
		{
			sphero.setFrontLEDBrightness(ZERO);
			backLED = false;
		}
		
		
		loopNumber++;
	}
		
	/**
	 * Returns the inverse of the maximum magnitude of a joystick at a given angle
	 * @param angle the angle in radians between -PI and PI
	 * @return the inverse of the max magnitude
	 */
	private static double inverseAngleMax(double angle)
	{
		if ((-3 * Math.PI / 4) < angle && angle <= (-Math.PI / 4))
		{
			//out.print('N');
			return -Math.sin(angle);
		}
		else if ((-Math.PI / 4) < angle && angle <= (Math.PI / 4))
		{
			//out.print('E');
			return Math.cos(angle);
		}
		else if ((Math.PI / 4) < angle && angle <= (3 * Math.PI / 4))
		{
			//out.print('S');
			return Math.sin(angle);
		}
		else if (Math.abs(angle) > Math.PI)
		{
			//out.print('?');
			return 0;
		}
		else
		{
			//out.print('W');
			return -Math.cos(angle);
		}
	}

	@Override
	public int compareTo(Sphero that)
	{
		// return (this - that);
		
		return this.getRobot().getId().compareTo(that.getRobot().getId());
	}

	@Override
	public int hashCode()
	{
		return getRobot().getId().hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Sphero other = (Sphero) obj;
		return this.getRobot().getId().equals(other.getRobot().getId());
	}
	
	private TimerTask timerTask = new TimerTask(){

		@Override
		public void run()
		{
			update(controller);
		}	
	};
}
