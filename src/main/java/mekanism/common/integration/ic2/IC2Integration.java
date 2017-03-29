package mekanism.common.integration.ic2;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import mekanism.api.MekanismConfig.general;
import mekanism.common.base.IEnergyWrapper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Method;

public class IC2Integration 
{
	@Method(modid = "IC2")
	public static boolean isOutputter(TileEntity tileEntity, EnumFacing side)
	{
		IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
		
		if(tile instanceof IEnergySource && ((IEnergySource)tile).emitsEnergyTo(null, side.getOpposite()))
		{
			return true;
		}
		
		return false;
	}
	
	@Method(modid = "IC2")
	public static boolean isAcceptor(TileEntity orig, TileEntity tileEntity, EnumFacing side)
	{
		IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
		
		if(tile instanceof IEnergySink)
		{
			if(((IEnergySink)tile).acceptsEnergyFrom(null, side.getOpposite()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Method(modid = "IC2")
	public static double emitEnergy(IEnergyWrapper from, TileEntity tileEntity, EnumFacing side, double currentSending)
	{
		IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
		
		if(tile instanceof IEnergySink && ((IEnergySink)tile).acceptsEnergyFrom(from, side.getOpposite()))
		{
			double toSend = Math.min(currentSending*general.TO_IC2, EnergyNet.instance.getPowerFromTier(((IEnergySink)tile).getSinkTier()));
			toSend = Math.min(Math.min(toSend, ((IEnergySink)tile).getDemandedEnergy()), Integer.MAX_VALUE);
			return (toSend - (((IEnergySink)tile).injectEnergy(side.getOpposite(), toSend, 0)))*general.FROM_IC2;
		}
		
		return 0;
	}
}
