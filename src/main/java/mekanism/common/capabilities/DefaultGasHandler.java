package mekanism.common.capabilities;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;

public class DefaultGasHandler implements IGasHandler
{
	@Override
	public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer)
	{
		return 0;
	}

	@Override
	public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) 
	{
		return null;
	}

	@Override
	public boolean canReceiveGas(EnumFacing side, Gas type) 
	{
		return false;
	}

	@Override
	public boolean canDrawGas(EnumFacing side, Gas type) 
	{
		return false;
	}
	
	public static void register()
	{
        CapabilityManager.INSTANCE.register(IGasHandler.class, new NullStorage<>(), DefaultGasHandler::new);
	}

	@Nonnull
	@Override
	public GasTankInfo[] getTankInfo()
	{
		return IGasHandler.NONE;
	}
}
