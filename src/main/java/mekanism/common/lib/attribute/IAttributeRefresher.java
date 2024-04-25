package mekanism.common.lib.attribute;

import java.util.List;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface IAttributeRefresher {

    void addToBuilder(List<ItemAttributeModifiers.Entry> builder);
}