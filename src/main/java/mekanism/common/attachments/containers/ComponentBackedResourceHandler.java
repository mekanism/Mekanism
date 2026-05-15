package mekanism.common.attachments.containers;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.resource.IMekanismResourceHandler;
import mekanism.api.resource.IResourceContainer;
import mekanism.api.resource.LargeResourceStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;

@NothingNullByDefault
public abstract class ComponentBackedResourceHandler<RESOURCE extends Resource, CONTAINER extends IResourceContainer<RESOURCE>>
      extends ComponentBackedHandler<LargeResourceStack<RESOURCE>, CONTAINER, AttachedResources<RESOURCE>> implements IMekanismResourceHandler<RESOURCE, CONTAINER> {

    public ComponentBackedResourceHandler(ItemStack attachedTo, int totalSlots) {
        super(attachedTo, totalSlots);
    }
}