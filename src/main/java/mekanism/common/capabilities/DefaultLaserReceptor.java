package mekanism.common.capabilities;

import mekanism.api.lasers.ILaserReceptor;
import mekanism.common.capabilities.DefaultStorageHelper.NullStorage;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultLaserReceptor implements ILaserReceptor {

    public static void register() {
        CapabilityManager.INSTANCE.register(ILaserReceptor.class, new NullStorage<>(), DefaultLaserReceptor::new);
    }

    @Override
    public void receiveLaserEnergy(double energy, Direction side) {
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}