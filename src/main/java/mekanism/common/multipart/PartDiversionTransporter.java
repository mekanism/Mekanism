package mekanism.common.multipart;

import java.util.ArrayList;

import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Range4D;
import mekanism.common.Mekanism;
import mekanism.common.Tier.TransporterTier;
import mekanism.common.content.transporter.TransporterStack;
import mekanism.common.network.PacketTileEntity.TileEntityMessage;
import mekanism.common.util.MekanismUtils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import io.netty.buffer.ByteBuf;

public class PartDiversionTransporter extends PartLogisticalTransporter
{
	public int[] modes = {0, 0, 0, 0, 0, 0};

	@Override
	public String getType()
	{
		return "mekanism:diversion_transporter";
	}

	@Override
	public TransmitterType getTransmitterType()
	{
		return TransmitterType.DIVERSION_TRANSPORTER;
	}

	@Override
	public IIcon getCenterIcon(boolean opaque)
	{
		return transporterIcons.getCenterIcon(5);
	}
	
	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return transporterIcons.getSideIcon(opaque ? 14 : (getTransmitter().color != null ? 11 : 10));
	}
	
	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return transporterIcons.getSideIcon(opaque ? 15 : (getTransmitter().color != null ? 13 : 12));
	}
	
	@Override
	public boolean renderCenter()
	{
		return true;
	}

	@Override
	public void load(NBTTagCompound nbtTags)
	{
		super.load(nbtTags);

		modes = nbtTags.getIntArray("modes");
	}

	@Override
	public void save(NBTTagCompound nbtTags)
	{
		super.save(nbtTags);

		nbtTags.setIntArray("modes", modes);
	}

	@Override
	public void handlePacketData(ByteBuf dataStream) throws Exception
	{
		super.handlePacketData(dataStream);
		
		modes[0] = dataStream.readInt();
		modes[1] = dataStream.readInt();
		modes[2] = dataStream.readInt();
		modes[3] = dataStream.readInt();
		modes[4] = dataStream.readInt();
		modes[5] = dataStream.readInt();
	}

	@Override
	public ArrayList getNetworkedData(ArrayList data)
	{
		data = super.getNetworkedData(data);

		data.add(modes[0]);
		data.add(modes[1]);
		data.add(modes[2]);
		data.add(modes[3]);
		data.add(modes[4]);
		data.add(modes[5]);

		return data;
	}

	@Override
	public ArrayList getSyncPacket(TransporterStack stack, boolean kill)
	{
		ArrayList data = super.getSyncPacket(stack, kill);

		data.add(modes[0]);
		data.add(modes[1]);
		data.add(modes[2]);
		data.add(modes[3]);
		data.add(modes[4]);
		data.add(modes[5]);

		return data;
	}

	@Override
	protected boolean onConfigure(EntityPlayer player, int part, int side)
	{
		int newMode = (modes[side] + 1) % 3;
		String description = "ERROR";

		modes[side] = newMode;

		switch(newMode)
		{
			case 0:
				description = MekanismUtils.localize("control.disabled.desc");
				break;
			case 1:
				description = MekanismUtils.localize("control.high.desc");
				break;
			case 2:
				description = MekanismUtils.localize("control.low.desc");
				break;
		}

		refreshConnections();
		tile().notifyPartChange(this);
		notifyTileChange();
		player.addChatMessage(new ChatComponentText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.toggleDiverter") + ": " + EnumColor.RED + description));
		Mekanism.packetHandler.sendToReceivers(new TileEntityMessage(Coord4D.get(tile()), getNetworkedData(new ArrayList())), new Range4D(Coord4D.get(tile())));

		return true;
	}

	@Override
	public boolean canConnect(ForgeDirection side)
	{
		if(!super.canConnect(side))
		{
			return false;
		}

		int mode = modes[side.ordinal()];
		boolean redstone = world().isBlockIndirectlyGettingPowered(x(), y(), z());

		if((mode == 2 && redstone == true) || (mode == 1 && redstone == false))
		{
			return false;
		}

		return true;
	}
}
