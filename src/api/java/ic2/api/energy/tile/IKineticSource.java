package ic2.api.energy.tile;

import net.minecraftforge.common.util.ForgeDirection;

public interface IKineticSource {
	/*
	 *  Return max kinetic energy transmission peer Tick (only theoretical bandwidth not available amount)
	 */
	int  maxrequestkineticenergyTick(ForgeDirection directionFrom);

	/*
	 * @param requested amount of kinetic energy to transfer
	 * 
	 * @return transmitted amount of kineticenergy
	 * 
	 * example: You Request 100 units of kinetic energy but the Source have only 50 units left
	 * 
	 * requestkineticenergy(100) : return 50 : so 50 units of kinetic energy remove from KineticSource
	 */

	int requestkineticenergy(ForgeDirection directionFrom, int requestkineticenergy);
}
