package mekanism.common.capabilities.chemical.item;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.chemical.ChemicalTanksBuilder;
import mekanism.common.attachments.containers.chemical.ComponentBackedChemicalTank;
import mekanism.common.capabilities.GenericTankSpec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChemicalTankSpec extends GenericTankSpec<ChemicalStack> {

    private final LongSupplier rate;
    private final LongSupplier capacity;
    @Nullable
    private final ToLongFunction<ItemStack> stackBasedCapacity;
    @Nullable
    private final ChemicalAttributeValidator validator;

    private ChemicalTankSpec(LongSupplier rate, LongSupplier capacity, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
          TriPredicate<ChemicalStack, @NotNull AutomationType, @NotNull ItemStack> canInsert, Predicate<ChemicalStack> isValid,
          @Nullable ChemicalAttributeValidator validator, Predicate<@NotNull ItemStack> supportsStack) {
        this(rate, capacity, null, canExtract, canInsert, isValid, validator, supportsStack);
    }

    private ChemicalTankSpec(LongSupplier rate, ToLongFunction<ItemStack> stackBasedCapacity, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
          TriPredicate<ChemicalStack, @NotNull AutomationType, @NotNull ItemStack> canInsert, Predicate<ChemicalStack> isValid,
          @Nullable ChemicalAttributeValidator validator, Predicate<@NotNull ItemStack> supportsStack) {
        this(rate, ConstantPredicates.ZERO_LONG, stackBasedCapacity, canExtract, canInsert, isValid, validator, supportsStack);
    }

    private ChemicalTankSpec(LongSupplier rate, LongSupplier capacity, @Nullable ToLongFunction<ItemStack> stackBasedCapacity,
          BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract, TriPredicate<ChemicalStack, @NotNull AutomationType, @NotNull ItemStack> canInsert,
          Predicate<ChemicalStack> isValid, @Nullable ChemicalAttributeValidator validator, Predicate<@NotNull ItemStack> supportsStack) {
        super(canExtract, canInsert, isValid, supportsStack);
        this.rate = rate;
        this.capacity = capacity;
        this.stackBasedCapacity = stackBasedCapacity;
        this.validator = validator;
    }

    public IChemicalTank createTank(TankFromSpecCreator tankCreator, ItemStack stack) {
        LongSupplier capacity = stackBasedCapacity == null ? this.capacity : () -> stackBasedCapacity.applyAsLong(stack);
        return tankCreator.create(rate, capacity, canExtract, (chemical, automationType) ->
              canInsert.test(chemical, automationType, stack), isValid, validator, null);
    }

    //TODO - 1.20.5: Re-evaluate this
    public void addTank(ChemicalTanksBuilder builder, ComponentTankFromSpecCreator tankCreator) {
        if (stackBasedCapacity == null) {
            builder.addTank(((type, attachedTo, containerIndex) -> tankCreator.create(attachedTo, containerIndex, canExtract,
                  (chemical, automationType) -> canInsert.test(chemical, automationType, attachedTo), isValid, rate, capacity, validator)));
        } else {
            builder.addTank(((type, attachedTo, containerIndex) -> tankCreator.create(attachedTo, containerIndex, canExtract,
                  (chemical, automationType) -> canInsert.test(chemical, automationType, attachedTo), isValid, rate, () -> stackBasedCapacity.applyAsLong(attachedTo), validator)));
        }
    }

    public static ChemicalTankSpec create(LongSupplier rate, LongSupplier capacity) {
        return new ChemicalTankSpec(rate, capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueTri(), ConstantPredicates.alwaysTrue(),
              null, ConstantPredicates.alwaysTrue());
    }

    public static ChemicalTankSpec createFillOnly(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull ChemicalStack> isValid) {
        return createFillOnly(rate, capacity, isValid, ConstantPredicates.alwaysTrue());
    }

    public static ChemicalTankSpec createFillOnly(LongSupplier rate, LongSupplier capacity, Predicate<@NotNull ChemicalStack> isValid, Predicate<@NotNull ItemStack> supportsStack) {
        return new ChemicalTankSpec(rate, capacity, ConstantPredicates.notExternal(), (chemical, automation, stack) -> supportsStack.test(stack), isValid, null, supportsStack);
    }

    public static ChemicalTankSpec createFillOnly(LongSupplier rate, ToLongFunction<ItemStack> stackBasedCapacity, Predicate<ChemicalStack> isValid,
          Predicate<@NotNull ItemStack> supportsStack) {
        return new ChemicalTankSpec(rate, stackBasedCapacity, ConstantPredicates.notExternal(),
              (chemical, automation, stack) -> supportsStack.test(stack), isValid, null, supportsStack);
    }

    @FunctionalInterface
    public interface ComponentTankFromSpecCreator {

        ComponentBackedChemicalTank create(ItemStack attachedTo, int tankIndex, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
              BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert, Predicate<ChemicalStack> isValid, LongSupplier rate, LongSupplier capacity,
              @Nullable ChemicalAttributeValidator validator);

        default ComponentBackedChemicalTank create(ItemStack attachedTo, int tankIndex, LongSupplier rate, LongSupplier capacity,
              BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract, BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert,
              Predicate<ChemicalStack> isValid) {
            return create(attachedTo, tankIndex, canExtract, canInsert, isValid, rate, capacity, null);
        }
    }

    @FunctionalInterface
    public interface TankFromSpecCreator {

        IChemicalTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
              BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert, Predicate<ChemicalStack> isValid, @Nullable ChemicalAttributeValidator validator,
              @Nullable IContentsListener listener);

        default IChemicalTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
              BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert, Predicate<ChemicalStack> isValid, @Nullable IContentsListener listener) {
            return create(rate, capacity, canExtract, canInsert, isValid, null, listener);
        }

        default IChemicalTank create(LongSupplier rate, LongSupplier capacity, BiPredicate<ChemicalStack, @NotNull AutomationType> canExtract,
              BiPredicate<ChemicalStack, @NotNull AutomationType> canInsert, Predicate<ChemicalStack> isValid) {
            return create(rate, capacity, canExtract, canInsert, isValid, null);
        }
    }
}