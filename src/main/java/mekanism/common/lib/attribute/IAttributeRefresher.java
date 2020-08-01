package mekanism.common.lib.attribute;

import com.google.common.collect.ImmutableMultimap;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public interface IAttributeRefresher {

    void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder);
}