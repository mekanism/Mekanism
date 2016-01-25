package ic2.api.energy.tile;

import net.minecraft.util.EnumFacing;

public interface IHeatSource {
	
	/*
	 *  Return max heat transmission peer Tick (only theoretical bandwidth not available amount)
	 */
	int  maxrequestHeatTick(EnumFacing directionFrom);
	
	/*
	 * @param requested amount of heat to transfer
	 * 
	 * @return transmitted amount of heat
	 * 
	 * example: You Request 100 units of heat but the Source have only 50 units left
	 * 
	 * requestHeat(100) : return 50 : so 50 units of heat remove from HeatSource
	 */
	
	int requestHeat(EnumFacing directionFrom, int requestheat);
}
