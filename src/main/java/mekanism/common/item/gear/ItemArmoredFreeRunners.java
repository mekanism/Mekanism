package mekanism.common.item.gear;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import mekanism.client.render.RenderPropertiesProvider;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.common.registries.MekanismArmorMaterials;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemArmoredFreeRunners extends ItemFreeRunners implements IAttributeRefresher {

    private final AttributeCache attributeCache;

    public ItemArmoredFreeRunners(Properties properties) {
        super(MekanismArmorMaterials.ARMORED_FREE_RUNNERS, properties);
        this.attributeCache = new AttributeCache(this, MekanismConfig.gear.armoredFreeRunnerArmor, MekanismConfig.gear.armoredFreeRunnerToughness,
              MekanismConfig.gear.armoredFreeRunnerKnockbackResistance);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.armoredFreeRunners());
    }

    @Override
    public int getDefense() {
        return MekanismConfig.gear.armoredFreeRunnerArmor.getOrDefault();
    }

    @Override
    public float getToughness() {
        return MekanismConfig.gear.armoredFreeRunnerToughness.getOrDefault();
    }

    @NotNull
    @Override
    public ItemAttributeModifiers getAttributeModifiers(@NotNull ItemStack stack) {
        return attributeCache.get();
    }

    @Override
    public void addToBuilder(List<ItemAttributeModifiers.Entry> builder) {
        UUID modifier = ARMOR_MODIFIER_UUID_PER_TYPE.get(getType());
        builder.add(new ItemAttributeModifiers.Entry(
              Attributes.ARMOR,
              new AttributeModifier(modifier, "Armor modifier", getDefense(), Operation.ADD_VALUE),
              EquipmentSlotGroup.FEET
        ));
        builder.add(new ItemAttributeModifiers.Entry(
              Attributes.ARMOR_TOUGHNESS,
              new AttributeModifier(modifier, "Armor toughness", getToughness(), Operation.ADD_VALUE),
              EquipmentSlotGroup.FEET
        ));
        builder.add(new ItemAttributeModifiers.Entry(
              Attributes.KNOCKBACK_RESISTANCE,
              new AttributeModifier(modifier, "Armor knockback resistance", MekanismConfig.gear.armoredFreeRunnerKnockbackResistance.getOrDefault(), Operation.ADD_VALUE),
              EquipmentSlotGroup.FEET
        ));
    }
}
