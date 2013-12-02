package mekanism.induction.common.wire;

import ic2.api.energy.tile.IEnergySink;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;

public class TEnergySink extends TileMultipart implements IEnergySink
{
	public Set<IEnergySink> ic2Sinks = new HashSet<IEnergySink>();

	@Override
	public void copyFrom(TileMultipart that)
	{
		super.copyFrom(that);
		if (that instanceof TEnergySink)
			ic2Sinks = ((TEnergySink) that).ic2Sinks;
	}

	@Override
	public void bindPart(TMultiPart part)
	{
		super.bindPart(part);
		if (part instanceof IEnergySink)
			ic2Sinks.add((IEnergySink) part);
	}

	@Override
	public void partRemoved(TMultiPart part, int p)
	{
		super.partRemoved(part, p);
		if (part instanceof IEnergySink)
			ic2Sinks.remove(part);
	}

	@Override
	public void clearParts()
	{
		super.clearParts();
		ic2Sinks.clear();
	}

	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		for (IEnergySink sink : this.ic2Sinks)
		{
			if (sink.acceptsEnergyFrom(emitter, direction))
				return true;
		}
		return false;
	}

	@Override
	public double demandedEnergyUnits()
	{
		double demanded = 0;

		for (IEnergySink sink : this.ic2Sinks)
		{
			demanded += sink.demandedEnergyUnits();
		}
		return demanded;
	}

	@Override
	public double injectEnergyUnits(ForgeDirection directionFrom, double amount)
	{
		for (IEnergySink sink : this.ic2Sinks)
		{
			amount = sink.injectEnergyUnits(directionFrom, Math.min(amount, sink.demandedEnergyUnits()));
		}
		return amount;
	}

	@Override
	public int getMaxSafeInput()
	{
		int safe = 0;
		for (IEnergySink sink : this.ic2Sinks)
		{
			safe += sink.getMaxSafeInput();
		}
		return safe;
	}

}
