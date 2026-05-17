package mekanism.common.capabilities.resolver.manager;

import java.util.List;
import java.util.function.BiFunction;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.capabilities.holder.IHolder;
import mekanism.common.capabilities.resolver.BasicSidedCapabilityResolver;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class CapabilityHandlerManager<HOLDER extends IHolder, CONTAINER, HANDLER> extends BasicSidedCapabilityResolver<HOLDER, HANDLER>
      implements ICapabilityHandlerManager<CONTAINER> {

    private final BiFunction<HOLDER, Direction, List<CONTAINER>> containerGetter;

    protected CapabilityHandlerManager(HOLDER holder, BlockCapability<HANDLER, @Nullable Direction> supportedCapability,
          BiFunction<HOLDER, Direction, List<CONTAINER>> containerGetter, ProxyCreator<HOLDER, HANDLER> proxyCreator) {
        super(holder, supportedCapability, proxyCreator);
        this.containerGetter = containerGetter;
    }

    @Override
    public List<CONTAINER> getContainers(@Nullable Direction side) {
        return containerGetter.apply(getHolder(), side);
    }

    @Nullable
    @Override
    public <T> T resolve(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (getContainers(side).isEmpty()) {
            //If we don't have any containers accessible from that side, don't return a handler
            return null;
        }
        return super.resolve(capability, side);
    }
}