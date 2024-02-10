package mekanism.common.tile.component;

import mekanism.common.inventory.container.MekanismContainer;
import mekanism.common.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;

public interface ITileComponent {

    String getComponentKey();

    default void read(CompoundTag nbtTags) {
        NBTUtils.setCompoundIfPresent(nbtTags, getComponentKey(), this::deserialize);
    }

    default void write(CompoundTag nbtTags) {
        CompoundTag componentTag = serialize();
        if (!componentTag.isEmpty()) {
            nbtTags.put(getComponentKey(), componentTag);
        }
    }

    void deserialize(CompoundTag componentTag);

    CompoundTag serialize();

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