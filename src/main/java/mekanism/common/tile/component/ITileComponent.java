package mekanism.common.tile.component;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.nbt.CompoundNBT;

public interface ITileComponent {

    void read(CompoundNBT nbtTags);

    void write(CompoundNBT nbtTags);

    default void invalidate() {
    }

    default void onChunkUnload() {
    }

    default void trackForMainContainer(MekanismContainer container) {
    }

    default void addToUpdateTag(CompoundNBT updateTag) {
    }

    default void readFromUpdateTag(CompoundNBT updateTag) {
    }
}