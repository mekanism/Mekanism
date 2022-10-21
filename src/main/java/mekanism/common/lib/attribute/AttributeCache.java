package mekanism.common.lib.attribute;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import mekanism.common.config.listener.ConfigBasedCachedSupplier;
import mekanism.common.config.value.CachedValue;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCache extends ConfigBasedCachedSupplier<Multimap<Attribute, AttributeModifier>> {

    public AttributeCache(IAttributeRefresher attributeRefresher, CachedValue<?>... configValues) {
        super(() -> {
            Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            attributeRefresher.addToBuilder(builder);
            return builder.build();
        }, configValues);
    }
}