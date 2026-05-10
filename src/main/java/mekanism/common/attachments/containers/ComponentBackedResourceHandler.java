package mekanism.common.attachments.containers;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.container.IMekanismResourceHandler;
import mekanism.api.container.IResourceContainer;
import mekanism.api.container.LargeResourceStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;

@NothingNullByDefault
public abstract class ComponentBackedResourceHandler<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>>
      extends ComponentBackedHandler<LargeResourceStack<RESOURCE>, CONTAINER, AttachedResources<RESOURCE>> implements IMekanismResourceHandler<RESOURCE, CONTAINER> {

    public ComponentBackedResourceHandler(ItemStack attachedTo, int totalSlots) {
        super(attachedTo, totalSlots);
    }
}