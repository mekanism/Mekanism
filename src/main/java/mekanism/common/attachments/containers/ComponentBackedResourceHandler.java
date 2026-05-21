package mekanism.common.attachments.containers;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;

@NothingNullByDefault
public class ComponentBackedResourceHandler<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>>
      extends ComponentBackedHandler<LargeResourceStack<RESOURCE>, CONTAINER, AttachedResources<RESOURCE>> implements IMekanismResourceHandler<RESOURCE, CONTAINER> {

    public ComponentBackedResourceHandler(ContainerType<CONTAINER, AttachedResources<RESOURCE>, ComponentBackedResourceHandler<RESOURCE, CONTAINER>> containerType,
          ItemAccess attachedAccess, int totalSlots) {
        super(containerType, attachedAccess, totalSlots);
    }
}