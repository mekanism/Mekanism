package mekanism.api.functions;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalStack;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class to reduce having to create duplicate objects for constant predicates.
 */
@SuppressWarnings("unchecked")
public class ConstantPredicates {

    private ConstantPredicates() {
    }

    /**
     * A boolean supplier that returns {@code true}.
     *
     * @since 10.5.0
     */
    public static final BooleanSupplier ALWAYS_TRUE = () -> true;

    /**
     * A supplier that returns {@code 0L}.
     *
     * @since 10.6.6
     */
    public static final LongSupplier ZERO_LONG = () -> 0;

    private static final Predicate<Object> alwaysTrue = t -> true;
    private static final BiPredicate<Object, Object> alwaysTrueBi = (t, u) -> true;
    private static final TriPredicate<Object, Object, Object> alwaysTrueTri = (t, u, v) -> true;

    /**
     * Represents a predicate that checks if an item stack is empty.
     *
     * @since 10.5.15
     */
    public static final Predicate<ItemStack> ITEM_EMPTY = ItemStack::isEmpty;
    /**
     * Represents a predicate that checks if a fluid stack is empty.
     *
     * @since 10.5.15
     */
    public static final Predicate<FluidStack> FLUID_EMPTY = FluidStack::isEmpty;
    /**
     * Represents a predicate that checks if a chemical stack is empty.
     *
     * @since 10.7.0
     */
    public static final Predicate<ChemicalStack> CHEMICAL_EMPTY = ChemicalStack::isEmpty;

    private static final Predicate<Object> alwaysFalse = t -> false;
    private static final BiPredicate<Object, Object> alwaysFalseBi = (t, u) -> false;
    private static final TriPredicate<Object, Object, Object> alwaysFalseTri = (t, u, v) -> false;

    private static final BiPredicate<Object, @NotNull AutomationType> internalOnly = (t, automationType) -> automationType == AutomationType.INTERNAL;
    private static final BiPredicate<Object, @NotNull AutomationType> notExternal = (t, automationType) -> automationType != AutomationType.EXTERNAL;
    private static final BiPredicate<Object, @NotNull AutomationType> manualOnly = (t, automationType) -> automationType == AutomationType.MANUAL;

    /**
     * Returns a predicate that returns {@code true} for any input.
     */
    public static <T> Predicate<T> alwaysTrue() {
        return (Predicate<T>) alwaysTrue;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input.
     */
    public static <T, U> BiPredicate<T, U> alwaysTrueBi() {
        return (BiPredicate<T, U>) alwaysTrueBi;
    }

    /**
     * Returns a tri predicate that returns {@code true} for any input.
     */
    public static <T, U, V> TriPredicate<T, U, V> alwaysTrueTri() {
        return (TriPredicate<T, U, V>) alwaysTrueTri;
    }

    /**
     * Returns a predicate that returns {@code false} for any input.
     */
    public static <T> Predicate<T> alwaysFalse() {
        return (Predicate<T>) alwaysFalse;
    }

    /**
     * Returns a bi predicate that returns {@code false} for any input.
     */
    public static <T, V> BiPredicate<T, V> alwaysFalseBi() {
        return (BiPredicate<T, V>) alwaysFalseBi;
    }

    /**
     * Returns a tri predicate that returns {@code false} for any input.
     */
    public static <T, U, V> TriPredicate<T, U, V> alwaysFalseTri() {
        return (TriPredicate<T, U, V>) alwaysFalseTri;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input when the automation type is internal.
     *
     * @since 10.3.3
     */
    public static <T> BiPredicate<T, @NotNull AutomationType> internalOnly() {
        return (BiPredicate<T, @NotNull AutomationType>) internalOnly;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input when the automation type is not external.
     *
     * @since 10.3.3
     */
    public static <T> BiPredicate<T, @NotNull AutomationType> notExternal() {
        return (BiPredicate<T, @NotNull AutomationType>) notExternal;
    }

    /**
     * Returns a bi predicate that returns {@code true} for any input when the automation type is manual.
     *
     * @since 10.7.0
     */
    public static <T> BiPredicate<T, @NotNull AutomationType> manualOnly() {
        return (BiPredicate<T, @NotNull AutomationType>) manualOnly;
    }

}