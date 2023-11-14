package mekanism.common.capabilities.chemical.item;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.pigment.IPigmentTank;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitPigmentTank;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RateLimitPigmentHandler extends ItemStackMekanismPigmentHandler {

    public static RateLimitPigmentHandler create(ItemStack stack, LongSupplier rate, LongSupplier capacity) {
        return create(stack, rate, capacity, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrueBi, ChemicalTankBuilder.PIGMENT.alwaysTrue);
    }

    public static RateLimitPigmentHandler create(ItemStack stack, LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Pigment, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Pigment, @NotNull AutomationType> canInsert, Predicate<@NotNull Pigment> isValid) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Pigment validity check cannot be null");
        return new RateLimitPigmentHandler(stack, listener -> new RateLimitPigmentTank(rate, capacity, canExtract, canInsert, isValid, listener));
    }

    private RateLimitPigmentHandler(ItemStack stack, Function<IContentsListener, IPigmentTank> tankProvider) {
        super(stack, tankProvider);
    }
}