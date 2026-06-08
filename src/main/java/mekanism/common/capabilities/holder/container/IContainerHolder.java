package mekanism.common.capabilities.holder.container;

import java.util.List;
import mekanism.common.capabilities.holder.IHolder;
import net.minecraft.core.Direction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface IContainerHolder<CONTAINER> extends IHolder {

    @NonNull
    List<CONTAINER> getContainers(@Nullable Direction side);
}