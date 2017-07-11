package mekanism.common.integration.tesla;

import mekanism.common.multipart.PartUniversalCable;
import mekanism.common.config.MekanismConfig.general;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla")
public class TeslaCableIntegration implements ITeslaConsumer
{
	public PartUniversalCable tileEntity;
	
	public EnumFacing side;
	
	public TeslaCableIntegration(PartUniversalCable tile, EnumFacing facing)
	{
		tileEntity = tile;
		side = facing;
	}
	
	@Override
	@Method(modid = "tesla")
	public long givePower(long power, boolean simulated) 
	{
		return (long)Math.round(tileEntity.acceptEnergy(side, power*general.FROM_TESLA, simulated)*general.TO_TESLA);
	}
}
