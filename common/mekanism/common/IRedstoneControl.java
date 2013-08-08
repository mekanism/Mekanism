package mekanism.common;

public interface IRedstoneControl 
{
	public static enum RedstoneControl
	{
		DISABLED("Disabled"),
		HIGH("High"),
		LOW("Low");
		
		private String display;
	
		public String getDisplay()
		{
			return display;
		}
		
		private RedstoneControl(String s)
		{
			display = s;
		}
	}
	
	/**
	 * Gets the RedstoneControl type from this block.
	 * @return this block's RedstoneControl type
	 */
	public RedstoneControl getControlType();
	
	/**
	 * Sets this block's RedstoneControl type to a new value.
	 * @param type - RedstoneControl type to set
	 */
	public void setControlType(RedstoneControl type);
}
