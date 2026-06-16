package mekanism.common.capabilities.holder.single;

import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ISingleContainerHolder<CONTAINER> extends IHolder {

    @Nullable
    CONTAINER getContainer(@Nullable Direction side);
}