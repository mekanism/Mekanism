package mekanism.common.resource;

import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

public interface IResource {

    String getRegistrySuffix();

    Item.Properties modifyProperties(Item.Properties properties, @Nullable ResourceType resourceType);
}