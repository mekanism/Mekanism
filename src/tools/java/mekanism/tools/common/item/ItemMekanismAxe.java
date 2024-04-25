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
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class ItemMekanismAxe extends AxeItem implements IHasRepairType, IAttributeRefresher {

    private final MaterialCreator material;
    private final AttributeCache attributeCache;

    public ItemMekanismAxe(MaterialCreator material, Item.Properties properties) {
        //TODO - 1.20.5: Figure this out
        super(Tiers.IRON, properties);
        //super(material, material.getAxeDamage(), material.getAxeAtkSpeed(), properties);
        this.material = material;
        this.attributeCache = new AttributeCache(this, material.attackDamage, material.axeDamage, material.axeAtkSpeed);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    //TODO - 1.20.5: ??
    //@Override
    public float getAttackDamage() {
        return material.getAxeDamage() + getTier().getAttackDamageBonus();
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return super.getDestroySpeed(stack, state) == 1 ? 1 : getTier().getSpeed();
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
              new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", getAttackDamage(), Operation.ADD_VALUE),
              EquipmentSlotGroup.MAINHAND
        ));
        builder.add(new ItemAttributeModifiers.Entry(
              Attributes.ATTACK_SPEED,
              new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", material.getAxeAtkSpeed(), Operation.ADD_VALUE),
              EquipmentSlotGroup.MAINHAND
        ));
    }
}