package mekanism.common.content.gear.mekasuit;

import mekanism.api.annotations.ParametersAreNotNullByDefault;
import mekanism.api.gear.EnchantmentAwareModule;
import mekanism.api.gear.IModule;
import mekanism.common.Mekanism;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;

@ParametersAreNotNullByDefault
public class SoulSurferUnit implements EnchantmentAwareModule<SoulSurferUnit> {

    private static final ResourceLocation MOVEMENT_EFFICIENCY = Mekanism.rl("movement_efficiency");

    @Override
    public void adjustAttributes(IModule<SoulSurferUnit> module, ItemAttributeModifierEvent event) {
        AttributeModifier modifier = new AttributeModifier(MOVEMENT_EFFICIENCY, Math.min(1, 0.33333334F * module.getInstalledCount()), AttributeModifier.Operation.ADD_VALUE);
        event.addModifier(Attributes.MOVEMENT_EFFICIENCY, modifier, EquipmentSlotGroup.FEET);
    }

    @NotNull
    @Override
    public ResourceKey<Enchantment> enchantment() {
        return Enchantments.SOUL_SPEED;
    }
}