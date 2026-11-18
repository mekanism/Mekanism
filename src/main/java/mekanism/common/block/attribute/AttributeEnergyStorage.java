package mekanism.common.block.attribute;

import java.util.Objects;
import java.util.function.LongSupplier;
import mekanism.common.tile.base.TileEntityMekanism;

public record AttributeEnergyStorage(LongSupplier energyStorage) implements Attribute, LongSupplier {

    public static AttributeEnergyStorage validateBlock(TileEntityMekanism tile) {
        Objects.requireNonNull(tile, "Tile cannot be null");
        return Attribute.getOrThrow(tile.getBlockHolder(), AttributeEnergyStorage.class);
    }

    @Override
    public long getAsLong() {
        return energyStorage.getAsLong();
    }
}
