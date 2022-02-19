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
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class ItemMekanismAxe extends AxeItem implements IHasRepairType, IAttributeRefresher {

    private final MaterialCreator material;
    private final AttributeCache attributeCache;

    public ItemMekanismAxe(MaterialCreator material, Item.Properties properties) {
        super(material, material.getAxeDamage(), material.getAxeAtkSpeed(), properties);
        this.material = material;
        this.attributeCache = new AttributeCache(this, material.attackDamage, material.axeDamage, material.axeAtkSpeed);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    @Override
    public float getAttackDamage() {
        return material.getAxeDamage() + getTier().getAttackDamageBonus();
    }

    public int getHarvestLevel() {
        return getTier().getLevel();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Wrap {@link AxeItem#getDestroySpeed(ItemStack, BlockState)} and {@link net.minecraft.item.ToolItem#getDestroySpeed(ItemStack, BlockState)} to return our
     * efficiency level
     */
    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, BlockState state) {
        if (DIGGABLE_MATERIALS.contains(state.getMaterial()) || getToolTypes(stack).stream().anyMatch(state::isToolEffective) || blocks.contains(state.getBlock())) {
            return getTier().getSpeed();
        }
        return 1;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Patches {@link AxeItem} to return true when the block's harvest tool is an axe
     */
    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.getHarvestTool() == ToolType.AXE ? getHarvestLevel() >= state.getHarvestLevel() : super.isCorrectToolForDrops(state);
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

    @Override
    public int getHarvestLevel(@Nonnull ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        return tool == ToolType.AXE ? getHarvestLevel() : super.getHarvestLevel(stack, tool, player, blockState);
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? attributeCache.getAttributes() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", getAttackDamage(), Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", material.getAxeAtkSpeed(), Operation.ADDITION));
    }
}