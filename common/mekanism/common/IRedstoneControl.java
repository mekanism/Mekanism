package mekanism.common;

import mekanism.common.util.MekanismUtils;

public interface IRedstoneControl 
{
	public static enum RedstoneControl
	{
		DISABLED("control.disabled"),
		HIGH("control.high"),
		LOW("control.low");
		
		private String display;
	
		public String getDisplay()
		{
			return MekanismUtils.localize(display);
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
