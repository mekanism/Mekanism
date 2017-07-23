package mekanism.common.integration.tesla;

import mekanism.common.base.IEnergyWrapper;
import mekanism.common.config.MekanismConfig.general;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
	@Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "tesla"),
	@Interface(iface = "net.darkhax.tesla.api.ITeslaProducer", modid = "tesla"),
	@Interface(iface = "net.darkhax.tesla.api.ITeslaHolder", modid = "tesla")
})
public class TeslaIntegration implements ITeslaHolder, ITeslaConsumer, ITeslaProducer
{
	public IEnergyWrapper tileEntity;
	
	public EnumFacing side;
	
	public TeslaIntegration(IEnergyWrapper tile, EnumFacing facing)
	{
		tileEntity = tile;
		side = facing;
	}

	@Override
	@Method(modid = "tesla")
	public long getStoredPower() 
	{
		return (long)Math.round(tileEntity.getEnergy()*general.TO_TESLA);
	}

	@Override
	@Method(modid = "tesla")
	public long getCapacity() 
	{
		return (long)Math.round(tileEntity.getMaxEnergy()*general.TO_TESLA);
	}
	
	@Override
	@Method(modid = "tesla")
	public long takePower(long power, boolean simulated) 
	{
		return rfToTesla(tileEntity.extractEnergy(side, teslaToRF(power), simulated));
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
