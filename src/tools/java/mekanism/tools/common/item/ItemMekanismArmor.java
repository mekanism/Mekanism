package mekanism.tools.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.tools.client.render.GlowArmor;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.registries.ToolsItems;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemMekanismArmor extends ArmorItem implements IHasRepairType, IAttributeRefresher {

    private final MaterialCreator material;
    private final AttributeCache attributeCache;
    private final boolean makesPiglinsNeutral;

    public ItemMekanismArmor(MaterialCreator material, EquipmentSlotType slot, Item.Properties properties, boolean makesPiglinsNeutral) {
        super(material, slot, properties);
        this.material = material;
        this.makesPiglinsNeutral = makesPiglinsNeutral;
        CachedIntValue armorConfig;
        if (slot == EquipmentSlotType.FEET) {
            armorConfig = material.bootArmor;
        } else if (slot == EquipmentSlotType.LEGS) {
            armorConfig = material.leggingArmor;
        } else if (slot == EquipmentSlotType.CHEST) {
            armorConfig = material.chestplateArmor;
        } else if (slot == EquipmentSlotType.HEAD) {
            armorConfig = material.helmetArmor;
        } else {
            throw new IllegalArgumentException("Invalid slot type for armor");
        }
        this.attributeCache = new AttributeCache(this, material.toughness, material.knockbackResistance, armorConfig);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BipedModel getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlotType armorSlot, BipedModel defaultModel) {
        if (itemStack.getItem() == ToolsItems.REFINED_GLOWSTONE_HELMET.getItem() || itemStack.getItem() == ToolsItems.REFINED_GLOWSTONE_CHESTPLATE.getItem()
            || itemStack.getItem() == ToolsItems.REFINED_GLOWSTONE_LEGGINGS.getItem() || itemStack.getItem() == ToolsItems.REFINED_GLOWSTONE_BOOTS.getItem()) {
            return GlowArmor.getGlow(armorSlot);
        }
        return super.getArmorModel(entityLiving, itemStack, armorSlot, defaultModel);
    }

    @Override
    public boolean makesPiglinsNeutral(@Nonnull ItemStack stack, @Nonnull LivingEntity wearer) {
        return makesPiglinsNeutral;
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getArmorMaterial().getRepairMaterial();
    }

    @Override
    public int getDamageReduceAmount() {
        return getArmorMaterial().getDamageReductionAmount(getEquipmentSlot());
    }

    @Override
    public float getToughness() {
        return getArmorMaterial().getToughness();
    }

    public float getKnockbackResistance() {
        return getArmorMaterial().getKnockbackResistance();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getDurability(getEquipmentSlot());
    }

    @Override
    public boolean isDamageable() {
        return material.getDurability(getEquipmentSlot()) > 0;
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        return slot == getEquipmentSlot() ? attributeCache.getAttributes() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        UUID modifier = ARMOR_MODIFIERS[getEquipmentSlot().getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(modifier, "Armor modifier", getDamageReduceAmount(), Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifier, "Armor toughness", getToughness(), Operation.ADDITION));
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifier, "Armor knockback resistance", getKnockbackResistance(), Operation.ADDITION));
    }
}