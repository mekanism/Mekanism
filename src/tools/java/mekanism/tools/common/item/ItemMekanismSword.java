package mekanism.tools.common.item;

import java.util.List;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

public class ItemMekanismSword extends SwordItem implements IHasRepairType, IAttributeRefresher {

    private final MaterialCreator material;
    private final AttributeCache attributeCache;

    public ItemMekanismSword(MaterialCreator material, Item.Properties properties) {
        //TODO - 1.20.5: Figure this out
        super(Tiers.IRON, properties);
        //super(material, (int) material.getSwordDamage(), material.getSwordAtkSpeed(), properties);
        this.material = material;
        this.attributeCache = new AttributeCache(this, material.attackDamage, material.swordDamage, material.swordAtkSpeed);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    //TODO - 1.20.5: ??
    //@Override
    public float getDamage() {
        return material.getSwordDamage() + getTier().getAttackDamageBonus();
    }

    @NotNull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairIngredient();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getTier().getUses();
    }

    @NotNull
    @Override
    public ItemAttributeModifiers getAttributeModifiers(@NotNull ItemStack stack) {
        return attributeCache.get();
    }

    @Override
    public void addToBuilder(List<ItemAttributeModifiers.Entry> builder) {
        builder.add(new ItemAttributeModifiers.Entry(
              Attributes.ATTACK_DAMAGE,
              new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", getDamage(), Operation.ADD_VALUE),
              EquipmentSlotGroup.MAINHAND
        ));
        builder.add(new ItemAttributeModifiers.Entry(
              Attributes.ATTACK_SPEED,
              new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", material.getSwordAtkSpeed(), Operation.ADD_VALUE),
              EquipmentSlotGroup.MAINHAND
        ));
    }
}