package universalelectricity.implement;

import net.minecraftforge.common.ForgeDirection;

/**
 * This interface should be applied onto all tile entities that are
 * rotatable. This interface however is optional and you do not need it for your
 * add-on to function. It just makes things easier for you to code.
 * 
 * @author Calclavia
 * 
 */

public interface IRotatable
{
	/**
	 * Gets the facing direction of the tile entity. Always returns the front
	 * side of the tile entity.
	 * 
	 * @return The facing side from 0-5 The full list of which side the number
	 *         represents is in the UniversalElectricity class.
	 */
	public ForgeDirection getDirection();

	/**
	 * Sets the facing direction of the tile entity.
	 * 
	 * @param facingDirection
	 *            - A direction from 0-5. The full list of which side the number
	 *            represents is in the UniversalElectricity class.
	 */
	public void setDirection(ForgeDirection facingDirection);
}
