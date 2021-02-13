package mekanism.common.tile.component;

import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.nbt.CompoundNBT;

public interface ITileComponent {

    void tick();

    void read(CompoundNBT nbtTags);

    void write(CompoundNBT nbtTags);

    default void invalidate() {
    }

    default void onChunkUnload() {
    }

    default void trackForMainContainer(MekanismContainer container) {
    }

    void addToUpdateTag(CompoundNBT updateTag);

    void readFromUpdateTag(CompoundNBT updateTag);
}