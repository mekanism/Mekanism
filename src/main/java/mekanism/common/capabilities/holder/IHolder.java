package mekanism.common.capabilities.holder;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IHolder {

    default boolean canInsert(@Nullable Direction direction) {
        return true;
    }

    default boolean canExtract(@Nullable Direction direction) {
        return true;
    }
}