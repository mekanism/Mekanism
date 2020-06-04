package mekanism.common.capabilities.holder.energy;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;

public class EnergyContainerHelper {

    private final IEnergyContainerHolder slotHolder;
    private boolean built;

    private EnergyContainerHelper(IEnergyContainerHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static EnergyContainerHelper forSide(Supplier<Direction> facingSupplier) {
        return new EnergyContainerHelper(new EnergyContainerHolder(facingSupplier));
    }

    public static EnergyContainerHelper forSideWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new EnergyContainerHelper(new ConfigEnergyContainerHolder(facingSupplier, configSupplier));
    }

    public void addContainer(@Nonnull IEnergyContainer container) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof EnergyContainerHolder) {
            ((EnergyContainerHolder) slotHolder).addContainer(container);
        } else if (slotHolder instanceof ConfigEnergyContainerHolder) {
            ((ConfigEnergyContainerHolder) slotHolder).addContainer(container);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add containers");
        }
    }

    public void addContainer(@Nonnull IEnergyContainer container, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof EnergyContainerHolder) {
            ((EnergyContainerHolder) slotHolder).addContainer(container, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add containers on specific sides");
        }
    }

    public IEnergyContainerHolder build() {
        built = true;
        return slotHolder;
    }
}