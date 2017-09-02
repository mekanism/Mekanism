package mekanism.common.base;

import mekanism.common.util.LangUtils;

public interface IRedstoneControl
{
	enum RedstoneControl
	{
		DISABLED("control.disabled"),
		HIGH("control.high"),
		LOW("control.low"),
		PULSE("control.pulse");

		private String display;

		public String getDisplay()
		{
			return LangUtils.localize(display);
		}

		RedstoneControl(String s)
		{
			display = s;
		}
	}

	/**
	 * Gets the RedstoneControl type from this block.
	 * @return this block's RedstoneControl type
	 */
    RedstoneControl getControlType();

	/**
	 * Sets this block's RedstoneControl type to a new value.
	 * @param type - RedstoneControl type to set
	 */
    void setControlType(RedstoneControl type);

	/**
	 * If the block is getting powered or not by redstone (indirectly).
	 * @return if the block is getting powered indirectly
	 */
    boolean isPowered();

	/**
	 * If the block was getting powered or not by redstone, last tick.
	 * Used for PULSE mode.
	 */
    boolean wasPowered();

	/**
	 * If the machine can be pulsed.
	 */
    boolean canPulse();
}
