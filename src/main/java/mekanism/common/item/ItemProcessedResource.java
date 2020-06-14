package mekanism.common.item;

import mekanism.common.resource.PrimaryResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.item.Item;

public class ItemProcessedResource extends Item {

    private final ResourceType type;
    private final PrimaryResource resource;

    public ItemProcessedResource(Item.Properties properties, ResourceType type, PrimaryResource resource) {
        super(properties);
        this.type = type;
        this.resource = resource;
    }
}
