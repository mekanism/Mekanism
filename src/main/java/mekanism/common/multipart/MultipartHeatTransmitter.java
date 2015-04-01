package mekanism.common.multipart;

import mekanism.api.IHeatTransfer;
import mekanism.common.HeatNetwork;
import mekanism.common.util.HeatUtils;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import codechicken.lib.colour.Colour;
import codechicken.lib.colour.ColourRGBA;

/**
 * Created by ben on 01/04/15.
 */
public class MultipartHeatTransmitter extends MultipartTransmitter<IHeatTransfer, HeatNetwork> implements IHeatTransfer
{
	public double temperature = 0;
	public double clientTemperature = 0;
	public double heatToAbsorb = 0;

	public HeatMaterial material = HeatMaterial.DEFAULT;

	public MultipartHeatTransmitter(PartHeatTransmitter multiPart)
	{
		super(multiPart);
	}

	@Override
	public double getTemp()
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return material.inverseConduction;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return material.inverseConductionInsulation;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat()
	{
		return HeatUtils.simulate(this);
	}

	@Override
	public double applyTemperatureChange()
	{
		temperature += material.inverseHeatCapacity * heatToAbsorb;
		heatToAbsorb = 0;
		if(Math.abs(temperature - clientTemperature) > (temperature / 100))
		{
			clientTemperature = temperature;
			getPart().sendTemp();
		}
		return temperature;
	}

	@Override
	public boolean canConnectHeat(ForgeDirection side)
	{
		return true;
	}

	@Override
	public IHeatTransfer getAdjacent(ForgeDirection side)
	{
		if(getPart().connectionMapContainsSide(getPart().getAllCurrentConnections(), side))
		{
			TileEntity adj = coord().getFromSide(side).getTileEntity(world());
			if(adj instanceof IHeatTransfer)
			{
				return (IHeatTransfer)adj;
			}
		}
		return null;
	}

	@Override
	public PartHeatTransmitter getPart()
	{
		return (PartHeatTransmitter)containingPart;
	}

	public static enum HeatMaterial
	{
		DEFAULT(5, 1, 0, new ColourRGBA(0.2, 0.2, 0.2, 1));

		double inverseConduction;
		double inverseHeatCapacity;
		double inverseConductionInsulation;
		ColourRGBA baseColour;

		private HeatMaterial(double inversek, double inverseC, double insulationInversek, ColourRGBA colour)
		{
			inverseConduction = inversek;
			inverseHeatCapacity = inverseC;
			inverseConductionInsulation = insulationInversek;
			baseColour = colour;
		}
	}
}
