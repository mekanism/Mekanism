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

public class ItemArmoredJetpack extends ItemJetpack implements IAttributeRefresher {

    private final AttributeCache attributeCache;

    public ItemArmoredJetpack(Properties properties) {
        super(MekanismArmorMaterials.ARMORED_JETPACK, properties);
        this.attributeCache = new AttributeCache(this, MekanismConfig.gear.armoredJetpackArmor, MekanismConfig.gear.armoredJetpackToughness,
              MekanismConfig.gear.armoredJetpackKnockbackResistance);
    }

    @Override
    public void initializeClient(@NotNull Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RenderPropertiesProvider.armoredJetpack());
    }

    @Override
    public int getDefense() {
        return getMaterial().value().getDefense(getType());
    }

    @Override
    public float getToughness() {
        return getMaterial().value().toughness();
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
              new AttributeModifier(modifier, "Armor knockback resistance", getMaterial().value().knockbackResistance(), Operation.ADD_VALUE),
              EquipmentSlotGroup.FEET
        ));
    }
}