package mekanism.common.capabilities.resolver.manager;

import java.util.List;
import mekanism.api.annotations.MethodsAreNotNullByDefault;
import mekanism.common.capabilities.resolver.ICapabilityResolver;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

@MethodsAreNotNullByDefault
public interface ICapabilityHandlerManager<CONTAINER> extends ICapabilityResolver<@Nullable Direction> {

    /**
     * Gets the containers for a given side.
     *
     * @param side The side
     *
     * @return Containers on the given side
     */
    List<CONTAINER> getContainers(@Nullable Direction side);
}