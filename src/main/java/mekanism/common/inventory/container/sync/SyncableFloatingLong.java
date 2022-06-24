package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import mekanism.api.math.FloatingLong;
import mekanism.common.network.to_client.container.property.FloatingLongPropertyData;
import mekanism.common.network.to_client.container.property.PropertyData;
import mekanism.common.network.to_client.container.property.ShortPropertyData;
import org.jetbrains.annotations.NotNull;

/**
 * Version of {@link net.minecraft.world.inventory.DataSlot} for handling floating long
 */
public class SyncableFloatingLong implements ISyncableData {

    public static SyncableFloatingLong create(Supplier<@NotNull FloatingLong> getter, Consumer<@NotNull FloatingLong> setter) {
        return new SyncableFloatingLong(getter, setter);
    }

    private final Supplier<@NotNull FloatingLong> getter;
    private final Consumer<@NotNull FloatingLong> setter;
    private long lastKnownValue;
    private short lastKnownDecimal;

    private SyncableFloatingLong(Supplier<@NotNull FloatingLong> getter, Consumer<@NotNull FloatingLong> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @NotNull
    public FloatingLong get() {
        return getter.get();
    }

    public void set(@NotNull FloatingLong value) {
        setter.accept(value);
    }

    public void setDecimal(short decimal) {
        set(FloatingLong.create(get().getValue(), decimal));
    }

    @Override
    public DirtyType isDirty() {
        FloatingLong val = get();
        long value = val.getValue();
        short decimal = val.getDecimal();
        if (value == lastKnownValue && decimal == lastKnownDecimal) {
            return DirtyType.CLEAN;
        }
        DirtyType type = DirtyType.DIRTY;
        if (value == lastKnownValue) {
            type = DirtyType.SIZE;
        }
        lastKnownValue = value;
        lastKnownDecimal = decimal;
        return type;
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        if (dirtyType == DirtyType.SIZE) {
            //If only the size changed, don't bother re-syncing the type
            return new ShortPropertyData(property, get().getDecimal());
        }
        return new FloatingLongPropertyData(property, get());
    }
}