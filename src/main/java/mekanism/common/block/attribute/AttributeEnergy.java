package mekanism.common.block.attribute;

import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Eventually we may want to make these suppliers be used more like suppliers in that:
// if the config updates it doesn't require a server restart (or chunk reload to take effect
public class AttributeEnergy implements Attribute {

    private FloatingLongSupplier energyUsage = () -> FloatingLong.ZERO;
    // 2 operations (20 secs) worth of ticks * usage
    private FloatingLongSupplier energyStorage = () -> energyUsage.get().multiply(400);

    public AttributeEnergy(@Nullable FloatingLongSupplier energyUsage, @Nullable FloatingLongSupplier energyStorage) {
        if (energyUsage != null) {
            this.energyUsage = energyUsage;
        }
        if (energyStorage != null) {
            this.energyStorage = energyStorage;
        }
    }

    @NotNull
    public FloatingLong getUsage() {
        return energyUsage.get();
    }

    @NotNull
    public FloatingLong getConfigStorage() {
        return energyStorage.get();
    }

    @NotNull
    public FloatingLong getStorage() {
        return getConfigStorage().max(getUsage());
    }
}
