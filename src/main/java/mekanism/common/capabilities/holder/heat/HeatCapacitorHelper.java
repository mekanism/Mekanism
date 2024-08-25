package mekanism.common.capabilities.holder.heat;

import java.util.function.Supplier;
import mekanism.api.RelativeSide;
import mekanism.api.heat.IHeatCapacitor;
import mekanism.common.tile.interfaces.ISideConfiguration;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;

public class HeatCapacitorHelper {

    private final IHeatCapacitorHolder slotHolder;
    private boolean built;

    private HeatCapacitorHelper(IHeatCapacitorHolder slotHolder) {
        this.slotHolder = slotHolder;
    }

    public static HeatCapacitorHelper forSide(Supplier<Direction> facingSupplier) {
        return new HeatCapacitorHelper(new HeatCapacitorHolder(facingSupplier));
    }

    public static HeatCapacitorHelper forSideWithConfig(ISideConfiguration sideConfiguration) {
        return new HeatCapacitorHelper(new ConfigHeatCapacitorHolder(sideConfiguration));
    }

    public <CAPACITOR extends IHeatCapacitor> CAPACITOR addCapacitor(@NotNull CAPACITOR capacitor) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof HeatCapacitorHolder slotHolder) {
            slotHolder.addCapacitor(capacitor);
        } else if (slotHolder instanceof ConfigHeatCapacitorHolder slotHolder) {
            slotHolder.addCapacitor(capacitor);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add capacitors");
        }
        return capacitor;
    }

    public <CAPACITOR extends IHeatCapacitor> CAPACITOR addCapacitor(@NotNull CAPACITOR capacitor, RelativeSide... sides) {
        if (built) {
            throw new IllegalStateException("Builder has already built.");
        }
        if (slotHolder instanceof HeatCapacitorHolder slotHolder) {
            slotHolder.addCapacitor(capacitor, sides);
        } else {
            throw new IllegalArgumentException("Holder does not know how to add capacitors on specific sides");
        }
        return capacitor;
    }

    public IHeatCapacitorHolder build() {
        built = true;
        return slotHolder;
    }
}
