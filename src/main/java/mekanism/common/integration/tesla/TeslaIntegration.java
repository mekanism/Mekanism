package mekanism.common.integration.tesla;

import mekanism.common.base.IEnergyWrapper;
import mekanism.common.config.MekanismConfig.general;
import mekanism.common.integration.MekanismHooks;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
	@Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = MekanismHooks.TESLA_MOD_ID),
	@Interface(iface = "net.darkhax.tesla.api.ITeslaProducer", modid = MekanismHooks.TESLA_MOD_ID),
	@Interface(iface = "net.darkhax.tesla.api.ITeslaHolder", modid = MekanismHooks.TESLA_MOD_ID)
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
	@Method(modid = MekanismHooks.TESLA_MOD_ID)
	public long getStoredPower() 
	{
		return Math.round(tileEntity.getEnergy()*general.TO_TESLA);
	}

	@Override
	@Method(modid = MekanismHooks.TESLA_MOD_ID)
	public long getCapacity() 
	{
		return Math.round(tileEntity.getMaxEnergy()*general.TO_TESLA);
	}
	
	@Override
	@Method(modid = MekanismHooks.TESLA_MOD_ID)
	public long takePower(long power, boolean simulated) 
	{
		return rfToTesla(tileEntity.extractEnergy(side, teslaToRF(power), simulated));
	}
	
	@Override
	@Method(modid = MekanismHooks.TESLA_MOD_ID)
	public long givePower(long power, boolean simulated) 
	{
		return rfToTesla(tileEntity.receiveEnergy(side, teslaToRF(power), simulated));
	}
	
	public long rfToTesla(int rf)
	{
		return Math.round(rf*general.FROM_RF*general.TO_TESLA);
	}
	
	public int teslaToRF(long tesla)
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, tesla*general.FROM_TESLA*general.TO_RF));
	}
}
