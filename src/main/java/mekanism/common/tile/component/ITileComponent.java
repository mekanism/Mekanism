package mekanism.common.tile.component;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.nbt.CompoundTag;

public interface ITileComponent {

    void read(CompoundTag nbtTags);

    void write(CompoundTag nbtTags);

    default void invalidate() {
    }

    default void onChunkUnload() {
    }

    default void trackForMainContainer(MekanismContainer container) {
    }

    default void addToUpdateTag(CompoundTag updateTag) {
    }

    default void readFromUpdateTag(CompoundTag updateTag) {
    }
}