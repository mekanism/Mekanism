package mekanism.common.multipart;

import java.util.Set;

import mekanism.api.gas.GasNetwork;
import mekanism.api.gas.IGasHandler;
import mekanism.api.transmitters.ITransmitter;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.PartTransmitterIcons;
import mekanism.client.render.RenderPartTransmitter;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartPressurizedTube extends PartTransmitter<GasNetwork>
{
    public static PartTransmitterIcons tubeIcons;

	@Override
	public String getType()
	{
		return "mekanism:pressurized_tube";
	}

    public static void registerIcons(IconRegister register)
    {
        tubeIcons = new PartTransmitterIcons(1);
        tubeIcons.registerCenterIcons(register, new String[]{"PressurizedTube"});
        tubeIcons.registerSideIcon(register, "TransmitterSideSmall");
    }

    @Override
    public Icon getCenterIcon()
    {
        return tubeIcons.getCenterIcon(0);
    }

    @Override
    public Icon getSideIcon()
    {
        return tubeIcons.getSideIcon();
    }

    @Override
	public TransmissionType getTransmissionType()
	{
		return TransmissionType.GAS;
	}

	@Override
	public boolean isValidAcceptor(TileEntity tile, ForgeDirection side)
	{
		return tile instanceof IGasHandler;
	}

	@Override
	public GasNetwork createNetworkFromSingleTransmitter(ITransmitter<GasNetwork> transmitter)
	{
		return new GasNetwork(transmitter);
	}

	@Override
	public GasNetwork createNetworkByMergingSet(Set<GasNetwork> networks)
	{
		return new GasNetwork(networks);
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
		return getTransmitterNetwork().getNeeded();
	}
	
	@Override
	public String getTransmitterNetworkFlow()
	{
		return getTransmitterNetwork().getFlow();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		RenderPartTransmitter.getInstance().renderContents(this, pos);
	}
}
