package mekanism.tools.common.item;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import mekanism.api.MekanismItemAbilities;
import mekanism.tools.common.ToolsTags;
import mekanism.tools.common.material.IPaxelMaterial;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.Nullable;

public class ItemMekanismPaxel extends Item implements IsMekanismTool {

    private static final Set<ItemAbility> PAXEL_ACTIONS = Util.make(new ReferenceOpenHashSet<>(), actions -> {
        actions.add(MekanismItemAbilities.PAXEL_DIG);
        //actions.addAll(ItemAbilities.DEFAULT_PICKAXE_ACTIONS);
        actions.addAll(ItemAbilities.DEFAULT_SHOVEL_ACTIONS);
        actions.addAll(ItemAbilities.DEFAULT_AXE_ACTIONS);
    });

    private final IPaxelMaterial material;

    public ItemMekanismPaxel(IPaxelMaterial material, Item.Properties properties) {
        super(properties
              .tool(
                    material.toToolMaterial(),
                    ToolsTags.Blocks.MINEABLE_WITH_PAXEL,
                    material.getPaxelDamage(),
                    material.getPaxelAtkSpeed(),
                    0
              )
              .durability(material.getPaxelDurability())//must go after Tool
        );
        this.material = material;
    }

    @Override
    public boolean canPerformAction(ItemInstance instance, ItemAbility action) {
        return PAXEL_ACTIONS.contains(action);
    }

    /// {@inheritDoc}
    ///
    /// Merged version of [net.minecraft.world.item.AxeItem#useOn(UseOnContext)] and [net.minecraft.world.item.ShovelItem#useOn(UseOnContext)]
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
            BlockState foundResult = blockstate.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
            if (foundResult != null && world.isEmptyBlock(blockpos.above())) {
                //We can flatten the item as a shovel
                world.playSound(player, blockpos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
                resultToSet = foundResult;
            } else {
                resultToSet = blockstate.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false);
                //We can use the paxel as a shovel to extinguish a campfire
                if (resultToSet != null && !world.isClientSide()) {
                    world.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, blockpos, 0);
                }
            }
            if (resultToSet == null) {
                return InteractionResult.PASS;
            }
        }
        if (!world.isClientSide()) {
            ItemStack stack = context.getItemInHand();
            if (player instanceof ServerPlayer serverPlayer) {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, blockpos, stack);
            }
            world.setBlock(blockpos, resultToSet, Block.UPDATE_ALL_IMMEDIATE);
            world.gameEvent(GameEvent.BLOCK_CHANGE, blockpos, GameEvent.Context.of(player, resultToSet));
            if (player != null) {
                stack.hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private BlockState useAsAxe(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState resultToSet = state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false);
        if (resultToSet != null) {
            world.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return resultToSet;
        }
        resultToSet = state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false);
        if (resultToSet != null) {
            world.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, pos, 0);
            return resultToSet;
        }
        resultToSet = state.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false);
        if (resultToSet != null) {
            world.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, pos, 0);
            return resultToSet;
        }
        return null;
    }
}