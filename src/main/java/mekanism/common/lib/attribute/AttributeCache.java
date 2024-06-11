package mekanism.common.lib.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import mekanism.common.config.listener.ConfigBasedCachedSupplier;
import mekanism.common.config.value.CachedValue;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class AttributeCache extends ConfigBasedCachedSupplier<ItemAttributeModifiers> {

    public AttributeCache(IAttributeRefresher attributeRefresher, CachedValue<?>... configValues) {
        super(() -> {
            List<ItemAttributeModifiers.Entry> builder = new ArrayList<>();
            attributeRefresher.addToBuilder(builder);
            //TODO - 1.21: Do we want these to show in the tooltip?
            return new ItemAttributeModifiers(Collections.unmodifiableList(builder), true);
        }, configValues);
    }
}