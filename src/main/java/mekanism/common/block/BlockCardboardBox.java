package mekanism.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.IStateStorage;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.NBTUtils;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockCardboardBox extends BlockMekanism implements IStateStorage, IHasTileEntity<TileEntityCardboardBox> {

    public BlockCardboardBox() {
        super(AbstractBlock.Properties.create(Material.WOOL).hardnessAndResistance(0.5F, 1F));
    }

    @Nonnull
    @Override
    @Deprecated
    public ActionResultType onBlockActivated(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand hand,
          @Nonnull BlockRayTraceResult hit) {
        if (!world.isRemote && player.isSneaking()) {
            TileEntityCardboardBox box = MekanismUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
            if (box != null && box.storedData != null) {
                BlockData data = box.storedData;
                //TODO: Note - this will not allow for rotation of the block based on how it is placed direction wise via the removal of
                // the cardboard box and will instead leave it how it was when the box was initially put on
                world.setBlockState(pos, data.blockState);
                if (data.tileTag != null) {
                    data.updateLocation(pos);
                    TileEntity tile = MekanismUtils.getTileEntity(world, pos);
                    if (tile != null) {
                        tile.read(state, data.tileTag);
                    }
                }
                //TODO: Do we need to call onBlockPlacedBy or not bother given we are setting the blockstate to what it was AND setting any tile data
                //data.blockState.getBlock().onBlockPlacedBy(world, pos, data.blockState, player, new ItemStack(data.block));
                spawnAsEntity(world, pos, MekanismBlocks.CARDBOARD_BOX.getItemStack());
            }
        }
        return player.isSneaking() ? ActionResultType.SUCCESS : ActionResultType.PASS;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull BlockState state, RayTraceResult target, @Nonnull IBlockReader world, @Nonnull BlockPos pos, PlayerEntity player) {
        ItemStack itemStack = new ItemStack(this);
        TileEntityCardboardBox tile = MekanismUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
        if (tile == null) {
            return itemStack;
        }
        if (tile.storedData != null) {
            ((ItemBlockCardboardBox) itemStack.getItem()).setBlockData(itemStack, tile.storedData);
        }
        return itemStack;
    }

    @Override
    public TileEntityType<TileEntityCardboardBox> getTileType() {
        return MekanismTileEntityTypes.CARDBOARD_BOX.getTileEntityType();
    }

    public static class BlockData {

        @Nonnull
        public final BlockState blockState;
        @Nullable
        public CompoundNBT tileTag;

        public BlockData(@Nonnull BlockState blockState) {
            this.blockState = blockState;
        }

        public static BlockData read(CompoundNBT nbtTags) {
            BlockData data = new BlockData(NBTUtil.readBlockState(nbtTags.getCompound(NBTConstants.BLOCK_STATE)));
            NBTUtils.setCompoundIfPresent(nbtTags, NBTConstants.TILE_TAG, nbt -> data.tileTag = nbt);
            return data;
        }

        public void updateLocation(BlockPos pos) {
            if (tileTag != null) {
                tileTag.putInt(NBTConstants.X, pos.getX());
                tileTag.putInt(NBTConstants.Y, pos.getY());
                tileTag.putInt(NBTConstants.Z, pos.getZ());
            }
        }

        public CompoundNBT write(CompoundNBT nbtTags) {
            nbtTags.put(NBTConstants.BLOCK_STATE, NBTUtil.writeBlockState(blockState));
            if (tileTag != null) {
                nbtTags.put(NBTConstants.TILE_TAG, tileTag);
            }
            return nbtTags;
        }
    }
}