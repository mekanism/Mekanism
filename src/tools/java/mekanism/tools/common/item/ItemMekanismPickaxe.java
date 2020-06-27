package mekanism.tools.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class ItemMekanismPickaxe extends PickaxeItem implements IHasRepairType {

    private final BaseMekanismMaterial material;

    public ItemMekanismPickaxe(BaseMekanismMaterial material) {
        super(material, material.getPickaxeDamage(), material.getPickaxeAtkSpeed(), ItemDeferredRegister.getMekBaseProperties());
        this.material = material;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        tooltip.add(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamage()));
    }

    public float getAttackDamage() {
        return material.getPickaxeDamage() + getTier().getAttackDamage();
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Wrap {@link PickaxeItem#getDestroySpeed(ItemStack, BlockState)} and  {@link net.minecraft.item.ToolItem#getDestroySpeed(ItemStack, BlockState)} to return
     * our efficiency level
     */
    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, BlockState state) {
        Material material = state.getMaterial();
        if (material == Material.IRON || material == Material.ANVIL || material == Material.ROCK || getToolTypes(stack).stream().anyMatch(state::isToolEffective) ||
            effectiveBlocks.contains(state.getBlock())) {
            return getTier().getEfficiency();
        }
        return 1;
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairMaterial();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getTier().getMaxUses();
    }

    @Override
    public boolean isDamageable() {
        return getTier().getMaxUses() > 0;
    }

    @Override
    public int getHarvestLevel(@Nonnull ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        return tool == ToolType.PICKAXE ? getTier().getHarvestLevel() : super.getHarvestLevel(stack, tool, player, blockState);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We bypass calling super to ensure we get added instead of not being able to add the proper values that {@link net.minecraft.item.ToolItem} tries to set
     */
    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        //TODO - 1.16: Cache this, and update it when one of the values change
        Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
        if (slot == EquipmentSlotType.MAINHAND) {
            attributes.put(Attributes.field_233823_f_, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getAttackDamage(), Operation.ADDITION));
            attributes.put(Attributes.field_233825_h_, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", material.getPickaxeAtkSpeed(), Operation.ADDITION));
        }
        return attributes;
    }
}