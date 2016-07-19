package mekanism.common.multipart;

import java.util.Collection;

import mekanism.api.Coord4D;
import mekanism.api.IHeatTransfer;
import mekanism.api.MekanismConfig.client;
import mekanism.api.transmitters.TransmissionType;
import mekanism.client.render.RenderPartTransmitter;
import mekanism.common.HeatNetwork;
import mekanism.common.Tier;
import mekanism.common.Tier.BaseTier;
import mekanism.common.Tier.ConductorTier;
import mekanism.common.util.HeatUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import codechicken.lib.colour.ColourRGBA;
import codechicken.lib.data.MCDataInput;
import codechicken.lib.data.MCDataOutput;
import codechicken.lib.vec.Vector3;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PartThermodynamicConductor extends PartTransmitter<IHeatTransfer, HeatNetwork> implements IHeatTransfer
{
	public Tier.ConductorTier tier;
	
	public static TransmitterIcons conductorIcons = new TransmitterIcons(4, 8);

	public double temperature = 0;
	public double clientTemperature = 0;
	public double heatToAbsorb = 0;

	public PartThermodynamicConductor(Tier.ConductorTier conductorTier)
	{
		super();
		tier = conductorTier;
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

    @Override
    public void updateShare() {}

	public static void registerIcons(IIconRegister register)
	{
        conductorIcons.registerCenterIcons(register, new String[]{"ThermodynamicConductorBasic", "ThermodynamicConductorAdvanced",
            "ThermodynamicConductorElite", "ThermodynamicConductorUltimate"});
        conductorIcons.registerSideIcons(register, new String[]{"ThermodynamicConductorVerticalBasic", "ThermodynamicConductorVerticalAdvanced", "ThermodynamicConductorVerticalElite", "ThermodynamicConductorVerticalUltimate",
            "ThermodynamicConductorHorizontalBasic", "ThermodynamicConductorHorizontalAdvanced", "ThermodynamicConductorHorizontalElite", "ThermodynamicConductorHorizontalUltimate"});
	}

	@Override
	public IIcon getCenterIcon(boolean opaque)
	{
		return conductorIcons.getCenterIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIcon(boolean opaque)
	{
		return conductorIcons.getSideIcon(tier.ordinal());
	}

	@Override
	public IIcon getSideIconRotated(boolean opaque)
	{
		return conductorIcons.getSideIcon(4+tier.ordinal());
	}

	@Override
	public TransmitterType getTransmitterType()
	{
		return tier.type;
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
		return "mekanism:thermodynamic_conductor_" + tier.name().toLowerCase();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderDynamic(Vector3 pos, float f, int pass)
	{
		if(pass == 0 && !client.opaqueTransmitters)
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
		
		packet.writeInt(tier.ordinal());
	}

	@Override
	public void readDesc(MCDataInput packet)
	{
		if(packet.readBoolean())
		{
			temperature = packet.readDouble();
		}
		else {
			super.readDesc(packet);
			
			tier = ConductorTier.values()[packet.readInt()];
		}
	}

	public ColourRGBA getBaseColour()
	{
		return tier.baseColour;
	}

	@Override
	public double getTemp()
	{
		return temperature;
	}

	@Override
	public double getInverseConductionCoefficient()
	{
		return tier.inverseConduction;
	}

	@Override
	public double getInsulationCoefficient(ForgeDirection side)
	{
		return tier.inverseConductionInsulation;
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
		temperature += tier.inverseHeatCapacity * heatToAbsorb;
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
	
	@Override
	public boolean upgrade(int tierOrdinal)
	{
		if(tier.ordinal() < BaseTier.ULTIMATE.ordinal() && tierOrdinal == tier.ordinal()+1)
		{
			tier = ConductorTier.values()[tier.ordinal()+1];
			
			markDirtyTransmitters();
			sendDesc = true;
			
			return true;
		}
		
		return false;
	}
}
