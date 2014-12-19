package mekanism.common.multipart;

import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.common.HeatNetwork;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class PartHeatTransmitter extends PartTransmitter<HeatNetwork> implements IHeatTransfer
{
	public double temperature = 0;

	public double inversek = 10;

	public double inverseHeatCapacity = 1;

	public double heatToAbsorb = 0;

	public static TransmitterIcons heatIcons = new TransmitterIcons(1, 2);

	@Override
	public HeatNetwork createNetworkFromSingleTransmitter(IGridTransmitter<HeatNetwork> transmitter)
	{
		return new HeatNetwork(transmitter);
	}

	@Override
	public HeatNetwork createNetworkByMergingSet(Set<HeatNetwork> networks)
	{
		return new HeatNetwork(networks);
	}

	@Override
	public int getTransmitterNetworkSize()
	{
		return getTransmitterNetwork().getSize();
	}

	@Override
	public int getTransmitterNetworkAcceptorSize()
	{
		return getTransmitterNetwork().getAcceptorSize();
	}

	@Override
	public String getTransmitterNetworkNeeded()
	{
		return getTransmitterNetwork().getNeededInfo();
	}

	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlowInfo();
	}

	@Override
	public int getCapacity()
	{
		return 0;
	}

	@Override
	public double getTemp()
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return inversek;
	}

	@Override
	public void transferHeatTo(double heat)
	{
		heatToAbsorb += heat;
	}

	@Override
	public double[] simulateHeat()
	{
		double heatTransferred[] = new double[]{0,0};
		Coord4D pos = Coord4D.get(tile());
		for(ForgeDirection side : ForgeDirection.VALID_DIRECTIONS)
		{
			if(connectionMapContainsSide(getAllCurrentConnections(), side))
			{
				TileEntity tileEntity = pos.getFromSide(side).getTileEntity(world());
				if(tileEntity instanceof IHeatTransfer)
				{
					IHeatTransfer sink = (IHeatTransfer)tileEntity;
					double invConduction = sink.getInverseConductionCoefficient() + getInverseConductionCoefficient();
					double heatToTransfer = getTemp() / invConduction;
					transferHeatTo(-heatToTransfer);
					sink.transferHeatTo(heatToTransfer);
					if(!(sink instanceof IGridTransmitter))
						heatTransferred[0] += heatToTransfer;
					continue;
				}
			}
			//Transfer to air otherwise
			double heatToTransfer = getTemp() / (10000+getInverseConductionCoefficient());
			transferHeatTo(-heatToTransfer);
			heatTransferred[1] += heatToTransfer;
		}
		return heatTransferred;
	}

	@Override
	public double applyTemperatureChange()
	{
		temperature += inverseHeatCapacity * heatToAbsorb;
		heatToAbsorb = 0;
		return temperature;
	}

	@Override
	public boolean isInsulated(ForgeDirection side)
	{
		return false;
	}

	public static void registerIcons(IIconRegister register)
	{
		heatIcons.registerCenterIcons(register, new String[] {"UniversalCableBasic"});
		heatIcons.registerSideIcons(register, new String[] {"SmallTransmitterVertical", "SmallTransmitterHorizontal"});
	}

	@Override
	public IIcon getCenterIcon()
	{
		return heatIcons.getCenterIcon(0);
	}

	@Override
	public IIcon getSideIcon()
	{
		return heatIcons.getSideIcon(0);
	}

	@Override
	public IIcon getSideIconRotated()
	{
		return heatIcons.getSideIcon(1);
	}

	@Override
	public TransmitterType getTransmitter()
	{
		return TransmitterType.HEAT_TRANSMITTER;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IHeatTransfer && !((IHeatTransfer)tile).isInsulated(side.getOpposite());
	}

	@Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.HEAT;
	}

	@Override
	public String getType()
	{
		return "mekanism:heat_transmitter";
	}

	@Override
	protected boolean onConfigure(EntityPlayer player, int part, int side)
	{
		temperature += 100;
		return true;
	}
}
