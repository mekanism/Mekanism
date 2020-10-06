package mekanism.common.inventory.container.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import mekanism.common.lib.frequency.Frequency;
import mekanism.common.network.container.property.FrequencyPropertyData;
import mekanism.common.network.container.property.PropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for handling frequencies
 */
public class SyncableFrequency<FREQUENCY extends Frequency> implements ISyncableData {

    public static <FREQUENCY extends Frequency> SyncableFrequency<FREQUENCY> create(Supplier<FREQUENCY> getter, Consumer<FREQUENCY> setter) {
        return new SyncableFrequency<>(getter, setter);
    }

    private final Supplier<FREQUENCY> getter;
    private final Consumer<FREQUENCY> setter;
    private int lastKnownHashCode;

    private SyncableFrequency(Supplier<FREQUENCY> getter, Consumer<FREQUENCY> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    @Nullable
    public FREQUENCY get() {
        return getter.get();
    }

    public void set(@Nullable FREQUENCY value) {
        setter.accept(value);
    }

    @Override
    public DirtyType isDirty() {
        FREQUENCY value = get();
        int valueHashCode = value == null ? 0 : value.getSyncHash();
        if (lastKnownHashCode == valueHashCode) {
            return DirtyType.CLEAN;
        }
        lastKnownHashCode = valueHashCode;
        return DirtyType.DIRTY;
    }

    @Override
    public PropertyData getPropertyData(short property, DirtyType dirtyType) {
        return new FrequencyPropertyData<>(property, get());
    }
}