package mekanism.common.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.common.Mekanism;
import mekanism.common.block.interfaces.IUpgradeableBlock;
import mekanism.common.tier.BaseTier;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTierInstaller extends Item {

    @Nullable
    private final BaseTier fromTier;
    private final BaseTier toTier;

    public ItemTierInstaller(@Nullable BaseTier fromTier, BaseTier toTier, Properties properties) {
        super(properties.maxStackSize(1));
        this.fromTier = fromTier;
        this.toTier = toTier;
    }

    @Nonnull
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        if (world.isRemote || player == null) {
            return ActionResultType.PASS;
        }
        BlockPos pos = context.getPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof IUpgradeableBlock) {
            IUpgradeableBlock<?> upgradeableBlock = (IUpgradeableBlock<?>) block;
            BaseTier baseTier = upgradeableBlock.getTier().getBaseTier();
            //TODO: Allow base tier ot be null if the upgradeableblock is not a tiered one?
            if (baseTier == fromTier && baseTier != toTier && baseTier != BaseTier.ULTIMATE && baseTier != BaseTier.CREATIVE) {
                BlockState upgradeState = upgradeableBlock.upgradeResult(state, toTier);
                if (state == upgradeState) {
                    return ActionResultType.PASS;
                }
                //TODO: Make it so it doesn't have to be a TileEntityMekanism?
                TileEntityMekanism tile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
                if (tile != null) {
                    if (tile.playersUsing.size() > 0) {
                        return ActionResultType.FAIL;
                    }
                    IUpgradeData upgradeData = tile.getUpgradeData();
                    if (upgradeData != null) {
                        world.setBlockState(pos, upgradeState);
                        TileEntityMekanism upgradedTile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
                        if (upgradedTile != null) {
                            upgradedTile.parseUpgradeData(upgradeData);
                            Mekanism.packetHandler.sendUpdatePacket(upgradedTile);
                            upgradedTile.markDirty();
                            if (!player.isCreative()) {
                                ItemStack stack = player.getHeldItem(context.getHand());
                                stack.shrink(1);
                            }
                            return ActionResultType.SUCCESS;
                        } else {
                            Mekanism.logger.warn("Error upgrading block at position: {} in {}.", pos, world);
                            return ActionResultType.FAIL;
                        }
                    } else if (tile.canBeUpgraded()) {
                        Mekanism.logger.warn("Got no upgrade data for block {} at position: {} in {} but it said it would be able to provide some.", block, pos, world);
                        return ActionResultType.FAIL;
                    }
                }
            }
        }
        return ActionResultType.PASS;
    }
}