package mekanism.common.multipart;

import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.PartTransmitterIcons;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;

public class PartLogisticalTransporter extends PartSidedPipe
{
	public static PartTransmitterIcons transporterIcons;

	public String getType()
	{
		return "mekanism:logistical_transporter";
	}

	public TransmissionType getTransmissionType()
	{
		return TransmissionType.ITEM;
	}

	public static void registerIcons(IconRegister register)
	{
		transporterIcons = new PartTransmitterIcons(1);
		transporterIcons.registerCenterIcons(register, new String[] {"LogisticalTransporter", "RestrictionTransporter", "DiversionTransporter"});
		transporterIcons.registerSideIcon(register, "LogisticalTransporterSide");
	}

	public Icon getCenterIcon()
	{
		return transporterIcons.getCenterIcon(0);
	}

	public Icon getSideIcon()
	{
		return transporterIcons.getSideIcon();
	}

	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IInventory;
	}

	@Override
	public void onModeChange(ForgeDirection side) {}
}
