package mekanism.common.capabilities.holder.energy;

import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.energy.IEnergyContainer;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class EnergyContainerHelper {

    private final IEnergyContainerHolder slotHolder;
    private boolean built;

    private EnergyContainerHelper(IEnergyContainerHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static EnergyContainerHelper forSide(Supplier<Direction> facingSupplier) {
        return new EnergyContainerHelper(new EnergyContainerHolder(facingSupplier));
    }

    public static EnergyContainerHelper forSideWithConfig(ISideConfiguration sideConfiguration) {
        return new EnergyContainerHelper(new ConfigEnergyContainerHolder(sideConfiguration));
    }

    public <CONTAINER extends IEnergyContainer> CONTAINER addContainer(@NotNull CONTAINER container) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof EnergyContainerHolder slotHolder) {
            slotHolder.addContainer(container);
        } else if (slotHolder instanceof ConfigEnergyContainerHolder slotHolder) {
            slotHolder.addContainer(container);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add containers");
        }
        return container;
    }

    public <CONTAINER extends IEnergyContainer> CONTAINER addContainer(@NotNull CONTAINER container, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof EnergyContainerHolder slotHolder) {
            slotHolder.addContainer(container, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add containers on specific sides");
        }
        return container;
    }

    public IEnergyContainerHolder build() {
        built = true;
        return slotHolder;
    }
}