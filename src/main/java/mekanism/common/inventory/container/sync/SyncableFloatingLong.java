package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.api.math.FloatingLong;
import mekanism.common.network.container.property.FloatingLongPropertyData;
import mekanism.common.network.container.property.PropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling floating long
 */
public class SyncableFloatingLong implements ISyncableData {

    public static SyncableFloatingLong create(Supplier<@NonNull FloatingLong> getter, Consumer<@NonNull FloatingLong> setter) {
        return new SyncableFloatingLong(getter, setter);
    }

    private final Supplier<@NonNull FloatingLong> getter;
    private final Consumer<@NonNull FloatingLong> setter;
    @Nonnull
    private FloatingLong lastKnownValue = FloatingLong.ZERO;

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

    @Override
    public DirtyType isDirty() {
        FloatingLong value = get();
        if (!value.equals(lastKnownValue)) {
            //Make sure to copy it in case our floating long object is the same object so would be getting modified
            // only do so though if it is dirty, as we don't need to spam object creation
            this.lastKnownValue = value.copyAsConst();
            return DirtyType.DIRTY;
        }
        return DirtyType.CLEAN;
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        return new FloatingLongPropertyData(property, get());
    }
}