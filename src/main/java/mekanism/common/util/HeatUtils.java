package mekanism.common.util;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.IGridTransmitter;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class HeatUtils
{
	public static double[] simulate(IHeatTransfer source, Coord4D pos, World world)
	{
		double heatTransferred[] = new double[]{0,0};
		for(EnumFacing side : EnumFacing.values())
		{
			IHeatTransfer sink = source.getAdjacent(side);
			if(sink != null)
			{
				double invConduction = sink.getInverseConductionCoefficient() + source.getInverseConductionCoefficient();
				double heatToTransfer = source.getTemp() / invConduction;
				source.transferHeatTo(-heatToTransfer);
				sink.transferHeatTo(heatToTransfer);
				if(!(sink instanceof IGridTransmitter))
					heatTransferred[0] += heatToTransfer;
				continue;
			}
			//Transfer to air otherwise
			double heatToTransfer = source.getTemp() / (IHeatTransfer.AIR_INVERSE_COEFFICIENT+source.getInsulationCoefficient(side)+source.getInverseConductionCoefficient());
			source.transferHeatTo(-heatToTransfer);
			heatTransferred[1] += heatToTransfer;
		}
		return heatTransferred;
	}
}
