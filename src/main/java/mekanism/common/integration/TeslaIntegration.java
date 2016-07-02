package mekanism.common.integration;

import mekanism.api.MekanismConfig.general;
import mekanism.common.base.IEnergyWrapper;
import mekanism.common.tile.TileEntityElectricBlock;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional.Interface;
import net.minecraftforge.fml.common.Optional.InterfaceList;
import net.minecraftforge.fml.common.Optional.Method;

@InterfaceList({
	@Interface(iface = "net.darkhax.tesla.api.ITeslaConsumer", modid = "Tesla"),
	@Interface(iface = "net.darkhax.tesla.api.ITeslaProducer", modid = "Tesla"),
	@Interface(iface = "net.darkhax.tesla.api.ITeslaHolder", modid = "Tesla")
})
public class TeslaIntegration implements ITeslaHolder, ITeslaConsumer, ITeslaProducer
{
	public IEnergyWrapper tileEntity;
	
	public EnumFacing side;
	
	public TeslaIntegration(TileEntityElectricBlock tile, EnumFacing facing)
	{
		tileEntity = tile;
		side = facing;
	}

	@Override
	@Method(modid = "Tesla")
	public long getStoredPower() 
	{
		return (long)Math.round(tileEntity.getEnergy()*general.TO_TESLA);
	}

	@Override
	@Method(modid = "Tesla")
	public long getCapacity() 
	{
		return (long)Math.round(tileEntity.getMaxEnergy()*general.TO_TESLA);
	}
	
	@Override
	@Method(modid = "Tesla")
	public long takePower(long power, boolean simulated) 
	{
		return (long)Math.round(tileEntity.removeEnergyFromProvider(side, power*general.FROM_TESLA)*general.TO_TESLA);
	}
	
	@Override
	@Method(modid = "Tesla")
	public long givePower(long power, boolean simulated) 
	{
		return (long)Math.round(tileEntity.transferEnergyToAcceptor(side, power*general.FROM_TESLA)*general.TO_TESLA);
	}
}
