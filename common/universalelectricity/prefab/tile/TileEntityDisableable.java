package universalelectricity.prefab.tile;

import universalelectricity.prefab.implement.IDisableable;

/**
 * An easier way to implement the methods from IElectricityDisableable with default values set.
 * 
 * @author Calclavia
 */
public abstract class TileEntityDisableable extends TileEntityAdvanced implements IDisableable
{
	protected int disabledTicks = 0;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		if (this.disabledTicks > 0)
		{
			this.disabledTicks--;
			this.whileDisable();
			return;
		}
	}

	/**
	 * Called every tick while this tile entity is disabled.
	 */
	protected void whileDisable()
	{

	}

	@Override
	public void onDisable(int duration)
	{
		this.disabledTicks = duration;
	}

	@Override
	public boolean isDisabled()
	{
		return this.disabledTicks > 0;
	}
}
