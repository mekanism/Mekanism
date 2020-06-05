package mekanism.common.capabilities.holder.heat;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.RelativeSide;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.util.Direction;

public class HeatCapacitorHelper {

    private final IHeatCapacitorHolder slotHolder;
    private boolean built;

    private HeatCapacitorHelper(IHeatCapacitorHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static HeatCapacitorHelper forSide(Supplier<Direction> facingSupplier) {
        return new HeatCapacitorHelper(new HeatCapacitorHolder(facingSupplier));
    }

    public static HeatCapacitorHelper forSideWithConfig(Supplier<Direction> facingSupplier, Supplier<TileComponentConfig> configSupplier) {
        return new HeatCapacitorHelper(new ConfigHeatCapacitorHolder(facingSupplier, configSupplier));
    }

    public void addCapacitor(@Nonnull IHeatCapacitor capacitor) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof HeatCapacitorHolder) {
            ((HeatCapacitorHolder) slotHolder).addCapacitor(capacitor);
        } else if (slotHolder instanceof ConfigHeatCapacitorHolder) {
            ((ConfigHeatCapacitorHolder) slotHolder).addCapacitor(capacitor);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add capacitors");
        }
    }

    public void addCapacitor(@Nonnull IHeatCapacitor container, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof HeatCapacitorHolder) {
            ((HeatCapacitorHolder) slotHolder).addCapacitor(container, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add capacitors on specific sides");
        }
    }

    public IHeatCapacitorHolder build() {
        built = true;
        return slotHolder;
    }
}
