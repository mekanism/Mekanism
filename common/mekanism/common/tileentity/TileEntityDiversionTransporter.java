package mekanism.common.tileentity;

import java.util.ArrayList;

import mekanism.api.EnumColor;
import mekanism.api.Coord4D;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.transporter.TransporterStack;
import mekanism.common.util.MekanismUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatMessageComponent;

import com.google.common.io.ByteArrayDataInput;

public class TileEntityDiversionTransporter extends TileEntityLogisticalTransporter
{
	public int[] modes = {0, 0, 0, 0, 0, 0};
	
	@Override
	public void readFromNBT(NBTTagCompound nbtTags)
	{
		super.readFromNBT(nbtTags);
		
		modes = nbtTags.getIntArray("modes");
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbtTags)
	{
		super.writeToNBT(nbtTags);
		
		nbtTags.setIntArray("modes", modes);
	}
	
	@Override
	public void handlePacketData(ByteArrayDataInput dataStream)
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
	public boolean onSneakRightClick(EntityPlayer player, int side)
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
		
		player.sendChatToPlayer(ChatMessageComponent.createFromText(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " " + MekanismUtils.localize("tooltip.configurator.toggleDiverter") + ": " + EnumColor.RED + description));
		PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity().setParams(Coord4D.get(this), getNetworkedData(new ArrayList())), Coord4D.get(this), 50D);
		
		return true;
	}
}
