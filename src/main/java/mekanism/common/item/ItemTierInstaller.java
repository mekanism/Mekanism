package mekanism.common.item;

import java.util.Optional;
import mekanism.api.MekanismAPITags;
import mekanism.api.security.IBlockSecurityUtils;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeHasBounding;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemTierInstaller extends Item {

    @Nullable
    private final BaseTier fromTier;
    @NotNull
    private final BaseTier toTier;

    public ItemTierInstaller(@Nullable BaseTier fromTier, @NotNull BaseTier toTier, Properties properties) {
        super(properties);
        this.fromTier = fromTier;
        this.toTier = toTier;
    }

    @Nullable
    public BaseTier getFromTier() {
        return fromTier;
    }

    @NotNull
    public BaseTier getToTier() {
        return toTier;
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        return TextComponentUtil.build(toTier.getColor(), super.getName(stack));
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (state.is(MekanismBlocks.BOUNDING_BLOCK)) {
            BlockPos mainPos = BlockBounding.getMainBlockPos(world, pos);
            if (mainPos != null) {
                //If we are a bounding block with a main pos, update to pretend we interacted with the logic block
                pos = mainPos;
                state = world.getBlockState(mainPos);
            }
        }
        if (!IBlockSecurityUtils.INSTANCE.canAccessOrDisplayError(player, world, pos) || state.is(MekanismAPITags.Blocks.BLACKLIST_INSTALLER_UPGRADEABLE)) {
            return InteractionResult.FAIL;
        } else if (world.isClientSide()) {
            return InteractionResult.PASS;
        }
        Holder<Block> block = state.getBlockHolder();
        AttributeUpgradeable upgradeableBlock = Attribute.get(block, AttributeUpgradeable.class);
        if (upgradeableBlock != null) {
            BaseTier baseTier = Attribute.getBaseTier(block);
            if (baseTier == fromTier && baseTier != toTier) {
                BlockState upgradeState = upgradeableBlock.upgradeResult(state, toTier);
                if (state == upgradeState) {
                    return InteractionResult.PASS;
                }
                BlockEntity tile = WorldUtils.getTileEntity(world, pos);
                if (tile instanceof ITierUpgradable tierUpgradable) {
                    if (tile instanceof TileEntityMekanism tileMek && !tileMek.playersUsing.isEmpty()) {
                        return InteractionResult.FAIL;
                    }
                    IUpgradeData upgradeData = tierUpgradable.getUpgradeData(world.registryAccess());
                    if (upgradeData == null) {
                        if (tierUpgradable.canBeUpgraded()) {
                            Mekanism.logger.warn("Got no upgrade data for block {} at position: {} in {} but it said it would be able to provide some.", block, pos, world.dimension().location());
                            return InteractionResult.FAIL;
                        }
                    } else {
                        AttributeHasBounding upgradeBounding = Attribute.get(upgradeState, AttributeHasBounding.class);
                        //If the resulting block has bounding blocks, validate that all of them will be able to be placed
                        if (upgradeBounding != null && !upgradeBounding.handle(world, pos, upgradeState, pos, (level, boundingPos, mainPos) -> {
                            Optional<BlockState> blockState = WorldUtils.getBlockState(level, boundingPos);
                            if (blockState.isPresent()) {
                                BlockState boundingCurrentState = blockState.get();
                                if (boundingCurrentState.canBeReplaced()) {
                                    return true;
                                } else if (boundingCurrentState.is(MekanismBlocks.BOUNDING_BLOCK)) {
                                    //Treat bounding blocks that will be removed because they are actually part of the unupgraded multiblock as valid
                                    // for us to put a new bounding block in
                                    return mainPos.equals(BlockBounding.getMainBlockPos(level, boundingPos));
                                }
                            }
                            return false;
                        })) {
                            //At least one bounding block we would be adding can't be placed. Error out instead of upgrading the block
                            return InteractionResult.FAIL;
                        }
                        //Update the block
                        if (!world.setBlockAndUpdate(pos, upgradeState)) {
                            //Something went wrong, bail rather than trying to
                            Mekanism.logger.warn("Error upgrading block at position: {} in {}.", pos, world.dimension().location());
                            return InteractionResult.FAIL;
                        }
                        //Place any bounding blocks the new state may have
                        if (upgradeBounding != null) {
                            upgradeBounding.placeBoundingBlocks(world, pos, upgradeState);
                        }
                        TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
                        if (upgradedTile == null) {
                            Mekanism.logger.warn("Error upgrading block at position: {} in {}. Expected a mekanism block as the result.", pos, world.dimension().location());
                            return InteractionResult.FAIL;
                        }
                        //TODO: Make it so it doesn't have to be a TileEntityMekanism in order to do these things?
                        if (tile instanceof ITileDirectional directional && directional.isDirectional()) {
                            upgradedTile.setFacing(directional.getDirection(), false);
                        }
                        upgradedTile.parseUpgradeData(world.registryAccess(), upgradeData);
                        upgradedTile.resyncMasterToBounding();
                        upgradedTile.sendUpdatePacket();
                        upgradedTile.setChanged();
                        //Notify the level that the caps at the position are no longer valid
                        // In general replacing the tile likely will have caused this to be invalidated
                        // but mark it just to be safe, and in case there are any bounding blocks so that they notify the level their caps might have changed
                        upgradedTile.invalidateCapabilitiesFull();
                        if (!player.isCreative()) {
                            context.getItemInHand().shrink(1);
                        }
                        if (player instanceof ServerPlayer serverPlayer) {
                            MekanismCriteriaTriggers.USE_TIER_INSTALLER.value().trigger(serverPlayer, toTier);
                        }
                        return InteractionResult.CONSUME;
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}