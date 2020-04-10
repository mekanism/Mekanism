package mekanism.common.capabilities.basic;

import javax.annotation.Nonnull;
import mekanism.api.lasers.ILaserReceptor;
import mekanism.api.math.FloatingLong;
import mekanism.common.capabilities.basic.DefaultStorageHelper.NullStorage;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class DefaultLaserReceptor implements ILaserReceptor {

    public static void register() {
        CapabilityManager.INSTANCE.register(ILaserReceptor.class, new NullStorage<>(), DefaultLaserReceptor::new);
    }

    @Override
    public void receiveLaserEnergy(@Nonnull FloatingLong energy, Direction side) {
    }

    @Override
    public boolean canLasersDig() {
        return false;
    }
}