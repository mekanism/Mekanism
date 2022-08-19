package mekanism.common.capabilities.chemical.item;

import java.util.function.BiPredicate;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.chemical.Chemical;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.capabilities.GenericTankSpec;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.NotNull;

public class ChemicalTankSpec<CHEMICAL extends Chemical<CHEMICAL>> extends GenericTankSpec<CHEMICAL> {

    final LongSupplier rate;
    final LongSupplier capacity;

    public ChemicalTankSpec(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull CHEMICAL, @NotNull AutomationType> canExtract,
          TriPredicate<@NotNull CHEMICAL, @NotNull AutomationType, @NotNull ItemStack> canInsert, Predicate<@NotNull CHEMICAL> isValid,
          Predicate<@NotNull ItemStack> supportsStack) {
        super(canExtract, canInsert, isValid, supportsStack);
        this.rate = rate;
        this.capacity = capacity;
    }

    @SuppressWarnings("Convert2Diamond")
    public static <CHEMICAL extends Chemical<CHEMICAL>> ChemicalTankSpec<CHEMICAL> create(LongSupplier rate, LongSupplier capacity) {
        return new ChemicalTankSpec<CHEMICAL>(rate, capacity, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueTri(), ConstantPredicates.alwaysTrue(),
              ConstantPredicates.alwaysTrue());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> ChemicalTankSpec<CHEMICAL> createFillOnly(LongSupplier rate, LongSupplier capacity,
          Predicate<@NotNull CHEMICAL> isValid) {
        return createFillOnly(rate, capacity, isValid, ConstantPredicates.alwaysTrue());
    }

    public static <CHEMICAL extends Chemical<CHEMICAL>> ChemicalTankSpec<CHEMICAL> createFillOnly(LongSupplier rate, LongSupplier capacity,
          Predicate<@NotNull CHEMICAL> isValid, Predicate<@NotNull ItemStack> supportsStack) {
        return new ChemicalTankSpec<>(rate, capacity, ConstantPredicates.notExternal(), (chemical, automation, stack) -> supportsStack.test(stack), isValid, supportsStack);
    }
}