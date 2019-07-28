package mekanism.common.integration.multipart;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;
import mekanism.common.MekanismBlock;
import mekanism.common.tier.BaseTier;
import mekanism.common.tile.transmitter.TileEntitySidedPipe;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MultipartTransmitter implements IMultipart {

    @Override
    public IPartSlot getSlotForPlacement(World world, BlockPos pos, IBlockState state, EnumFacing facing, float hitX, float hitY, float hitZ, EntityLivingBase placer) {
        return EnumCenterSlot.CENTER;
    }

    @Override
    public IPartSlot getSlotFromWorld(IBlockAccess world, BlockPos pos, IBlockState state) {
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
    public void onPartHarvested(IPartInfo part, EntityPlayer player) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntitySidedPipe) {
            IBlockState partState = part.getState();
            partState.getBlock().removedByPlayer(partState, part.getPartWorld(), part.getContainer().getPartPos(), player, true);
        }
        IMultipart.super.onPartHarvested(part, player);
    }

    @Override
    public void onPartPlacedBy(IPartInfo part, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = part.getTile().getTileEntity();
        if (tile instanceof TileEntitySidedPipe) {
            BaseTier baseTier = BaseTier.BASIC;
            if (stack.hasTagCompound()) {
                baseTier = BaseTier.values()[stack.getTagCompound().getInteger("tier")];
            }
            ((TileEntitySidedPipe) tile).setBaseTier(baseTier);
        }
    }

    @Override
    public Block getBlock() {
        //TODO
        return MekanismBlock.BASIC_UNIVERSAL_CABLE.getBlock();
    }
}