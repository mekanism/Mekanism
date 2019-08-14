/*package mekanism.common.integration.multipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumFaceSlot;
import mcmultipart.api.slot.IPartSlot;
import mekanism.common.MekanismBlock;
import mekanism.common.block.BlockGlowPanel;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.tile.TileEntityGlowPanel;
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

public class MultipartGlowPanel implements IMultipart {

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, BlockState state, Direction facing, float hitX, float hitY, float hitZ, LivingEntity placer) {
        return EnumFaceSlot.values()[facing.ordinal()];
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockReader world, BlockPos pos, BlockState state) {
        return EnumFaceSlot.values()[state.get(BlockStateHelper.facingProperty).ordinal()];
    }

    @Override
    public void onPartPlacedBy(IPartInfo part, LivingEntity placer, ItemStack stack) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntityGlowPanel) {
            Direction facing = Direction.values()[((EnumFaceSlot) part.getSlot()).ordinal()];
            //EnumColor col = EnumColor.DYES[stack.getDamage()];
            TileEntityGlowPanel glowPanel = (TileEntityGlowPanel) tile;
            glowPanel.setOrientation(facing);
            //TODO: Is this needed or can it work fine due to block. Look at once multipart is reimplemented
            //glowPanel.setColor(col);
        }
    }

    @Override
    public void onPartRemoved(IPartInfo part, IPartInfo otherPart) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntityGlowPanel && !BlockGlowPanel.canStay(part.getPartWorld(), part.getPartPos())) {
            BlockPos pos = part.getPartPos();
            //TileEntityGlowPanel glowPanel = (TileEntityGlowPanel) tile;
            //TODO: Get correct color if it is unaware of it the tile will have to store it
            ItemStack stack = MekanismBlock.BLACK_GLOW_PANEL.getItemStack();
            Block.spawnAsEntity(part.getActualWorld(), pos, stack);
            part.remove();
        }
    }

    @Override
    public void onPartHarvested(IPartInfo part, PlayerEntity player) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntityGlowPanel) {
            BlockState partState = part.getState();
            partState.getBlock().removedByPlayer(partState, part.getPartWorld(), part.getContainer().getPartPos(), player, true);
        }
        IMultipart.super.onPartHarvested(part, player);
    }

    @Override
    public Block getBlock() {
        //TODO
        return MekanismBlock.BLACK_GLOW_PANEL.getBlock();
    }
}*/