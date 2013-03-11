package mekanism.common;

public interface IBoundingBlock 
{
	/**
	 * Called when the main block is placed.
	 */
	public void onPlace();
	
	/**
	 * Called when any part of the structure is broken.
	 */
	public void onBreak();
}
