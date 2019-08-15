package mekanism.common.item;

import mekanism.common.resource.INamedResource;
import mekanism.common.resource.ResourceType;

public class ItemResource extends ItemMekanism {

    private final ResourceType type;
    private final INamedResource resource;

    public ItemResource(ResourceType type, INamedResource resource) {
        super(type.getRegistryPrefix() + "_" + resource.getRegistrySuffix());
        this.type = type;
        this.resource = resource;
    }
}