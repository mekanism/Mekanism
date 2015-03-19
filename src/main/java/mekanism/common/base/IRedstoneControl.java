package mekanism.common.base;

import mekanism.common.util.MekanismUtils;

public interface IRedstoneControl
{
	public static enum RedstoneControl
	{
		DISABLED("control.disabled"),
		HIGH("control.high"),
		LOW("control.low"),
		PULSE("control.pulse");

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

	/**
	 * If the block is getting powered or not by redstone (indirectly).
	 * @return if the block is getting powered indirectly
	 */
	public boolean isPowered();

	/**
	 * If the block was getting powered or not by redstone, last tick.
	 * Used for PULSE mode.
	 */
	public boolean wasPowered();

	/**
	 * If the machine can be pulsed.
	 */
	public boolean canPulse();
}
