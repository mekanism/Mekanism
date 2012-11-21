package universalelectricity.prefab.multiblock;

import net.minecraft.src.EntityPlayer;

/**
 * A general interface to be implemented by anything that needs it.
 * 
 * @author Calclavia
 * 
 */
public interface IBlockActivate
{
	/**
	 * Called when activated
	 */
	public boolean onActivated(EntityPlayer entityPlayer);
}