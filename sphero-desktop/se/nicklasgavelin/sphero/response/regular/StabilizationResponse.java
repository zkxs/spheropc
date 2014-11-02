package se.nicklasgavelin.sphero.response.regular;

import se.nicklasgavelin.sphero.response.ResponseMessage;

/**
 * Response error the StabilizationCommand
 * 
 * @author Nicklas Gavelin, nicklas.gavelin@gmail.com, Luleå University of Technology
 */
public class StabilizationResponse extends ResponseMessage
{
	/**
	 * Create a StabilizationResponse from the received data
	 * 
	 * @param rh The response header containing the response data
	 */
	public StabilizationResponse( ResponseHeader rh )
	{
		super( rh );
	}
}
