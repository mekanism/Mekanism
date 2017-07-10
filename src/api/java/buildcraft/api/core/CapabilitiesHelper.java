package buildcraft.api.core;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilitiesHelper {
    public static <T> void registerCapability(Class<T> clazz) {
        CapabilityManager.INSTANCE.register(clazz, new VoidStorage<>(), () -> {
            throw new IllegalStateException("You must create your own instances!");
        });
    }

    @Nonnull
    public static <T> Capability<T> ensureRegistration(Capability<T> cap, Class<T> clazz) {
        if (cap == null) {
            throw new Error("Capability registration failed for " + clazz);
        }
        return cap;
    }

    public static class VoidStorage<T> implements Capability.IStorage<T> {
        @Override
        public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
            throw new IllegalStateException("You must create your own instances!");
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
            throw new IllegalStateException("You must create your own instances!");
        }
    }
}
