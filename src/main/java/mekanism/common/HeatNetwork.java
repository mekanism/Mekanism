package mekanism.common;

import java.util.Collection;
import java.util.Set;

import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.multipart.MultipartTransmitter;
import mekanism.common.util.MekanismUtils;
import cpw.mods.fml.common.FMLCommonHandler;

public class HeatNetwork extends DynamicNetwork<IHeatTransfer, HeatNetwork>
{
	public double meanTemp = 0;

	public double heatLost = 0;
	public double heatTransferred = 0;

	public HeatNetwork() {}

	public HeatNetwork(Collection<HeatNetwork> networks)
	{
		for(HeatNetwork net : networks)
		{
			if(net != null)
			{
				adoptTransmittersAndAcceptorsFrom(net);
				net.deregister();
			}
		}

		register();
	}

	@Override
	public String getNeededInfo()
	{
		return "Not Applicable";
	}

	@Override
	public String getStoredInfo()
	{
		return MekanismUtils.getTemperatureDisplay(meanTemp, TemperatureUnit.KELVIN) + " above ambient";
	}

	@Override
	public String getFlowInfo()
	{
		return MekanismUtils.getTemperatureDisplay(heatTransferred, TemperatureUnit.KELVIN) + " transferred to acceptors, " + MekanismUtils.getTemperatureDisplay(heatLost, TemperatureUnit.KELVIN) + " lost to environment, " + (heatTransferred + heatLost == 0 ? "" : heatTransferred / (heatTransferred + heatLost) * 100 + "% efficiency");
	}

	@Override
	public void absorbBuffer(IGridTransmitter<IHeatTransfer, HeatNetwork> transmitter) {}

	@Override
	public void clampBuffer() {}

	@Override
	public Set<IHeatTransfer> getAcceptors(Object data)
	{
		return null;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		double newSumTemp = 0;
		double newHeatLost = 0;
		double newHeatTransferred = 0;

		if(FMLCommonHandler.instance().getEffectiveSide().isServer())
		{
			for(IGridTransmitter<IHeatTransfer, HeatNetwork> transmitter : transmitters)
			{
				if(transmitter instanceof MultipartTransmitter && ((MultipartTransmitter)transmitter).getPart() instanceof IHeatTransfer)
				{
					IHeatTransfer heatTransmitter = (IHeatTransfer)((MultipartTransmitter)transmitter).getPart();
					double[] d = heatTransmitter.simulateHeat();
					newHeatTransferred += d[0];
					newHeatLost += d[1];
				}
			}
			
			for(IGridTransmitter<IHeatTransfer, HeatNetwork> transmitter : transmitters)
			{
				if(transmitter instanceof MultipartTransmitter && ((MultipartTransmitter)transmitter).getPart() instanceof IHeatTransfer)
				{
					IHeatTransfer heatTransmitter = (IHeatTransfer)((MultipartTransmitter)transmitter).getPart();
					newSumTemp += heatTransmitter.applyTemperatureChange();
				}
			}
		}
		
		heatLost = newHeatLost;
		heatTransferred = newHeatTransferred;
		meanTemp = newSumTemp / transmitters.size();
	}
}
