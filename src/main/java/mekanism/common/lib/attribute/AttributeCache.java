package mekanism.common.lib.attribute;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;
import mekanism.common.config.value.CachedValue;
import mekanism.common.config.value.CachedValue.IConfigValueInvalidationListener;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeCache {

    private final IAttributeRefresher attributeRefresher;
    private Multimap<Attribute, AttributeModifier> attributes;

    public AttributeCache(IAttributeRefresher attributeRefresher, CachedValue<?>... configValues) {
        this.attributeRefresher = attributeRefresher;
        IConfigValueInvalidationListener refreshListener = this::refreshAttributes;
        for (CachedValue<?> configValue : configValues) {
            configValue.addInvalidationListener(refreshListener);
        }
        refreshAttributes();
    }

    private void refreshAttributes() {
        Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        attributeRefresher.addToBuilder(builder);
        this.attributes = builder.build();
    }

    public Multimap<Attribute, AttributeModifier> getAttributes() {
        return attributes;
    }
}