package mekanism.tools.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.config.value.CachedIntValue;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.integration.gender.ToolsGenderCapabilityHelper;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class ItemMekanismArmor extends ArmorItem implements IHasRepairType, IAttributeRefresher {

    private final MaterialCreator material;
    private final AttributeCache attributeCache;

    public ItemMekanismArmor(MaterialCreator material, EquipmentSlot slot, Item.Properties properties) {
        super(material, slot, properties);
        this.material = material;
        CachedIntValue armorConfig = switch (slot) {
            case FEET -> material.bootArmor;
            case LEGS -> material.leggingArmor;
            case CHEST -> material.chestplateArmor;
            case HEAD -> material.helmetArmor;
            default -> throw new IllegalArgumentException("Invalid slot type for armor");
        };
        this.attributeCache = new AttributeCache(this, material.toughness, material.knockbackResistance, armorConfig);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getMaterial().getRepairIngredient();
    }

    @Override
    public int getDefense() {
        return getMaterial().getDefenseForSlot(getSlot());
    }

    @Override
    public float getToughness() {
        return getMaterial().getToughness();
    }

    public float getKnockbackResistance() {
        return getMaterial().getKnockbackResistance();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getDurabilityForSlot(getSlot());
    }

    @Override
    public boolean canBeDepleted() {
        return material.getDurabilityForSlot(getSlot()) > 0;
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        return slot == getSlot() ? attributeCache.getAttributes() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        UUID modifier = ARMOR_MODIFIER_UUID_PER_SLOT[getSlot().getIndex()];
        builder.put(Attributes.ARMOR, new AttributeModifier(modifier, "Armor modifier", getDefense(), Operation.ADDITION));
        builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifier, "Armor toughness", getToughness(), Operation.ADDITION));
        builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(modifier, "Armor knockback resistance", getKnockbackResistance(), Operation.ADDITION));
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        ItemCapabilityWrapper wrapper = new ItemCapabilityWrapper(stack);
        ToolsGenderCapabilityHelper.addGenderCapability(this, wrapper);
        return wrapper;
    }
}