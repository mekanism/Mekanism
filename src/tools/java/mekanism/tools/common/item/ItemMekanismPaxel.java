package mekanism.tools.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collections;
import java.util.List;
import java.util.function.IntSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.functions.FloatSupplier;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.material.Material;
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
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.Constants.WorldEvents;

public class ItemMekanismPaxel extends ToolItem implements IHasRepairType, IAttributeRefresher {

    private static final ToolType PAXEL_TOOL_TYPE = ToolType.get("paxel");

    private static Item.Properties getItemProperties(ItemTier material) {
        Item.Properties properties = ItemDeferredRegister.getMekBaseProperties();
        if (material == ItemTier.NETHERITE) {
            properties = properties.isImmuneToFire();
        }
        return addHarvestLevel(properties, material.getHarvestLevel());
    }

    private static Item.Properties addHarvestLevel(Item.Properties properties, int harvestLevel) {
        return properties.addToolType(ToolType.AXE, harvestLevel).addToolType(ToolType.PICKAXE, harvestLevel)
              .addToolType(ToolType.SHOVEL, harvestLevel).addToolType(PAXEL_TOOL_TYPE, harvestLevel);
    }

    private final FloatSupplier paxelDamage;
    private final FloatSupplier paxelAtkSpeed;
    private final FloatSupplier paxelEfficiency;
    private final IntSupplier paxelEnchantability;
    private final IntSupplier paxelMaxDurability;
    private final IntSupplier paxelHarvestLevel;
    private final AttributeCache attributeCache;

    public ItemMekanismPaxel(MaterialCreator material, Item.Properties properties) {
        super(material.getPaxelDamage(), material.getPaxelAtkSpeed(), material, Collections.emptySet(), addHarvestLevel(properties, material.getPaxelHarvestLevel()));
        paxelDamage = material::getPaxelDamage;
        paxelAtkSpeed = material::getPaxelAtkSpeed;
        paxelEfficiency = material::getPaxelEfficiency;
        paxelEnchantability = material::getPaxelEnchantability;
        paxelMaxDurability = material::getPaxelMaxUses;
        paxelHarvestLevel = material::getPaxelHarvestLevel;
        this.attributeCache = new AttributeCache(this, material.attackDamage, material.paxelDamage, material.paxelAtkSpeed);
    }

    public ItemMekanismPaxel(ItemTier material) {
        super(4, -2.4F, material, Collections.emptySet(), getItemProperties(material));
        paxelDamage = () -> 4;
        paxelAtkSpeed = () -> -2.4F;
        paxelEfficiency = material::getEfficiency;
        paxelEnchantability = material::getEnchantability;
        paxelMaxDurability = material::getMaxUses;
        paxelHarvestLevel = material::getHarvestLevel;
        //Don't add any listeners as all the values are "static"
        attributeCache = new AttributeCache(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    @Override
    public float getAttackDamage() {
        return paxelDamage.getAsFloat() + getTier().getAttackDamage();
    }

    private int getHarvestLevel() {
        return paxelHarvestLevel.getAsInt();
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
        if (state.isIn(Blocks.SNOW) || state.isIn(Blocks.SNOW_BLOCK)) {
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
        //If pickaxe hardcoded shortcut, or axe hardcoded shortcut or ToolItem#getDestroySpeed checks
        //Note: We do it this way so that we don't need to check if the AxeItem material set contains the material if one of the pickaxe checks match
        if (material == Material.IRON || material == Material.ANVIL || material == Material.ROCK || AxeItem.EFFECTIVE_ON_MATERIALS.contains(material) ||
            getToolTypes(stack).stream().anyMatch(state::isToolEffective) || effectiveBlocks.contains(state.getBlock())) {
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
        ItemStack stack = context.getItem();
        BlockState blockstate = world.getBlockState(blockpos);
        BlockState resultToSet = blockstate.getToolModifiedState(world, blockpos, player, stack, ToolType.AXE);
        if (resultToSet != null) {
            //We can strip the item as an axe
            world.playSound(player, blockpos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
        } else {
            //We cannot strip the item that was right clicked, so attempt to use the paxel as a shovel
            if (context.getFace() == Direction.DOWN) {
                return ActionResultType.PASS;
            }
            BlockState foundResult = blockstate.getToolModifiedState(world, blockpos, player, stack, ToolType.SHOVEL);
            if (foundResult != null && world.isAirBlock(blockpos.up())) {
                //We can flatten the item as a shovel
                world.playSound(player, blockpos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                resultToSet = foundResult;
            } else if (blockstate.getBlock() instanceof CampfireBlock && blockstate.get(CampfireBlock.LIT)) {
                //We can use the paxel as a shovel to extinguish a campfire
                if (!world.isRemote) {
                    world.playEvent(null, WorldEvents.FIRE_EXTINGUISH_SOUND, blockpos, 0);
                }
                CampfireBlock.extinguish(world, blockpos, blockstate);
                resultToSet = blockstate.with(CampfireBlock.LIT, false);
            }
        }
        if (resultToSet == null) {
            return ActionResultType.PASS;
        }
        if (!world.isRemote) {
            world.setBlockState(blockpos, resultToSet, BlockFlags.DEFAULT_AND_RERENDER);
            if (player != null) {
                stack.damageItem(1, player, onBroken -> onBroken.sendBreakAnimation(context.getHand()));
            }
        }
        return ActionResultType.func_233537_a_(world.isRemote);
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
    public int getHarvestLevel(@Nonnull ItemStack stack, @Nonnull ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
        if (tool == ToolType.AXE || tool == ToolType.PICKAXE || tool == ToolType.SHOVEL || tool == PAXEL_TOOL_TYPE) {
            return getHarvestLevel();
        }
        return super.getHarvestLevel(stack, tool, player, blockState);
    }

    @Nonnull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@Nonnull EquipmentSlotType slot, @Nonnull ItemStack stack) {
        return slot == EquipmentSlotType.MAINHAND ? attributeCache.getAttributes() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", getAttackDamage(), Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", paxelAtkSpeed.getAsFloat(), Operation.ADDITION));
    }
}