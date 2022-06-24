package mekanism.common.tile.component;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.nbt.CompoundTag;

public interface ITileComponent {

    void read(CompoundTag nbtTags);

    void write(CompoundTag nbtTags);

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