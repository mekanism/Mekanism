package mekanism.common.item;

import javax.annotation.Nonnull;
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

    private final BaseTier tier;

    public ItemTierInstaller(BaseTier tier, Properties properties) {
        super(properties.maxStackSize(1));
        this.tier = tier;
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
            if (baseTier != tier && baseTier != BaseTier.ULTIMATE && baseTier != BaseTier.CREATIVE) {
                if (baseTier.ordinal() + 1 != tier.ordinal()) {
                    //TODO: Allow for going past more than a singular tier at once
                    // Will need to check the base tier that it starts at matches the current tier of the machine
                    return ActionResultType.FAIL;
                }
                BlockState upgradeState = upgradeableBlock.upgradeResult(state, tier);
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
                        //TODO: Do we need to remove the block as well instead of just replacing?
                        //world.removeBlock(pos, false);
                        world.setBlockState(pos, upgradeState);
                        TileEntityMekanism upgradedTile = MekanismUtils.getTileEntity(TileEntityMekanism.class, world, pos);
                        if (upgradedTile != null) {
                            upgradedTile.parseUpgradeData(upgradeData);
                            Mekanism.packetHandler.sendUpdatePacket(upgradedTile);
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