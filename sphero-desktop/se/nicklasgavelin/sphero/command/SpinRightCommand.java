package se.nicklasgavelin.sphero.command;

import se.nicklasgavelin.sphero.command.RawMotorCommand.MOTOR_MODE;

/**
 * Command to spin the Sphero right with a given speed.
 *
 * NOTICE: Sending this command will result in some future commands to fail
 * to execute on the Sphero for some reason. Has something to do with the logic
 * on the Sphero
 * @author Nicklas Gavelin, nicklas.gavelin@gmail.com, Luleå University of Technology
 */
public class SpinRightCommand extends CommandMessage
{
    private int speed;


    /**
     * Create a spin left command
     *
     * @param speed The speed to spin at 0-255
     */
    public SpinRightCommand( int speed )
    {
        super( COMMAND_MESSAGE_TYPE.SPIN_RIGHT );
        this.speed = speed;
    }


    @Override
    protected byte[] getPacketData()
    {
        byte[] data = 
        {
        		( byte ) MOTOR_MODE.REVERSE.getValue(),
        		( byte ) this.speed,
        		( byte ) MOTOR_MODE.FORWARD.getValue(),
        		( byte ) this.speed
        };

        return data;
    }
}
