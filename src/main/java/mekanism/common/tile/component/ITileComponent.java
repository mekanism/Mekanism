package mekanism.common.tile.component;

import java.util.List;
import mekanism.common.inventory.container.MekanismContainer;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public interface ITileComponent extends ValueIOSerializable {

    String getComponentKey();

    default void read(ValueInput input) {
        input.readChild(getComponentKey(), this);
    }

    default void write(ValueOutput output) {
        String key = getComponentKey();
        ValueOutput child = output.child(key);
        serialize(child);
        //TODO - 26.2: Do we want to just store it regardless and use output.putChild(getComponentKey(), this)?
        if (child.isEmpty()) {
            output.discard(key);
        }
    }

    default void applyImplicitComponents(DataComponentGetter input) {
    }

    default void collectImplicitComponents(DataComponentMap.Builder builder) {
    }

    default void addRemapEntries(List<DataComponentType<?>> remapEntries) {
    }

    /// Called when the tile is removed, both permanently and during unloads.
    default void invalidate() {
    }

    /// Called when the tile is permanently removed
    default void removed() {
    }

    default void trackForMainContainer(MekanismContainer container) {
    }

    default void addToUpdateTag(ValueOutput output) {
    }

    default void readFromUpdateTag(ValueInput input) {
    }
}