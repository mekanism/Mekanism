package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.PartTransmitterIcons;
import mekanism.client.render.RenderPartTransmitter;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartLogisticalTransporter extends PartSidedPipe
{
	public static PartTransmitterIcons transporterIcons;

	@Override
	public String getType()
	{
		return "mekanism:logistical_transporter";
	}

	@Override
	public TransmitterType getTransmitter()
	{
		return TransmitterType.LOGISTICAL_TRANSPORTER;
	}

	public static void registerIcons(IconRegister register)
	{
		transporterIcons = new PartTransmitterIcons(3, 2);
		transporterIcons.registerCenterIcons(register, new String[] {"LogisticalTransporter", "RestrictiveTransporter", "DiversionTransporter"});
		transporterIcons.registerSideIcons(register, new String[] {"LogisticalTransporterSide", "RestrictiveTransporterSide"});
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		RenderPartTransmitter.getInstance().renderContents(this, pos);
	}

	@Override
	public Icon getCenterIcon()
	{
		return transporterIcons.getCenterIcon(0);
	}

	@Override
	public Icon getSideIcon()
	{
		return transporterIcons.getSideIcon(0);
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IInventory;
	}

	@Override
	public void onModeChange(ForgeDirection side) {}
}
