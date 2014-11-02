package se.nicklasgavelin.sphero.macro.command;

import se.nicklasgavelin.sphero.macro.MacroCommand;
import se.nicklasgavelin.util.ByteArrayBuffer;
import se.nicklasgavelin.util.Value;

/**
 * Macro ROLL command
 * 
 * @author Orbotix
 * @author Nicklas Gavelin, nicklas.gavelin@gmail.com, Luleå University of
 *         Technology
 */
public class Roll extends MacroCommand
{
	private int delay; // = Integer.valueOf( 0 );
	private int heading; // = Integer.valueOf( 0 );
	private double speed; // = Double.valueOf( 0.5D );
	public static final double MIN_SPEED = 0, MAX_SPEED = 1;
	public static final int MIN_DELAY = 0, MAX_DELAY = 255, MIN_HEADING = 0,
			MAX_HEADING = 359;

	// public Roll( byte[] data )
	// {
	// }

	/**
	 * Create a Roll macro command with a given speed, heading and delay
	 * 
	 * @param _speed The speed (MIN_SPEED - MAX_SPEED)
	 * @param _heading The heading (MIN_HEADING - MAX_HEADING)
	 * @param _delay The dealy (MIN_DELAY - MAX_DELAY)
	 */
	public Roll( double _speed, int _heading, int _delay )
	{
		super( MACRO_COMMAND.MAC_ROLL );

		this.setSpeed( _speed );
		this.setHeading( _heading );
		this.setDelay( _delay );
	}

	public Integer getDelay()
	{
		return this.delay;
	}

	/**
	 * Update the internal delay value
	 * 
	 * @param _delay The internal delay
	 */
	public void setDelay( int _delay )
	{
		this.delay = Value.clamp( _delay, MIN_DELAY, MAX_DELAY );
	}

	/**
	 * Returns the internal heading value
	 * 
	 * @return The internal heading value
	 */
	public int getHeading()
	{
		return this.heading;
	}

	/**
	 * Update the internal heading value
	 * 
	 * @param _heading The new heading value (MIN_HEADING - MAX_HEADING)
	 */
	public void setHeading( int _heading )
	{
		this.heading = Value.clamp( _heading, MIN_HEADING, MAX_HEADING );
		// if ( (_heading.intValue() >= 0) && (_heading.intValue() <= 359) )
		// this.heading = _heading;
	}

	/**
	 * Returns the internal speed value
	 * 
	 * @return The internal speed value
	 */
	public double getSpeed()
	{
		return this.speed;
	}

	/**
	 * Update the internal speed value
	 * 
	 * @param _speed The new internal speed value (MIN_SPEED - MAX_SPEED)
	 */
	public void setSpeed( double _speed )
	{
		this.speed = Value.clamp( _speed, MIN_SPEED, MAX_SPEED );
		// if ( (_speed.doubleValue() >= 0.0D) && (_speed.doubleValue() <= 1.0D) )
		// this.speed = _speed;
	}

	@Override
	public byte[] getByteRepresentation()
	{
		ByteArrayBuffer bytes = new ByteArrayBuffer( getLength() );
		bytes.append( getCommandID() );
		bytes.append( (int) ( this.speed * 255.0D ) );
		bytes.append( this.heading >> 8 );
		bytes.append( this.heading & 0xFF );
		bytes.append( this.delay );

		return bytes.toByteArray();
	}
}