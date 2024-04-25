package mekanism.tools.common.item;

import java.util.List;
import java.util.UUID;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.integration.gender.ToolsGenderCapabilityHelper;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;

public class ItemMekanismArmor extends ArmorItem implements IHasRepairType, IAttributeRefresher, ICapabilityAware {

    private final MaterialCreator material;
    private final AttributeCache attributeCache;

    public ItemMekanismArmor(MaterialCreator material, ArmorItem.Type armorType, Item.Properties properties) {
        //TODO - 1.20.5: Figure this out
        super(ArmorMaterials.IRON, armorType, properties);
        //super(material, armorType, properties);
        this.material = material;
        CachedIntValue armorConfig = switch (armorType) {
            case BOOTS -> material.bootArmor;
            case LEGGINGS -> material.leggingArmor;
            case CHESTPLATE, BODY -> material.chestplateArmor;
            case HELMET -> material.helmetArmor;
        };
        this.attributeCache = new AttributeCache(this, material.toughness, material.knockbackResistance, armorConfig);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    @NotNull
    @Override
    public Ingredient getRepairMaterial() {
        return getMaterial().value().repairIngredient().get();
    }

    @Override
    public int getDefense() {
        return getMaterial().value().getDefense(getType());
    }

    @Override
    public float getToughness() {
        return getMaterial().value().toughness();
    }

    public float knockbackResistance() {
        return getMaterial().value().knockbackResistance();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getDurabilityForType(getType());
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

    @Override
    public void attachCapabilities(RegisterCapabilitiesEvent event) {
        ToolsGenderCapabilityHelper.addGenderCapability(this, event);
    }
}