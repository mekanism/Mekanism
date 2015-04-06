package mekanism.common.base;

import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.util.MekanismUtils;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyReceiver;
import ic2.api.energy.tile.IEnergySink;

public abstract class EnergyAcceptorWrapper implements IStrictEnergyAcceptor
{
	public Coord4D coord;

	public static EnergyAcceptorWrapper get(TileEntity tileEntity)
	{
		EnergyAcceptorWrapper wrapper = null;
		
		if(tileEntity instanceof IStrictEnergyAcceptor)
		{
			wrapper = new MekanismAcceptor((IStrictEnergyAcceptor)tileEntity);
		}
		else if(MekanismUtils.useRF() && tileEntity instanceof IEnergyReceiver)
		{
			wrapper = new RFAcceptor((IEnergyReceiver)tileEntity);
		}
		else if(MekanismUtils.useIC2() && tileEntity instanceof IEnergySink)
		{
			wrapper = new IC2Acceptor((IEnergySink)tileEntity);
		}
		
		if(wrapper != null)
		{
			wrapper.coord = Coord4D.get(tileEntity);
		}
		
		return wrapper;
	}

	public abstract double getNeeded();

	public static class MekanismAcceptor extends EnergyAcceptorWrapper
	{
		private IStrictEnergyAcceptor acceptor;

		public MekanismAcceptor(IStrictEnergyAcceptor mekAcceptor)
		{
			acceptor = mekAcceptor;
		}

		@Override
		public double transferEnergyToAcceptor(ForgeDirection side, double amount)
		{
			return acceptor.transferEnergyToAcceptor(side, amount);
		}

		@Override
		public boolean canReceiveEnergy(ForgeDirection side)
		{
			return acceptor.canReceiveEnergy(side);
		}

		@Override
		public double getEnergy()
		{
			return acceptor.getEnergy();
		}

		@Override
		public void setEnergy(double energy)
		{
			acceptor.setEnergy(energy);
		}

		@Override
		public double getMaxEnergy()
		{
			return acceptor.getMaxEnergy();
		}

		@Override
		public double getNeeded()
		{
			return acceptor.getMaxEnergy() - acceptor.getEnergy();
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
		public double transferEnergyToAcceptor(ForgeDirection side, double amount)
		{
			return fromRF(acceptor.receiveEnergy(side, toRF(amount), false));
		}

		@Override
		public boolean canReceiveEnergy(ForgeDirection side)
		{
			return acceptor.canConnectEnergy(side);
		}

		@Override
		public double getEnergy()
		{
			return fromRF(acceptor.getEnergyStored(ForgeDirection.UNKNOWN));
		}

		@Override
		public void setEnergy(double energy)
		{
			int rfToSet = toRF(energy);
			int amountToReceive = rfToSet - acceptor.getEnergyStored(ForgeDirection.UNKNOWN);
			acceptor.receiveEnergy(ForgeDirection.UNKNOWN, amountToReceive, false);
		}

		@Override
		public double getMaxEnergy()
		{
			return fromRF(acceptor.getMaxEnergyStored(ForgeDirection.UNKNOWN));
		}

		@Override
		public double getNeeded()
		{
			return fromRF(acceptor.getMaxEnergyStored(ForgeDirection.UNKNOWN) - acceptor.getEnergyStored(ForgeDirection.UNKNOWN));
		}

		public int toRF(double joules)
		{
			return (int)Math.round(joules * general.TO_TE);
		}

		public double fromRF(int rf)
		{
			return rf * general.FROM_TE;
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
		public double transferEnergyToAcceptor(ForgeDirection side, double amount)
		{
			return acceptor.injectEnergy(side, toEU(amount), 0);
		}

		@Override
		public boolean canReceiveEnergy(ForgeDirection side)
		{
			return acceptor.acceptsEnergyFrom(null, side);
		}

		@Override
		public double getEnergy()
		{
			return 0;
		}

		@Override
		public void setEnergy(double energy)
		{
			return;
		}

		@Override
		public double getMaxEnergy()
		{
			return 0;
		}

		@Override
		public double getNeeded()
		{
			return fromEU(acceptor.getDemandedEnergy());
		}

		public double toEU(double joules)
		{
			return joules * general.TO_IC2;
		}

		public double fromEU(double eu)
		{
			return eu * general.FROM_IC2;
		}
	}
}
