package buildcraft.api.gates;

import net.minecraft.src.TileEntity;

public interface ITrigger {

	public abstract int getId();

	/**
	 * Return the texture file for this trigger icon
	 */
	public abstract String getTextureFile();

	/**
	 * Return the icon id in the texture file
	 */
	public abstract int getIndexInTexture();

	/**
	 * Return true if this trigger can accept parameters
	 */
	public abstract boolean hasParameter();

	/**
	 * Return the trigger description in the UI
	 */
	public abstract String getDescription();

	/**
	 * Return true if the tile given in parameter activates the trigger, given
	 * the parameters.
	 */
	public abstract boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter);

	/**
	 * Create parameters for the trigger. As for now, there is only one kind of
	 * trigger parameter available so this subprogram is final.
	 */
	public abstract ITriggerParameter createParameter();

}