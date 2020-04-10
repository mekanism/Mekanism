package mekanism.common.inventory.container.sync;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.common.network.container.property.IntPropertyData;

/**
 * Version of {@link net.minecraft.util.IntReferenceHolder} for making it easier to handle enums
 */
public class SyncableEnum<ENUM extends Enum<ENUM>> implements ISyncableData {

    public static <ENUM extends Enum<ENUM>> SyncableEnum<ENUM> create(Int2ObjectFunction<ENUM> decoder, @Nonnull ENUM defaultValue, Supplier<@NonNull ENUM> getter,
          Consumer<@NonNull ENUM> setter) {
        return new SyncableEnum<>(decoder, defaultValue, getter, setter);
    }

    private final Int2ObjectFunction<ENUM> decoder;
    private final Supplier<@NonNull ENUM> getter;
    private final Consumer<@NonNull ENUM> setter;
    @Nonnull
    private ENUM lastKnownValue;

    private SyncableEnum(Int2ObjectFunction<ENUM> decoder, @Nonnull ENUM defaultValue, Supplier<@NonNull ENUM> getter, Consumer<@NonNull ENUM> setter) {
        this.decoder = decoder;
        this.lastKnownValue = defaultValue;
        this.getter = getter;
        this.setter = setter;
    }

    @Nonnull
    public ENUM get() {
        return getter.get();
    }

    public void set(int ordinal) {
        set(decoder.apply(ordinal));
    }

    public void set(@Nonnull ENUM value) {
        setter.accept(value);
    }

    @Override
    public DirtyType isDirty() {
        ENUM oldValue = get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return DirtyType.get(dirty);
    }

    @Override
    public IntPropertyData getPropertyData(short property, DirtyType dirtyType) {
        return new IntPropertyData(property, get().ordinal());
    }
}