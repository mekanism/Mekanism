package mekanism.common.integration.tesla;

import mekanism.api.MekanismConfig.general;
import mekanism.common.tile.transmitter.TileEntityUniversalCable;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.Method;

@Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla")
public class TeslaCableIntegration implements ITeslaConsumer
{
	public TileEntityUniversalCable tileEntity;
	
	public EnumFacing side;
	
	public TeslaCableIntegration(TileEntityUniversalCable tile, EnumFacing facing)
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
