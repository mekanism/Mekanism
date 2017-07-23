package mekanism.common.capabilities;

import mekanism.api.energy.IStrictEnergyOutputter;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.CapabilityManager;

/**
 * Created by ben on 30/04/16.
 */
public class DefaultCableOutputter implements IStrictEnergyOutputter
{
	@Override
    public double pullEnergy(EnumFacing side, double amount, boolean simulate)
    {
    	return 0;
    }
	
    @Override
    public boolean canOutputEnergy(EnumFacing side)
    {
        return true;
    }
    
    public static void register()
    {
        CapabilityManager.INSTANCE.register(IStrictEnergyOutputter.class, new NullStorage<>(), DefaultCableOutputter.class);
        //empty backwards compat
        DeprecatedCableOutputter.register();
    }

    @SuppressWarnings("deprecation")
    public static class DeprecatedCableOutputter implements mekanism.api.energy.ICableOutputter{

	    private static void register(){
            CapabilityManager.INSTANCE.register(mekanism.api.energy.ICableOutputter.class, new NullStorage<>(), DeprecatedCableOutputter.class );
        }

        @Override
        public boolean canOutputTo(EnumFacing side)
        {
            return false;
        }
    }
}
