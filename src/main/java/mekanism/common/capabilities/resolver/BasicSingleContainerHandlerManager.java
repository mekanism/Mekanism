package mekanism.common.capabilities.resolver;

import mekanism.common.capabilities.holder.single.ISingleContainerHolder;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

public class BasicSingleContainerHandlerManager<CONTAINER, HANDLER> extends BasicSidedCapabilityResolver<ISingleContainerHolder<CONTAINER>, HANDLER> {

    private final ISingleContainerHolder<CONTAINER> holder;

    public BasicSingleContainerHandlerManager(ISingleContainerHolder<CONTAINER> holder, BlockCapability<HANDLER, @Nullable Direction> supportedCapability,
          ProxyCreator<ISingleContainerHolder<CONTAINER>, HANDLER> proxyCreator) {
        super(holder, supportedCapability,proxyCreator);
        this.holder = holder;
    }

    @Nullable
    public CONTAINER getContainer(@Nullable Direction side) {
        return holder.getContainer(side);
    }

    @Nullable
    @Override
    public <T> T resolve(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (getContainer(side) == null) {
            //If we don't have a container accessible from that side, don't return a handler
            return null;
        }
        return super.resolve(capability, side);
    }
}