package mekanism.common.multipart;

import java.util.Set;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.transmitters.IGridTransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.HeatNetwork;
import mekanism.common.util.HeatUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Vector3;

public class PartHeatTransmitter extends PartTransmitter<HeatNetwork> implements IHeatTransfer
{
	public double temperature = 0;
	public double clientTemperature = 0;

	public double inversek = 5;

	public double insulationInversek = 0;

	public double inverseHeatCapacity = 1;

	public ColourRGBA baseColour = new ColourRGBA(0.2, 0.2, 0.2, 1);

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
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return insulationInversek;
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
		temperature += inverseHeatCapacity * heatToAbsorb;
		heatToAbsorb = 0;
		if(Math.abs(temperature - clientTemperature) > (temperature / 100))
		{
			clientTemperature = temperature;
			sendTemp();
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
		if(connectionMapContainsSide(getAllCurrentConnections(), side))
		{
			TileEntity adj = Coord4D.get(tile()).getFromSide(side).getTileEntity(world());
			if(adj instanceof IHeatTransfer)
			{
				return (IHeatTransfer)adj;
			}
		}
		return null;
	}

	public static void registerIcons(IIconRegister register)
	{
		heatIcons.registerCenterIcons(register, new String[] {"HeatTransmitter"});
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
		return tile instanceof IHeatTransfer && ((IHeatTransfer)tile).canConnectHeat(side.getOpposite());
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
		temperature += 10000;
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		if(pass == 0)
		{
			RenderPartTransmitter.getInstance().renderContents(this, pos);
		}
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		temperature = nbtTags.getDouble("temperature");
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		nbtTags.setDouble("temperature", temperature);
	}

	public void sendTemp()
	{
		MCDataOutput packet = getWriteStream();
		packet.writeBoolean(true);
		packet.writeDouble(temperature);
	}

	@Override
	public void writeDesc(MCDataOutput packet)
	{
		packet.writeBoolean(false);
		super.writeDesc(packet);
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		if(packet.readBoolean())
		{
			temperature = packet.readDouble();
		}
		else
		{
			super.readDesc(packet);
		}
	}

	public ColourRGBA getBaseColour()
	{
		return baseColour;
	}

}
