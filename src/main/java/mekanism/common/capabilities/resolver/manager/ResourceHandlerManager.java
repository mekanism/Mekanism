package mekanism.common.capabilities.resolver.manager;

import java.util.List;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.common.capabilities.MultiTypeCapability;
import mekanism.common.capabilities.holder.IContainerHolder;
import mekanism.common.capabilities.proxy.ProxyResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

/**
 * Helper class to make reading easier rather than having super messy generics
 */
@NothingNullByDefault
public class ResourceHandlerManager<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>> extends CapabilityHandlerManager<IContainerHolder<CONTAINER>, CONTAINER, ResourceHandler<RESOURCE>> {

    public ResourceHandlerManager(MultiTypeCapability<ResourceHandler<RESOURCE>> supportedCapability, IContainerHolder<CONTAINER> holder,
          @Nullable IContentsListener changeListener) {
        super(holder, supportedCapability.block(), IContainerHolder::getContainers, (side, containerHolder) ->
              new ProxyResourceHandler<>(new IMekanismResourceHandler<RESOURCE, CONTAINER>() {
                  @Override
                  public void onContentsChanged() {
                      if (changeListener != null) {
                          changeListener.onContentsChanged();
                      }
                  }

                  @Override
                  public List<CONTAINER> getContainers() {
                      return containerHolder.getContainers(side);
                  }
              }, side, containerHolder)
        );
    }
}