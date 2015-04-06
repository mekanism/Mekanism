package mekanism.common.util;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.ITransmitterTile;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class HeatUtils
{
	public static double[] simulate(IHeatTransfer source)
	{
		double heatTransferred[] = new double[] {0,0};
		
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			IHeatTransfer sink = source.getAdjacent(side);
			
			if(sink != null)
			{
				double invConduction = sink.getInverseConductionCoefficient() + source.getInverseConductionCoefficient();
				double heatToTransfer = source.getTemp() / invConduction;
				source.transferHeatTo(-heatToTransfer);
				sink.transferHeatTo(heatToTransfer);
				
				if(!(sink instanceof ITransmitterTile))
				{
					heatTransferred[0] += heatToTransfer;
				}
				
				continue;
			}
			
			//Transfer to air otherwise
			double invConduction = IHeatTransfer.AIR_INVERSE_COEFFICIENT + source.getInsulationCoefficient(side) + source.getInverseConductionCoefficient();
			double heatToTransfer = source.getTemp() / invConduction;
			source.transferHeatTo(-heatToTransfer);
			heatTransferred[1] += heatToTransfer;
		}
		
		return heatTransferred;
	}
}
