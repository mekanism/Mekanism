package mekanism.common.multipart;

import java.util.Collection;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
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

public class PartHeatTransmitter extends PartTransmitter<IHeatTransfer, HeatNetwork>
{
	public static TransmitterIcons heatIcons = new TransmitterIcons(1, 2);

	public PartHeatTransmitter()
	{
		transmitterDelegate = new MultipartHeatTransmitter(this);
	}

	@Override
	public HeatNetwork createNewNetwork()
	{
		return new HeatNetwork();
	}

	@Override
	public HeatNetwork createNetworkByMerging(Collection networks)
	{
		return new HeatNetwork(networks);
	}

	@Override
	public int getCapacity()
	{
		return 0;
	}

	@Override
	public Object getBuffer()
	{
		return null;
	}

	@Override
	public void takeShare() {}

	public static void registerIcons(IIconRegister register)
	{
		heatIcons.registerCenterIcons(register, new String[] {"HeatTransmitter"});
		heatIcons.registerSideIcons(register, new String[] {"SmallTransmitterVertical", "SmallTransmitterHorizontal"});
	}

	@Override
	public IIcon getCenterIcon(boolean opaque)
	{
		return heatIcons.getCenterIcon(0);
	}

	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return heatIcons.getSideIcon(0);
	}

	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return heatIcons.getSideIcon(1);
	}

	@Override
	public TransmitterType getTransmitterType()
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
		getTransmitter().temperature += 10000;
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

		getTransmitter().temperature = nbtTags.getDouble("temperature");
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		nbtTags.setDouble("temperature", getTransmitter().temperature);
	}

	public void sendTemp()
	{
		MCDataOutput packet = getWriteStream();
		packet.writeBoolean(true);
		packet.writeDouble(getTransmitter().temperature);
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
			getTransmitter().temperature = packet.readDouble();
		}
		else {
			super.readDesc(packet);
		}
	}

	public ColourRGBA getBaseColour()
	{
		return getTransmitter().material.baseColour;
	}

	public MultipartHeatTransmitter getTransmitter()
	{
		return (MultipartHeatTransmitter)transmitterDelegate;
	}
}
