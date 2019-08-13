package mekanism.common.item;

import mekanism.common.resource.INamedResource;
import mekanism.common.resource.ResourceType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ItemResource extends ItemMekanism {

    private final ResourceType type;
    private final INamedResource resource;

    public ItemResource(ResourceType type, INamedResource resource) {
        super(type.getRegistryPrefix() + "_" + resource.getRegistrySuffix());
        this.type = type;
        this.resource = resource;
    }

    @Override
    public void registerOreDict() {
        //TODO: Make tags for Compressed resources
        OreDictionary.registerOre(type.getOrePrefix() + resource.getOreSuffix(), new ItemStack(this));
    }
}