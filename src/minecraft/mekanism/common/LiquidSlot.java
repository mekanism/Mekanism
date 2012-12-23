package mekanism.common;

/**
 * Used to manage a slot that stores liquid. Has 3 main values -- a stored amount of liquid,
 * maximum liquid, and liquid ID.
 * @author AidanBrady
 *
 */
public class LiquidSlot 
{
	/** The amount of liquid this slot is currently holding. */
	public int liquidStored;
	
	/** The maximum amount of liquid this slot can handle. */
	public int MAX_LIQUID;
	
	/** The liquid's still block ID. 9 for water. */
	public int liquidID;
	
	/**
	 * Creates a LiquidSlot with a defined liquid ID and max liquid. The liquid stored starts at 0.
	 * @param max - max liquid
	 * @param id - liquid id
	 */
	public LiquidSlot(int max, int id)
	{
		MAX_LIQUID = max;
		liquidID = id;
	}
	
	/**
	 * Sets the liquid to a new amount.
	 * @param liquid - amount to store
	 */
	public void setLiquid(int amount)
	{
		liquidStored = Math.max(Math.min(amount, MAX_LIQUID), 0);
	}
}
