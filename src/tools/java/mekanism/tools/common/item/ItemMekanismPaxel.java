package mekanism.tools.common.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.HashSet;
import java.util.List;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.material.BaseMekanismMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.Constants.WorldEvents;

public class ItemMekanismPaxel extends ToolItem implements IHasRepairType {

    private static final ToolType PAXEL_TOOL_TYPE = ToolType.get("paxel");

    private static Item.Properties getItemProperties(int harvestLevel) {
        return ItemDeferredRegister.getMekBaseProperties().addToolType(ToolType.AXE, harvestLevel).addToolType(ToolType.PICKAXE, harvestLevel)
              .addToolType(ToolType.SHOVEL, harvestLevel).addToolType(PAXEL_TOOL_TYPE, harvestLevel);
    }

    private final FloatSupplier paxelDamage;
    private final FloatSupplier paxelAtkSpeed;
    private final FloatSupplier paxelEfficiency;
    private final IntSupplier paxelEnchantability;
    private final IntSupplier paxelMaxDurability;

    public ItemMekanismPaxel(BaseMekanismMaterial material) {
        super(material.getPaxelDamage(), material.getPaxelAtkSpeed(), material, new HashSet<>(), getItemProperties(material.getPaxelHarvestLevel()));
        paxelDamage = material::getPaxelDamage;
        paxelAtkSpeed = material::getPaxelAtkSpeed;
        paxelEfficiency = material::getPaxelEfficiency;
        paxelEnchantability = material::getPaxelEnchantability;
        paxelMaxDurability = material::getPaxelMaxUses;
    }

    public ItemMekanismPaxel(ItemTier material) {
        super(4, -2.4F, material, new HashSet<>(), getItemProperties(material.getHarvestLevel()));
        paxelDamage = () -> 4;
        paxelAtkSpeed = () -> -2.4F;
        paxelEfficiency = material::getEfficiency;
        paxelEnchantability = material::getEnchantability;
        paxelMaxDurability = material::getMaxUses;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamage()));
    }

    public float getAttackDamage() {
        return paxelDamage.getAsFloat() + getTier().getAttackDamage();
    }

    public int getHarvestLevel() {
        return getTier().getHarvestLevel();
    }

    @Override
    public boolean canHarvestBlock(BlockState state) {
        ToolType harvestTool = state.getHarvestTool();
        if (harvestTool == ToolType.AXE || harvestTool == ToolType.PICKAXE || harvestTool == ToolType.SHOVEL) {
            if (getHarvestLevel() >= state.getHarvestLevel()) {
                //If the required tool type is one of the tools we "support" then return that we can harvest it if
                // we have an equal or higher harvest level
                return true;
            }
        }
        Block block = state.getBlock();
        if (block == Blocks.SNOW || block == Blocks.SNOW_BLOCK) {
            //Extra hardcoded shovel checks
            return true;
        }
        //Extra hardcoded pickaxe checks
        Material material = state.getMaterial();
        return material == Material.ROCK || material == Material.IRON || material == Material.ANVIL;
    }

    /**
     * {@inheritDoc}
     *
     * @implNote Include hardcoded checks from other items and wrap {@link net.minecraft.item.ToolItem#getDestroySpeed(ItemStack, BlockState)} to return our efficiency
     * level
     */
    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, BlockState state) {
        Material material = state.getMaterial();
        boolean pickaxeShortcut = material == Material.IRON || material == Material.ANVIL || material == Material.ROCK;
        boolean axeShortcut = material == Material.WOOD || material == Material.PLANTS || material == Material.TALL_PLANTS || material == Material.BAMBOO;
        if (pickaxeShortcut || axeShortcut || getToolTypes(stack).stream().anyMatch(state::isToolEffective) || effectiveBlocks.contains(state.getBlock())) {
            return paxelEfficiency.getAsFloat();
        }
        return 1;
    }

    /**
     * {@inheritDoc}
     *
     * Merged version of {@link AxeItem#onItemUse(ItemUseContext)} and {@link ShovelItem#onItemUse(ItemUseContext)}
     */
    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos blockpos = context.getPos();
        PlayerEntity player = context.getPlayer();
        BlockState blockstate = world.getBlockState(blockpos);
        BlockState resultToSet = null;
        Block strippedResult = AxeItem.BLOCK_STRIPPING_MAP.get(blockstate.getBlock());
        if (strippedResult != null) {
            //We can strip the item as an axe
            world.playSound(player, blockpos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
            resultToSet = strippedResult.getDefaultState().with(RotatedPillarBlock.AXIS, blockstate.get(RotatedPillarBlock.AXIS));
        } else {
            //We cannot strip the item that was right clicked, so attempt to use the paxel as a shovel
            if (context.getFace() == Direction.DOWN) {
                return ActionResultType.PASS;
            }
            BlockState foundResult = ShovelItem.SHOVEL_LOOKUP.get(blockstate.getBlock());
            if (foundResult != null && world.isAirBlock(blockpos.up())) {
                //We can flatten the item as a shovel
                world.playSound(player, blockpos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                resultToSet = foundResult;
            } else if (blockstate.getBlock() instanceof CampfireBlock && blockstate.get(CampfireBlock.LIT)) {
                //We can use the paxel as a shovel to extinguish a campfire
                world.playEvent(null, WorldEvents.FIRE_EXTINGUISH_SOUND, blockpos, 0);
                resultToSet = blockstate.with(CampfireBlock.LIT, false);
            }
        }
        if (resultToSet == null) {
            return ActionResultType.PASS;
        }
        if (!world.isRemote) {
            world.setBlockState(blockpos, resultToSet, 11);
            if (player != null) {
                context.getItem().damageItem(1, player, onBroken -> onBroken.sendBreakAnimation(context.getHand()));
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public int getItemEnchantability() {
        return paxelEnchantability.getAsInt();
    }

    @Nonnull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairMaterial();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return paxelMaxDurability.getAsInt();
    }

    @Override
    public boolean isDamageable() {
        return paxelMaxDurability.getAsInt() > 0;
    }

    @Override
    public int getHarvestLevel(ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        if (tool == ToolType.AXE || tool == ToolType.PICKAXE || tool == ToolType.SHOVEL || tool == PAXEL_TOOL_TYPE) {
            return getHarvestLevel();
        }
        return super.getHarvestLevel(stack, tool, player, blockState);
    }

    /**
     * {@inheritDoc}
     *
     * @implNote We bypass calling super to ensure we get added instead of not being able to add the proper values that {@link net.minecraft.item.ToolItem} tries to set
     */
    @Nonnull
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot) {
        Multimap<String, AttributeModifier> attributes = HashMultimap.create();
        if (slot == EquipmentSlotType.MAINHAND) {
            attributes.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getAttackDamage(), Operation.ADDITION));
            attributes.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", paxelAtkSpeed.getAsFloat(), Operation.ADDITION));
        }
        return attributes;
    }

    /**
     * Primitive supplier helper for floats
     */
    @FunctionalInterface
    private interface FloatSupplier {

        /**
         * Gets a result.
         *
         * @return a result
         */
        float getAsFloat();
    }
}