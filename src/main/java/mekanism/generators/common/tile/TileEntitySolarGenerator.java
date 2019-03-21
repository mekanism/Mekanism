package mekanism.generators.common.tile;

import io.netty.buffer.ByteBuf;

import mekanism.api.TileNetworkList;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ChargeUtils;
import mekanism.common.util.MekanismUtils;
import micdoodle8.mods.galacticraft.api.world.ISolarLevel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.biome.BiomeDesert;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySolarGenerator extends TileEntityGenerator
{
	/** Whether or not this generator sees the sun. */
	public boolean seesSun = false;

	/** How fast this tile entity generates energy. */
	public double GENERATION_RATE;

	public TileEntitySolarGenerator()
	{
		this("SolarGenerator", 96000, MekanismConfig.current().generators.solarGeneration.val()*2);
		GENERATION_RATE = MekanismConfig.current().generators.solarGeneration.val();
	}

	public TileEntitySolarGenerator(String name, double maxEnergy, double output)
	{
		super("solar", name, maxEnergy, output);
		inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side)
	{
		return new int[] {0};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getVolume()
	{
		return 0.05F*super.getVolume();
	}

	@Override
	public boolean canSetFacing(int facing)
	{
		return facing != 0 && facing != 1;
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!world.isRemote)
		{
			ChargeUtils.charge(0, this);
			
			if(world.isDaytime() && ((!world.isRaining() && !world.isThundering()) || isDesert()) && !world.provider.isNether() && world.canSeeSky(getPos().add(0, 4, 0))) // TODO Check isNether call, maybe it should be hasSkyLight
			{
				seesSun = true;
			}
			else {
				seesSun = false;
			}

			if(canOperate())
			{
				setActive(true);
				setEnergy(getEnergy() + getProduction());
			}
			else {
				setActive(false);
			}
		}
	}

	public boolean isDesert()
	{
		return world.provider.getBiomeForCoords(getPos()).getBiomeClass() == BiomeDesert.class;
	}

	@Override
	public boolean canExtractItem(int slotID, ItemStack itemstack, EnumFacing side)
	{
		if(slotID == 0)
		{
			return ChargeUtils.canBeOutputted(itemstack, true);
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slotID, ItemStack itemstack)
	{
		if(slotID == 0)
		{
			return ChargeUtils.canBeCharged(itemstack);
		}

		return true;
	}

	@Override
	public boolean canOperate()
	{
		return getEnergy() < getMaxEnergy() && seesSun && MekanismUtils.canFunction(this);
	}

	public double getProduction()
	{
		if(seesSun)
		{
			double ret = GENERATION_RATE;

			if(MekanismUtils.existsAndInstance(world.provider, "micdoodle8.mods.galacticraft.api.world.ISolarLevel"))
			{
				ret *= ((ISolarLevel)world.provider).getSolarEnergyMultiplier();
			}

			if(isDesert())
			{
				ret *= 1.5;
			}

			return ret;
		}

		return 0;
	}

    private static final String[] methods = new String[] {"getEnergy", "getOutput", "getMaxEnergy", "getEnergyNeeded", "getSeesSun"};

	@Override
	public String[] getMethods()
	{
		return methods;
	}

	@Override
	public Object[] invoke(int method, Object[] arguments) throws Exception
	{
		switch(method)
		{
			case 0:
				return new Object[] {electricityStored};
			case 1:
				return new Object[] {output};
			case 2:
				return new Object[] {BASE_MAX_ENERGY};
			case 3:
				return new Object[] {(BASE_MAX_ENERGY -electricityStored)};
			case 4:
				return new Object[] {seesSun};
			default:
				throw new NoSuchMethodException();
		}
	}

	@Override
	public void handlePacketData(ByteBuf dataStream)
	{
		super.handlePacketData(dataStream);
		
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
		{
			seesSun = dataStream.readBoolean();
		}
	}

	@Override
	public TileNetworkList getNetworkedData(TileNetworkList data)
	{
		super.getNetworkedData(data);
		data.add(seesSun);
		return data;
	}

	@Override
	public boolean sideIsOutput(EnumFacing side)
	{
		return side == EnumFacing.DOWN;
	}

	@Override
	public boolean renderUpdate()
	{
		return false;
	}

	@Override
	public boolean lightUpdate()
	{
		return false;
	}
}
