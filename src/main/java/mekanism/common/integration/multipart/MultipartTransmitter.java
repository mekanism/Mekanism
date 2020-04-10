/*package mekanism.common.integration.multipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import mekanism.common.MekanismBlock;
import mekanism.api.tier.BaseTier;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class MultipartTransmitter implements IMultipart {

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, BlockState state, Direction facing, float hitX, float hitY, float hitZ, LivingEntity placer) {
        return EnumCenterSlot.CENTER;
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockReader world, BlockPos pos, BlockState state) {
        return EnumCenterSlot.CENTER;
    }

    @Override
    public void onAdded(IPartInfo part) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntitySidedPipe) {
            ((TileEntitySidedPipe) tile).onAdded();
        }
    }

    @Override
    public void onPartAdded(IPartInfo part, IPartInfo otherPart) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntitySidedPipe) {
            tile.validate();
            ((TileEntitySidedPipe) tile).notifyTileChange();
        }
    }

    @Override
    public void onPartChanged(IPartInfo part, IPartInfo otherPart) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntitySidedPipe) {
            ((TileEntitySidedPipe) tile).onPartChanged(otherPart.getPart());
        }
    }

    @Override
    public void onPartHarvested(IPartInfo part, PlayerEntity player) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntitySidedPipe) {
            BlockState partState = part.getState();
            partState.getBlock().removedByPlayer(partState, part.getPartWorld(), part.getContainer().getPartPos(), player, true);
        }
        IMultipart.super.onPartHarvested(part, player);
    }

    @Override
    public void onPartPlacedBy(IPartInfo part, LivingEntity placer, ItemStack stack) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntitySidedPipe) {
            BaseTier baseTier = BaseTier.BASIC;
            if (stack.hasTag()) {
                baseTier = EnumUtils.BASE_TIERS[stack.getTag().getInt("tier")];
            }
            ((TileEntitySidedPipe) tile).setBaseTier(baseTier);
        }
    }

    @Override
    public Block getBlock() {
        //TODO
        return MekanismBlock.BASIC_UNIVERSAL_CABLE.getBlock();
    }
}*/