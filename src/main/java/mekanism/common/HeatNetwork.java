package mekanism.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.DynamicNetwork;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;

import cpw.mods.fml.common.FMLCommonHandler;

public class HeatNetwork extends DynamicNetwork<IHeatTransfer, HeatNetwork>
{
	public double meanTemp = 0;

	public double heatLost = 0;
	public double heatTransferred = 0;

	public HeatNetwork(IGridTransmitter<HeatNetwork>... varPipes)
	{
		transmitters.addAll(Arrays.asList(varPipes));
		register();
	}

	public HeatNetwork(Collection<IGridTransmitter<HeatNetwork>> collection)
	{
		transmitters.addAll(collection);
		register();
	}

	public HeatNetwork(Set<HeatNetwork> networks)
	{
		for(HeatNetwork net : networks)
		{
			if(net != null)
			{
				addAllTransmitters(net.transmitters);
				net.deregister();
			}
		}

		register();
	}


	@Override
	protected HeatNetwork create(Collection<IGridTransmitter<HeatNetwork>> collection)
	{
		return new HeatNetwork(collection);
	}

	@Override
	public String getNeededInfo()
	{
		return "Not Applicable";
	}

	@Override
	public String getStoredInfo()
	{
		return meanTemp + "K above ambient";
	}

	@Override
	public String getFlowInfo()
	{
		return heatTransferred + " transferred to acceptors,  " + heatLost + " lost to environment, " + (heatTransferred + heatLost == 0 ? "" : heatTransferred / (heatTransferred + heatLost) * 100 + "% efficiency");
	}

	@Override
	public Set<IHeatTransfer> getAcceptors(Object... data)
	{
		return null;
	}

	@Override
	public void refresh()
	{
		Set<IGridTransmitter<HeatNetwork>> iterPipes = (Set<IGridTransmitter<HeatNetwork>>)transmitters.clone();
		Iterator<IGridTransmitter<HeatNetwork>> it = iterPipes.iterator();
		boolean networkChanged = false;

		while(it.hasNext())
		{
			IGridTransmitter<HeatNetwork> conductor = it.next();

			if(conductor == null || conductor.getTile().isInvalid())
			{
				it.remove();
				networkChanged = true;
				transmitters.remove(conductor);
			}
			else {
				conductor.setTransmitterNetwork(this);
			}
		}

		if(networkChanged)
		{
			updateCapacity();
		}
	}

	@Override
	public void refresh(IGridTransmitter<HeatNetwork> transmitter)
	{
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.HEAT;
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
			for(IGridTransmitter<HeatNetwork> transmitter : transmitters)
			{
				if(transmitter instanceof IHeatTransfer)
				{
					IHeatTransfer heatTransmitter = (IHeatTransfer)transmitter;
					double[] d = heatTransmitter.simulateHeat();
					newHeatTransferred += d[0];
					newHeatLost += d[1];
				}
			}
			for(IGridTransmitter<HeatNetwork> transmitter : transmitters)
			{
				if(transmitter instanceof IHeatTransfer)
				{
					IHeatTransfer heatTransmitter = (IHeatTransfer)transmitter;
					newSumTemp += heatTransmitter.applyTemperatureChange();
				}
			}
		}
		heatLost = newHeatLost;
		heatTransferred = newHeatTransferred;
		meanTemp = newSumTemp / transmitters.size();
	}
}
