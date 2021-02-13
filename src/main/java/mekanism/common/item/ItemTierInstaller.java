package mekanism.common.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.TextComponentUtil;
import mekanism.api.tier.BaseTier;
import mekanism.common.Mekanism;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeUpgradeable;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.interfaces.ITierUpgradable;
import mekanism.common.tile.interfaces.ITileDirectional;
import mekanism.common.upgrade.IUpgradeData;
import mekanism.common.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class ItemTierInstaller extends Item {

    @Nullable
    private final BaseTier fromTier;
    @Nonnull
    private final BaseTier toTier;

    public ItemTierInstaller(@Nullable BaseTier fromTier, @Nonnull BaseTier toTier, Properties properties) {
        super(properties);
        this.fromTier = fromTier;
        this.toTier = toTier;
    }

    @Nullable
    public BaseTier getFromTier() {
        return fromTier;
    }

    @Nonnull
    public BaseTier getToTier() {
        return toTier;
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName(@Nonnull ItemStack stack) {
        return TextComponentUtil.build(toTier.getTextColor(), super.getDisplayName(stack));
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
        if (Attribute.has(block, AttributeUpgradeable.class)) {
            AttributeUpgradeable upgradeableBlock = Attribute.get(block, AttributeUpgradeable.class);
            BaseTier baseTier = Attribute.getBaseTier(block);
            if (baseTier == fromTier && baseTier != toTier) {
                BlockState upgradeState = upgradeableBlock.upgradeResult(state, toTier);
                if (state == upgradeState) {
                    return ActionResultType.PASS;
                }
                TileEntity tile = WorldUtils.getTileEntity(world, pos);
                if (tile instanceof ITierUpgradable) {
                    if (tile instanceof TileEntityMekanism && !((TileEntityMekanism) tile).playersUsing.isEmpty()) {
                        return ActionResultType.FAIL;
                    }
                    IUpgradeData upgradeData = ((ITierUpgradable) tile).getUpgradeData();
                    if (upgradeData == null) {
                        if (((ITierUpgradable) tile).canBeUpgraded()) {
                            Mekanism.logger.warn("Got no upgrade data for block {} at position: {} in {} but it said it would be able to provide some.", block, pos, world);
                            return ActionResultType.FAIL;
                        }
                    } else {
                        world.setBlockState(pos, upgradeState);
                        //TODO: Make it so it doesn't have to be a TileEntityMekanism?
                        TileEntityMekanism upgradedTile = WorldUtils.getTileEntity(TileEntityMekanism.class, world, pos);
                        if (upgradedTile == null) {
                            Mekanism.logger.warn("Error upgrading block at position: {} in {}.", pos, world);
                            return ActionResultType.FAIL;
                        } else {
                            if (tile instanceof ITileDirectional && ((ITileDirectional) tile).isDirectional()) {
                                upgradedTile.setFacing(((ITileDirectional) tile).getDirection());
                            }
                            upgradedTile.parseUpgradeData(upgradeData);
                            upgradedTile.sendUpdatePacket();
                            upgradedTile.markDirty();
                            if (!player.isCreative()) {
                                context.getItem().shrink(1);
                            }
                            return ActionResultType.SUCCESS;
                        }
                    }
                }
            }
        }
        return ActionResultType.PASS;
    }
}