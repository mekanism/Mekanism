package mekanism.common.integration.tesla;

import mekanism.common.base.IEnergyWrapper;
import mekanism.common.config.MekanismConfig;
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
		return (long)Math.round(tileEntity.getEnergy()* MekanismConfig.current().general.TO_TESLA.val());
	}

	@Override
	@Method(modid = MekanismHooks.TESLA_MOD_ID)
	public long getCapacity() 
	{
		return (long)Math.round(tileEntity.getMaxEnergy()* MekanismConfig.current().general.TO_TESLA.val());
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
		return (long)Math.round(rf* MekanismConfig.current().general.FROM_RF.val()* MekanismConfig.current().general.TO_TESLA.val());
	}
	
	public int teslaToRF(long tesla)
	{
		return (int)Math.round(Math.min(Integer.MAX_VALUE, tesla* MekanismConfig.current().general.FROM_TESLA.val()* MekanismConfig.current().general.TO_RF.val()));
	}
}
