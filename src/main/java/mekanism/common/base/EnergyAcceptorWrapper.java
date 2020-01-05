package mekanism.common.base;

import ic2.api.energy.tile.IEnergySink;
import mekanism.api.Coord4D;
import mekanism.api.MekanismConfig.general;
import mekanism.api.energy.IStrictEnergyAcceptor;
import mekanism.common.util.MekanismUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cofh.api.energy.IEnergyReceiver;

public abstract class EnergyAcceptorWrapper implements IStrictEnergyAcceptor
{
	public Coord4D coord;

	public static EnergyAcceptorWrapper get(TileEntity tileEntity)
	{
		if(tileEntity != null && tileEntity.getWorldObj() == null)
		{
			return null;
		}
		
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

	public abstract boolean needsEnergy(ForgeDirection side);

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
		public boolean needsEnergy(ForgeDirection side)
		{
			return acceptor.getMaxEnergy() - acceptor.getEnergy() > 0;
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
			int transferred = acceptor.receiveEnergy(side, Math.min(Integer.MAX_VALUE, toRF(amount)), false);
			
			return fromRF(transferred);
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
		public boolean needsEnergy(ForgeDirection side)
		{
			return acceptor.receiveEnergy(side, 1, true) > 0 || getEnergyNeeded(side) > 0;
		}

		public int toRF(double joules)
		{
			return (int)Math.round(joules * general.TO_TE);
		}

		public double fromRF(int rf)
		{
			return rf * general.FROM_TE;
		}
		
		public int getEnergyNeeded(ForgeDirection side)
		{
			return acceptor.getMaxEnergyStored(side) - acceptor.getEnergyStored(side);
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
			double toTransfer = Math.min(Math.min(acceptor.getDemandedEnergy(), toEU(amount)), Integer.MAX_VALUE);
			double rejects = acceptor.injectEnergy(side, toTransfer, 0);
			
			return fromEU(toTransfer - rejects);
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
		public boolean needsEnergy(ForgeDirection side)
		{
			return acceptor.getDemandedEnergy() > 0;
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
