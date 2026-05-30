package mekanism.common.capabilities.proxy;

import java.util.Collections;
import java.util.List;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.capabilities.MultiTypeCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jspecify.annotations.Nullable;

public class BelowContainerCache<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> {

    private final BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction> capabilityCache;
    private final List<BlockCapabilityCache<ResourceHandler<RESOURCE>, @Nullable Direction>> handlersList;
    @Nullable
    private CONTAINER belowContainer;
    private boolean hasResolved;

    public BelowContainerCache(MultiTypeCapability<ResourceHandler<RESOURCE>> capability, ServerLevel level, BlockPos pos) {
        capabilityCache = capability.createCache(level, pos.below(), Direction.UP, ConstantPredicates.ALWAYS_TRUE, () -> {
            //Reset the tank that we know is below this
            hasResolved = false;
            belowContainer = null;
        });
        this.handlersList = Collections.singletonList(capabilityCache);
    }

    @Nullable
    public ResourceHandler<RESOURCE> getHandler() {
        return capabilityCache.getCapability();
    }

    @Nullable
    public CONTAINER getContainer(Class<? extends CONTAINER> containerClass) {
        if (!hasResolved) {
            hasResolved = true;
            ResourceHandler<RESOURCE> belowHandler = capabilityCache.getCapability();
            if (belowHandler != null && belowHandler.size() == 1 && belowHandler instanceof ProxyResourceHandler<RESOURCE, ?> proxyHandler) {
                IResourceContainer<RESOURCE> container = proxyHandler.getProxiedContainers().getFirst();
                if (containerClass.isInstance(container)) {
                    //Note: We don't need to bother with weak references as these are vertical so will always be in the same chunk
                    belowContainer = containerClass.cast(container);
                }
            }
        }
        return belowContainer;
    }
}