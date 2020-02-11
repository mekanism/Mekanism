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
public abstract class SyncableEnum<ENUM extends Enum<ENUM>> implements ISyncableData {

    private final Int2ObjectFunction<ENUM> decoder;
    @Nonnull
    private ENUM lastKnownValue;

    private SyncableEnum(Int2ObjectFunction<ENUM> decoder, @Nonnull ENUM defaultValue) {
        this.decoder = decoder;
        this.lastKnownValue = defaultValue;
    }

    @Nonnull
    public abstract ENUM get();

    public void set(int ordinal) {
        set(decoder.apply(ordinal));
    }

    public abstract void set(@Nonnull ENUM value);

    @Override
    public boolean isDirty() {
        ENUM oldValue = this.get();
        boolean dirty = oldValue != this.lastKnownValue;
        this.lastKnownValue = oldValue;
        return dirty;
    }

    @Override
    public IntPropertyData getPropertyData(short property) {
        return new IntPropertyData(property, get().ordinal());
    }

    public static <ENUM extends Enum<ENUM>> SyncableEnum<ENUM> create(Int2ObjectFunction<ENUM> decoder, @Nonnull ENUM defaultValue, Supplier<@NonNull ENUM> getter,
          Consumer<@NonNull ENUM> setter) {
        return new SyncableEnum<ENUM>(decoder, defaultValue) {

            @Nonnull
            @Override
            public ENUM get() {
                return getter.get();
            }

            @Override
            public void set(@Nonnull ENUM value) {
                setter.accept(value);
            }
        };
    }
}