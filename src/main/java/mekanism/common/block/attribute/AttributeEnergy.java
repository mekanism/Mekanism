package mekanism.common.block.attribute;

import com.google.common.primitives.UnsignedLongs;
import java.util.function.LongSupplier;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.FloatingLongSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO: Eventually we may want to make these suppliers be used more like suppliers in that:
// if the config updates it doesn't require a server restart (or chunk reload to take effect
public class AttributeEnergy implements Attribute {

    private LongSupplier energyUsage = () -> 0L;
    // 2 operations (20 secs) worth of ticks * usage
    private LongSupplier energyStorage = () -> energyUsage.getAsLong() * (400);

    public AttributeEnergy(@Nullable LongSupplier energyUsage, @Nullable LongSupplier energyStorage) {
        if (energyUsage != null) {
            this.energyUsage = energyUsage;
        }
        if (energyStorage != null) {
            this.energyStorage = energyStorage;
        }
    }

    public long getUsage() {
        return energyUsage.getAsLong();
    }

    public long getConfigStorage() {
        return energyStorage.getAsLong();
    }

    public long getStorage() {
        return UnsignedLongs.max(getConfigStorage(), getUsage());
    }
}
