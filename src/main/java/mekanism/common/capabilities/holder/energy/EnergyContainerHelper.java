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

    //TODO: View usages and determine which ones if any should be moved to having a config
    public static EnergyContainerHelper forSide(Supplier<Direction> facingSupplier) {
        return new EnergyContainerHelper(new EnergyContainerHolder(facingSupplier));
    }

    public static EnergyContainerHelper forSideWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new EnergyContainerHelper(new ConfigEnergyContainerHolder(facingSupplier, configSupplier));
    }

    public void addContainer(@Nonnull IEnergyContainer container) {
        if (built) {
            throw new RuntimeException("Builder has already built.");
        }
        if (slotHolder instanceof EnergyContainerHolder) {
            ((EnergyContainerHolder) slotHolder).addContainer(container);
        } else if (slotHolder instanceof ConfigEnergyContainerHolder) {
            ((ConfigEnergyContainerHolder) slotHolder).addContainer(container);
        }
        //TODO: Else warning?
    }

    public void addContainer(@Nonnull IEnergyContainer container, RelativeSide... sides) {
        if (built) {
            throw new RuntimeException("Builder has already built.");
        }
        if (slotHolder instanceof EnergyContainerHolder) {
            ((EnergyContainerHolder) slotHolder).addContainer(container, sides);
        }
        //TODO: Else warning?
    }

    public IEnergyContainerHolder build() {
        built = true;
        return slotHolder;
    }
}