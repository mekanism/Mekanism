package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.common.network.container.property.FloatingLongPropertyData;
import mekanism.common.network.container.property.PropertyData;
import mekanism.common.network.container.property.ShortPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling floating long
 */
public class SyncableFloatingLong implements ISyncableData {

    public static SyncableFloatingLong create(Supplier<@NonNull FloatingLong> getter, Consumer<@NonNull FloatingLong> setter) {
        return new SyncableFloatingLong(getter, setter);
    }

    private final Supplier<@NonNull FloatingLong> getter;
    private final Consumer<@NonNull FloatingLong> setter;
    private long lastKnownValue;
    private short lastKnownDecimal;

    private SyncableFloatingLong(Supplier<@NonNull FloatingLong> getter, Consumer<@NonNull FloatingLong> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Nonnull
    public FloatingLong get() {
        return getter.get();
    }

    public void set(@Nonnull FloatingLong value) {
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