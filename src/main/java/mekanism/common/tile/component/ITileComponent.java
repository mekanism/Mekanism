package mekanism.common.tile.component;

import java.util.List;
import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.util.NBTUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public interface ITileComponent {

    String getComponentKey();

    default void read(CompoundTag nbtTags, HolderLookup.Provider provider) {
        NBTUtils.setCompoundIfPresent(nbtTags, getComponentKey(), tag -> deserialize(tag, provider));
    }

    default void write(CompoundTag nbtTags, HolderLookup.Provider provider) {
        CompoundTag componentTag = serialize(provider);
        if (!componentTag.isEmpty()) {
            nbtTags.put(getComponentKey(), componentTag);
        }
    }

    void deserialize(CompoundTag componentTag, HolderLookup.Provider provider);

    CompoundTag serialize(HolderLookup.Provider provider);

    default void applyImplicitComponents(@NotNull BlockEntity.DataComponentInput input) {
    }

    default void collectImplicitComponents(DataComponentMap.Builder builder) {
    }

    default void addRemapEntries(List<DataComponentType<?>> remapEntries) {
    }

    /**
     * Called when the tile is removed, both permanently and during unloads.
     */
    default void invalidate() {
    }

    /**
     * Called when the tile is permanently removed
     */
    default void removed() {
    }

    default void trackForMainContainer(MekanismContainer container) {
    }

    default void addToUpdateTag(CompoundTag updateTag) {
    }

    default void readFromUpdateTag(CompoundTag updateTag) {
    }
}