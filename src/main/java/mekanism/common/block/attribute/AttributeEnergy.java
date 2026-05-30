package mekanism.common.block.attribute;

import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import mekanism.api.functions.ConstantPredicates;
import mekanism.api.math.MathUtils;
import org.jetbrains.annotations.Nullable;

//TODO: Eventually we may want to make these suppliers be used more like suppliers in that:
// if the config updates it doesn't require a server restart (or chunk reload to take effect
public class AttributeEnergy implements Attribute {

    private IntSupplier energyUsage = ConstantPredicates.ZERO;
    // 2 operations (20 secs) worth of ticks * usage
    //TODO - 26.1: Validate this is the proper change, Sara believes in the energy rework thiakil changed things to call 4 * getUsage instead of modifying the default energy storage
    // We also should update the comment above
    private LongSupplier energyStorage = () -> MathUtils.multiplyClamped(energyUsage.getAsInt(), 4L);

    public AttributeEnergy(@Nullable IntSupplier energyUsage, @Nullable LongSupplier energyStorage) {
        if (energyUsage != null) {
            this.energyUsage = energyUsage;
        }
        if (energyStorage != null) {
            this.energyStorage = energyStorage;
        }
    }

    public int getUsage() {
        return energyUsage.getAsInt();
    }

    public long getConfigStorage() {
        return energyStorage.getAsLong();
    }

    public long getStorage() {
        return Math.max(getConfigStorage(), getUsage());
    }
}
