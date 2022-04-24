package mekanism.tools.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class ItemMekanismSword extends SwordItem implements IHasRepairType, IAttributeRefresher {

    private final MaterialCreator material;
    private final AttributeCache attributeCache;

    public ItemMekanismSword(MaterialCreator material, Item.Properties properties) {
        super(material, (int) material.getSwordDamage(), material.getSwordAtkSpeed(), properties);
        this.material = material;
        this.attributeCache = new AttributeCache(this, material.attackDamage, material.swordDamage, material.swordAtkSpeed);
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    @Override
    public float getDamage() {
        return material.getSwordDamage() + getTier().getAttackDamageBonus();
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairIngredient();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getTier().getUses();
    }

    @Override
    public boolean canBeDepleted() {
        return getTier().getUses() > 0;
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlot slot, @Nonnull ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? attributeCache.getAttributes() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getDamage(), Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", material.getSwordAtkSpeed(), Operation.ADDITION));
    }
}