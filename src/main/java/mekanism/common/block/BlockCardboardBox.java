package mekanism.common.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.NBTConstants;
import mekanism.common.block.interfaces.IHasTileEntity;
import mekanism.common.block.states.IStateStorage;
import mekanism.common.item.block.ItemBlockCardboardBox;
import mekanism.common.registration.impl.TileEntityTypeRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.TileEntityCardboardBox;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class BlockCardboardBox extends BlockMekanism implements IStateStorage, IHasTileEntity<TileEntityCardboardBox> {

    public BlockCardboardBox() {
        super(BlockBehaviour.Properties.of(Material.WOOL).strength(0.5F, 0.6F));
    }

    @Nonnull
    @Override
    @Deprecated
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand,
          @Nonnull BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        } else if (!canReplace(world, player, pos, state)) {
            return InteractionResult.FAIL;
        }
        if (!world.isClientSide) {
            TileEntityCardboardBox box = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
            if (box != null && box.storedData != null) {
                BlockData data = box.storedData;
                //TODO: Note - this will not allow for rotation of the block based on how it is placed direction wise via the removal of
                // the cardboard box and will instead leave it how it was when the box was initially put on
                world.setBlockAndUpdate(pos, data.blockState);
                if (data.tileTag != null) {
                    data.updateLocation(pos);
                    BlockEntity tile = WorldUtils.getTileEntity(world, pos);
                    if (tile != null) {
                        tile.load(data.tileTag);
                    }
                }
                //TODO: Do we need to call onBlockPlacedBy or not bother given we are setting the blockstate to what it was AND setting any tile data
                //data.blockState.getBlock().onBlockPlacedBy(world, pos, data.blockState, player, new ItemStack(data.block));
                popResource(world, pos, MekanismBlocks.CARDBOARD_BOX.getItemStack());
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    private static boolean canReplace(Level world, Player player, BlockPos pos, BlockState state) {
        //Check if the player is allowed to use the cardboard box in the given position
        if (world.mayInteract(player, pos)) {
            //If they are then check if they can "break" the cardboard block that is in that spot
            if (!MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, state, player))) {
                //If they can then we need to see if they are allowed to "place" the unboxed block in the given position
                //TODO: Once forge fixes https://github.com/MinecraftForge/MinecraftForge/issues/7609 use block snapshots
                // and fire a place event to see if the player is able to "place" the cardboard box
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getCloneItemStack(@Nonnull BlockState state, HitResult target, @Nonnull BlockGetter world, @Nonnull BlockPos pos, Player player) {
        ItemStack itemStack = new ItemStack(this);
        TileEntityCardboardBox tile = WorldUtils.getTileEntity(TileEntityCardboardBox.class, world, pos);
        if (tile == null) {
            return itemStack;
        } else if (tile.storedData != null) {
            ((ItemBlockCardboardBox) itemStack.getItem()).setBlockData(itemStack, tile.storedData);
        }
        return itemStack;
    }

    @Override
    public TileEntityTypeRegistryObject<TileEntityCardboardBox> getTileType() {
        return MekanismTileEntityTypes.CARDBOARD_BOX;
    }

    public static class BlockData {

        @Nonnull
        public final BlockState blockState;
        @Nullable
        public CompoundTag tileTag;

        public BlockData(@Nonnull BlockState blockState) {
            this.blockState = blockState;
        }

        public static BlockData read(CompoundTag nbtTags) {
            BlockData data = new BlockData(NbtUtils.readBlockState(nbtTags.getCompound(NBTConstants.BLOCK_STATE)));
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

        public CompoundTag write(CompoundTag nbtTags) {
            nbtTags.put(NBTConstants.BLOCK_STATE, NbtUtils.writeBlockState(blockState));
            if (tileTag != null) {
                nbtTags.put(NBTConstants.TILE_TAG, tileTag);
            }
            return nbtTags;
        }
    }
}