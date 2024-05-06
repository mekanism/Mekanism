package mekanism.common.lib.attribute;

import java.util.List;
import net.minecraft.world.item.component.ItemAttributeModifiers;

//TODO - 1.20.5: Re-evaluate if something like this even works anymore
public interface IAttributeRefresher {

    void addToBuilder(List<ItemAttributeModifiers.Entry> builder);
}