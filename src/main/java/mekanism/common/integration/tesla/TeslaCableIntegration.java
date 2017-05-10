package mekanism.common.integration.tesla;

import mekanism.api.MekanismConfig.general;
import mekanism.common.multipart.PartUniversalCable;
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
		return rfToTesla(tileEntity.receiveEnergy(side, teslaToRF(power), simulated));
	}
	
	public long rfToTesla(int rf)
	{
		return (long)Math.round(rf*general.FROM_RF*general.TO_TESLA);
	}
	
	public int teslaToRF(long tesla)
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, tesla*general.FROM_TESLA*general.TO_RF));
	}
}
