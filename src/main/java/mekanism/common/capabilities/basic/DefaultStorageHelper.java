package mekanism.common.capabilities.basic;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Created by ben on 03/05/16.
 */
public class DefaultStorageHelper {

    private DefaultStorageHelper() {
    }

    public static class DefaultStorage<T> implements IStorage<T> {

        @Override
        public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
            if (instance instanceof INBTSerializable) {
                return ((INBTSerializable<?>) instance).serializeNBT();
            }
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
            if (instance instanceof INBTSerializable) {
                Class<? extends INBT> nbtClass = ((INBTSerializable<? extends INBT>) instance).serializeNBT().getClass();
                if (nbtClass.isInstance(nbt)) {
                    ((INBTSerializable) instance).deserializeNBT(nbtClass.cast(nbt));
                }
            }
        }
    }

    public static class NullStorage<T> implements IStorage<T> {

        @Override
        public INBT writeNBT(Capability<T> capability, T instance, Direction side) {
            return new CompoundNBT();
        }

        @Override
        public void readNBT(Capability<T> capability, T instance, Direction side, INBT nbt) {
        }
    }
}