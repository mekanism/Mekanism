package mekanism.common.attachments;

import java.util.Objects;
import java.util.Optional;
import mekanism.api.NBTConstants;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.NBTUtils;
import mekanism.common.util.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
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

    public static BlockData create() {
        return new BlockData(Blocks.AIR.defaultBlockState(), null);
    }

    private BlockState blockState;
    @Nullable
    private CompoundTag blockEntityTag;

    public BlockData(BlockState state, @Nullable BlockEntity blockEntity) {
        this.blockState = state;
        if (blockEntity != null) {
            this.blockEntityTag = blockEntity.saveWithFullMetadata();
        }
    }

    @Deprecated
    public void loadLegacyData(CompoundTag data) {
        //Note: We can't use deserialize as the legacy data doesn't compress the tile tag onto the block state tag
        blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), data.getCompound(NBTConstants.BLOCK_STATE));
        NBTUtils.setCompoundIfPresent(data, "tileTag", nbt -> blockEntityTag = nbt);
    }

    public void placeIntoWorld(Level level, BlockPos pos) {
        //TODO: Note - this will not allow for rotation of the block based on how it is placed direction wise via the removal of
        // the cardboard box and will instead leave it how it was when the box was initially put on
        //Adjust the state based on neighboring blocks to ensure double chests properly become single chests again
        BlockState adjustedState = Block.updateFromNeighbourShapes(blockState, level, pos);
        level.setBlockAndUpdate(pos, adjustedState);
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
}