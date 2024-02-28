package mekanism.common.attachments;

import java.util.Objects;
import java.util.Optional;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.config.MekanismConfig;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public final class BlockData implements INBTSerializable<CompoundTag> {

    @Deprecated
    public static BlockData createWithLegacy(IAttachmentHolder attachmentHolder) {
        BlockData blockData = create();
        //TODO - 1.21: Remove this legacy way of loading data
        if (attachmentHolder instanceof ItemStack stack && !stack.isEmpty()) {
            ItemDataUtils.getAndRemoveData(stack, NBTConstants.DATA, CompoundTag::getCompound).ifPresent(blockData::loadLegacyData);
        }
        return blockData;
    }

    public static BlockData create(BlockState state, @Nullable BlockEntity blockEntity) {
        return new BlockData(state, blockEntity == null ? null : blockEntity.saveWithFullMetadata());
    }

    public static BlockData create() {
        return new BlockData(Blocks.AIR.defaultBlockState(), null);
    }

    private BlockState blockState;
    @Nullable
    private CompoundTag blockEntityTag;

    private BlockData(BlockState state, @Nullable CompoundTag blockEntityTag) {
        this.blockState = state;
        this.blockEntityTag = blockEntityTag;
    }

    @Deprecated
    public void loadLegacyData(CompoundTag data) {
        //Note: We can't use deserialize as the legacy data doesn't compress the tile tag onto the block state tag
        blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), data.getCompound(NBTConstants.BLOCK_STATE));
        NBTUtils.setCompoundIfPresent(data, "tileTag", nbt -> blockEntityTag = nbt);
    }

    public boolean tryPlaceIntoWorld(Level level, BlockPos pos, @Nullable Player player) {
        //TODO: Note - this will not allow for rotation of the block based on how it is placed direction wise via the removal of
        // the cardboard box and will instead leave it how it was when the box was initially put on
        //Adjust the state based on neighboring blocks to ensure double chests properly become single chests again
        BlockState adjustedState = Block.updateFromNeighbourShapes(blockState, level, pos);

        FluidState fluidState = adjustedState.getFluidState();
        FluidType fluidType = fluidState.getFluidType();
        //Note: Doesn't support nbt
        FluidStack fluid = new FluidStack(fluidState.getType(), FluidType.BUCKET_VOLUME);
        BucketPickup tryPickup = null;
        //Do our best effort to support to not allow water to be placed into the nether
        if (fluidType.isVaporizedOnPlacement(level, pos, fluid)) {
            if (!MekanismConfig.general.strictUnboxing.get() && adjustedState.getBlock() instanceof BucketPickup pickup) {
                tryPickup = pickup;
            } else {
                //Not a bucket pickup, we don't know how to pick up the block
                return false;
            }
        }

        level.setBlockAndUpdate(pos, adjustedState);
        //TODO: Do we need to call setPlacedBy or not bother given we are setting the blockstate to what it was AND setting any tile data
        //adjustedState.getBlock().setPlacedBy(world, pos, blockState, player, new ItemStack(adjustedState.getBlock()));
        if (blockEntityTag != null) {
            //Update the location
            blockEntityTag.putInt(NBTConstants.X, pos.getX());
            blockEntityTag.putInt(NBTConstants.Y, pos.getY());
            blockEntityTag.putInt(NBTConstants.Z, pos.getZ());
            //And get the block entity and load it from the data
            BlockEntity tile = WorldUtils.getTileEntity(level, pos);
            if (tile != null) {
                tile.load(blockEntityTag);
            }
        }
        if (tryPickup != null) {
            if (!tryPickup.pickupBlock(player, level, pos, adjustedState).isEmpty()) {
                fluidType.onVaporize(null, level, pos, fluid);
            }
        }
        return true;
    }

    public Block getBlock() {
        return blockState.getBlock();
    }

    public boolean hasBlockEntity() {
        return blockEntityTag != null;
    }

    public Optional<String> getBlockEntityName() {
        return Optional.ofNullable(blockEntityTag)
              .filter(tag -> tag.contains(NBTConstants.ID, Tag.TAG_STRING))
              .map(tag -> tag.getString(NBTConstants.ID));
    }

    public boolean isCompatible(BlockData other) {
        if (other == this) {
            return true;
        }
        return blockState == other.blockState && Objects.equals(blockEntityTag, other.blockEntityTag);
    }

    @Nullable
    @Override
    public CompoundTag serializeNBT() {
        if (blockState.isAir()) {
            return null;
        }
        CompoundTag nbt = NbtUtils.writeBlockState(blockState);
        if (blockEntityTag != null) {
            nbt.put(NBTConstants.BE_TAG, blockEntityTag);
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt);
        NBTUtils.setCompoundIfPresent(nbt, NBTConstants.BE_TAG, tag -> blockEntityTag = tag);
    }

    @Nullable
    public BlockData copy(IAttachmentHolder holder) {
        if (blockState.isAir()) {
            return null;
        }
        return new BlockData(blockState, blockEntityTag == null ? null : blockEntityTag.copy());
    }
}