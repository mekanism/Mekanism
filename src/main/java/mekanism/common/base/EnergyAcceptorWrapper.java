package mekanism.common.base;

import cofh.redstoneflux.api.IEnergyReceiver;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import mekanism.api.Coord4D;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.util.CapabilityUtils;
import mekanism.common.util.MekanismUtils;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public abstract class EnergyAcceptorWrapper implements IStrictEnergyAcceptor
{
	public Coord4D coord;

	public static EnergyAcceptorWrapper get(TileEntity tileEntity, EnumFacing side)
	{
		if(tileEntity == null || tileEntity.getWorld() == null)
		{
			return null;
		}
		
		EnergyAcceptorWrapper wrapper = null;
		
		if(CapabilityUtils.hasCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side))
		{
			wrapper = new MekanismAcceptor(CapabilityUtils.getCapability(tileEntity, Capabilities.ENERGY_ACCEPTOR_CAPABILITY, side));
		}
		else if(MekanismUtils.useTesla() && CapabilityUtils.hasCapability(tileEntity, Capabilities.TESLA_CONSUMER_CAPABILITY, side))
		{
			wrapper = new TeslaAcceptor(CapabilityUtils.getCapability(tileEntity, Capabilities.TESLA_CONSUMER_CAPABILITY, side));
		}
		else if(MekanismUtils.useForge() && CapabilityUtils.hasCapability(tileEntity, CapabilityEnergy.ENERGY, side))
		{
			wrapper = new ForgeAcceptor(CapabilityUtils.getCapability(tileEntity, CapabilityEnergy.ENERGY, side));
		}
		else if(MekanismUtils.useRF() && tileEntity instanceof IEnergyReceiver)
		{
			wrapper = new RFAcceptor((IEnergyReceiver)tileEntity);
		}
		else if(MekanismUtils.useIC2())
		{
			IEnergyTile tile = EnergyNet.instance.getSubTile(tileEntity.getWorld(), tileEntity.getPos());
			
			if(tile instanceof IEnergySink)
			{
				wrapper = new IC2Acceptor((IEnergySink)tile);
			}
		}
		
		if(wrapper != null)
		{
			wrapper.coord = Coord4D.get(tileEntity);
		}
		
		return wrapper;
	}

	public abstract boolean needsEnergy(EnumFacing side);

	public static class MekanismAcceptor extends EnergyAcceptorWrapper
	{
		private IStrictEnergyAcceptor acceptor;

		public MekanismAcceptor(IStrictEnergyAcceptor mekAcceptor)
		{
			acceptor = mekAcceptor;
		}

		@Override
		public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
		{
			return acceptor.acceptEnergy(side, amount, simulate);
		}

		@Override
		public boolean canReceiveEnergy(EnumFacing side)
		{
			return acceptor.canReceiveEnergy(side);
		}

		@Override
		public boolean needsEnergy(EnumFacing side)
		{
			return acceptor.acceptEnergy(side, 1, true) > 0;
		}
	}

	public static class RFAcceptor extends EnergyAcceptorWrapper
	{
		private IEnergyReceiver acceptor;

		public RFAcceptor(IEnergyReceiver rfAcceptor)
		{
			acceptor = rfAcceptor;
		}

		@Override
		public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
		{
			return fromRF(acceptor.receiveEnergy(side, Math.min(Integer.MAX_VALUE, toRF(amount)), simulate));
		}

		@Override
		public boolean canReceiveEnergy(EnumFacing side)
		{
			return acceptor.canConnectEnergy(side);
		}

		@Override
		public boolean needsEnergy(EnumFacing side)
		{
			return acceptor.receiveEnergy(side, 1, true) > 0;
		}

		public int toRF(double joules)
		{
			return (int)Math.round(joules*general.TO_RF);
		}

		public double fromRF(int rf)
		{
			return rf*general.FROM_RF;
		}
	}

	public static class IC2Acceptor extends EnergyAcceptorWrapper
	{
		private IEnergySink acceptor;

		public IC2Acceptor(IEnergySink ic2Acceptor)
		{
			acceptor = ic2Acceptor;
		}

		@Override
		public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
		{
			double toTransfer = Math.min(Math.min(acceptor.getDemandedEnergy(), toEU(amount)), Integer.MAX_VALUE);
			double rejects = acceptor.injectEnergy(side, toTransfer, 0);
			
			return fromEU(toTransfer - rejects);
		}

		@Override
		public boolean canReceiveEnergy(EnumFacing side)
		{
			return acceptor.acceptsEnergyFrom(null, side);
		}

		@Override
		public boolean needsEnergy(EnumFacing side)
		{
			return acceptor.getDemandedEnergy() > 0;
		}

		public double toEU(double joules)
		{
			return joules*general.TO_IC2;
		}
		
		public double fromEU(double eu)
		{
			return eu*general.FROM_IC2;
		}
	}
	
	public static class TeslaAcceptor extends EnergyAcceptorWrapper
	{
		private ITeslaConsumer acceptor;
		
		public TeslaAcceptor(ITeslaConsumer teslaConsumer)
		{
			acceptor = teslaConsumer;
		}
		
		@Override
		public double acceptEnergy(EnumFacing side, double amount, boolean simulate) 
		{
			return fromTesla(acceptor.givePower(toTesla(amount), false));
		}

		@Override
		public boolean canReceiveEnergy(EnumFacing side) 
		{
			return acceptor.givePower(1, true) > 0;
		}

		@Override
		public boolean needsEnergy(EnumFacing side)
		{
			return canReceiveEnergy(side);
		}
		
		public long toTesla(double joules)
		{
			return (long)Math.round(joules*general.TO_TESLA);
		}
		
		public double fromTesla(double tesla)
		{
			return tesla*general.FROM_TESLA;
		}
	}
	
	public static class ForgeAcceptor extends EnergyAcceptorWrapper
	{
		private IEnergyStorage acceptor;
		
		public ForgeAcceptor(IEnergyStorage forgeConsumer)
		{
			acceptor = forgeConsumer;
		}
		
		@Override
		public double acceptEnergy(EnumFacing side, double amount, boolean simulate)
		{
			return fromForge(acceptor.receiveEnergy(Math.min(Integer.MAX_VALUE, toForge(amount)), simulate));
		}

		@Override
		public boolean canReceiveEnergy(EnumFacing side) 
		{
			return acceptor.canReceive();
		}

		@Override
		public boolean needsEnergy(EnumFacing side) 
		{
			return acceptor.canReceive();
		}
		
		public int toForge(double joules)
		{
			return (int)Math.round(joules*general.TO_FORGE);
		}
		
		public double fromForge(double forge)
		{
			return forge*general.FROM_FORGE;
		}
	}
}
