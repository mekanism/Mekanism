package mekanism.tools.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import mekanism.common.lib.attribute.AttributeCache;
import mekanism.common.lib.attribute.IAttributeRefresher;
import mekanism.tools.common.IHasRepairType;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.IPaxelMaterial;
import mekanism.tools.common.material.MaterialCreator;
import mekanism.tools.common.material.VanillaPaxelMaterialCreator;
import mekanism.tools.common.util.ToolsUtils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemMekanismPaxel extends DiggerItem implements IHasRepairType, IAttributeRefresher {

    private static final ToolAction PAXEL_DIG = ToolAction.get("paxel_dig");

    private final IPaxelMaterial material;
    private final AttributeCache attributeCache;

    public ItemMekanismPaxel(MaterialCreator material, Item.Properties properties) {
        super(material.getPaxelDamage(), material.getPaxelAtkSpeed(), material, ToolsTags.Blocks.MINEABLE_WITH_PAXEL, properties);
        this.material = material;
        this.attributeCache = new AttributeCache(this, material.attackDamage, material.paxelDamage, material.paxelAtkSpeed);
    }

    public ItemMekanismPaxel(VanillaPaxelMaterialCreator material, Item.Properties properties) {
        super(material.getPaxelDamage(), material.getPaxelAtkSpeed(), material.getVanillaTier(), ToolsTags.Blocks.MINEABLE_WITH_PAXEL, properties);
        this.material = material;
        //Don't add the material's damage as a listener as the vanilla component is not configurable
        this.attributeCache = new AttributeCache(this, material.paxelDamage, material.paxelAtkSpeed);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level world, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        ToolsUtils.addDurability(tooltip, stack);
    }

    @Override
    public float getAttackDamage() {
        return material.getPaxelDamage() + getTier().getAttackDamageBonus();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction action) {
        return action == PAXEL_DIG || ToolActions.DEFAULT_AXE_ACTIONS.contains(action) || ToolActions.DEFAULT_PICKAXE_ACTIONS.contains(action) ||
               ToolActions.DEFAULT_SHOVEL_ACTIONS.contains(action);
    }

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        return super.getDestroySpeed(stack, state) == 1 ? 1 : material.getPaxelEfficiency();
    }

    /**
     * {@inheritDoc}
     *
     * Merged version of {@link AxeItem#useOn(UseOnContext)} and {@link net.minecraft.world.item.ShovelItem#useOn(UseOnContext)}
     */
    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState blockstate = world.getBlockState(blockpos);
        BlockState resultToSet = useAsAxe(blockstate, context);
        if (resultToSet == null) {
            //We cannot strip the item that was right-clicked, so attempt to use the paxel as a shovel
            if (context.getClickedFace() == Direction.DOWN) {
                return InteractionResult.PASS;
            }
            BlockState foundResult = blockstate.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
            if (foundResult != null && world.isEmptyBlock(blockpos.above())) {
                //We can flatten the item as a shovel
                world.playSound(player, blockpos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                resultToSet = foundResult;
            } else if (blockstate.getBlock() instanceof CampfireBlock && blockstate.getValue(CampfireBlock.LIT)) {
                //We can use the paxel as a shovel to extinguish a campfire
                if (!world.isClientSide) {
                    world.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, blockpos, 0);
                }
                CampfireBlock.dowse(player, world, blockpos, blockstate);
                resultToSet = blockstate.setValue(CampfireBlock.LIT, false);
            }
            if (resultToSet == null) {
                return InteractionResult.PASS;
            }
        }
        if (!world.isClientSide) {
            ItemStack stack = context.getItemInHand();
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockpos, stack);
            }
            world.setBlock(blockpos, resultToSet, Block.UPDATE_ALL_IMMEDIATE);
            if (player != null) {
                stack.hurtAndBreak(1, player, onBroken -> onBroken.broadcastBreakEvent(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Nullable
    private BlockState useAsAxe(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState resultToSet = state.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
        if (resultToSet != null) {
            world.playSound(player, blockpos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return resultToSet;
        }
        resultToSet = state.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false);
        if (resultToSet != null) {
            world.playSound(player, blockpos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, blockpos, 0);
            return resultToSet;
        }
        resultToSet = state.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false);
        if (resultToSet != null) {
            world.playSound(player, blockpos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, blockpos, 0);
            return resultToSet;
        }
        return null;
    }

    @Override
    public int getEnchantmentValue() {
        return material.getPaxelEnchantability();
    }

    @NotNull
    @Override
    public Ingredient getRepairMaterial() {
        return getTier().getRepairIngredient();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return material.getPaxelMaxUses();
    }

    @Override
    public boolean canBeDepleted() {
        return material.getPaxelMaxUses() > 0;
    }

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        return slot == EquipmentSlot.MAINHAND ? attributeCache.get() : ImmutableMultimap.of();
    }

    @Override
    public void addToBuilder(ImmutableMultimap.Builder<Attribute, AttributeModifier> builder) {
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", getAttackDamage(), Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", material.getPaxelAtkSpeed(), Operation.ADDITION));
    }
}