package mekanism.fabric_shim.common;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link DataComponentHolder} whose components can be mutated (stand-in for NeoForge's
 * MutableDataComponentHolder; same surface, fresh implementation).
 */
public interface MutableDataComponentHolder extends DataComponentHolder {

    @Nullable
    <T> T set(DataComponentType<? super T> componentType, @Nullable T value);

    @Nullable
    default <T> T set(Supplier<? extends DataComponentType<? super T>> componentType, @Nullable T value) {
        return set(componentType.get(), value);
    }

    @Nullable
    default <T, U> T update(DataComponentType<T> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
        return set(componentType, updater.apply(getOrDefault(componentType, value), updateContext));
    }

    @Nullable
    default <T, U> T update(Supplier<? extends DataComponentType<T>> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
        return update(componentType.get(), value, updateContext, updater);
    }

    @Nullable
    default <T> T update(DataComponentType<T> componentType, T value, UnaryOperator<T> updater) {
        return set(componentType, updater.apply(getOrDefault(componentType, value)));
    }

    @Nullable
    default <T> T update(Supplier<? extends DataComponentType<T>> componentType, T value, UnaryOperator<T> updater) {
        return update(componentType.get(), value, updater);
    }

    @Nullable
    <T> T remove(DataComponentType<? extends T> componentType);

    @Nullable
    default <T> T remove(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return remove(componentType.get());
    }

    default void copyFrom(DataComponentHolder src, DataComponentType<?>... componentTypes) {
        for (DataComponentType<?> componentType : componentTypes) {
            copyFrom(componentType, src);
        }
    }

    @SuppressWarnings("unchecked")
    default void copyFrom(DataComponentHolder src, Supplier<? extends DataComponentType<?>>... componentTypes) {
        for (Supplier<? extends DataComponentType<?>> componentType : componentTypes) {
            copyFrom(componentType.get(), src);
        }
    }

    void applyComponents(DataComponentPatch patch);

    void applyComponents(DataComponentMap components);

    private <T> void copyFrom(DataComponentType<T> componentType, DataComponentHolder src) {
        set(componentType, src.get(componentType));
    }
}
