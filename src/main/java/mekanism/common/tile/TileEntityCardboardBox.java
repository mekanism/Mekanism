package mekanism.common.tile;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.Error;
import java.util.List;
import java.util.Optional;
import mekanism.common.Mekanism;
import mekanism.common.attachments.BlockData;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.base.TileEntityUpdateable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

//TODO: If we end up with more blocks where we care about the actual backing component, we should abstract some of this up into TileEntityUpdateable
public class TileEntityCardboardBox extends TileEntityUpdateable {

    public TileEntityCardboardBox(BlockPos pos, BlockState state) {
        super(MekanismTileEntityTypes.CARDBOARD_BOX, pos, state);
    }

    @Override
    public List<DataComponentType<?>> getRemapEntries() {
        List<DataComponentType<?>> remapEntries = super.getRemapEntries();
        if (!remapEntries.contains(MekanismDataComponents.BLOCK_DATA.get())) {
            remapEntries.add(MekanismDataComponents.BLOCK_DATA.get());
        }
        return remapEntries;
    }

    @NotNull
    @Override
    public CompoundTag getReducedUpdateTag(@NotNull HolderLookup.Provider provider) {
        CompoundTag updateTag = super.getReducedUpdateTag(provider);
        DataComponentType<BlockData> componentType = MekanismDataComponents.BLOCK_DATA.get();
        if (components().has(componentType)) {
            DataResult<Tag> encoded = componentType.codecOrThrow().encodeStart(
                  provider.createSerializationContext(NbtOps.INSTANCE), components().get(componentType)
            );
            Optional<Error<Tag>> error = encoded.error();
            if (error.isPresent()) {
                Mekanism.logger.error("Failed to encode cardboard box block data: {}", error.get().message());
            } else {
                updateTag.put(MekanismDataComponents.BLOCK_DATA.getId().toString(), encoded.getOrThrow());
            }
        }
        return updateTag;
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider provider) {
        super.handleUpdateTag(tag, provider);
        String key = MekanismDataComponents.BLOCK_DATA.getId().toString();
        if (tag.contains(key)) {
            DataComponentType<BlockData> componentType = MekanismDataComponents.BLOCK_DATA.get();
            DataResult<Pair<BlockData, Tag>> decoded = componentType.codecOrThrow().decode(
                  provider.createSerializationContext(NbtOps.INSTANCE), tag.get(key)
            );
            Optional<Error<Pair<BlockData, Tag>>> error = decoded.error();
            if (error.isPresent()) {
                Mekanism.logger.error("Failed to decode cardboard box block data: {}", error.get().message());
            } else {
                BlockData data = decoded.getOrThrow().getFirst();
                setComponents(DataComponentMap.builder()
                      .addAll(components())
                      .set(MekanismDataComponents.BLOCK_DATA, data)
                      .build());
            }
        }
    }
}