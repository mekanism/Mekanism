package mekanism.common.capabilities.chemical.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.slurry.ISlurryTank;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitSlurryTank;
import org.jetbrains.annotations.NotNull;

@NothingNullByDefault
public class RateLimitSlurryHandler extends ItemStackMekanismSlurryHandler {

    public static RateLimitSlurryHandler create(LongSupplier rate, LongSupplier capacity) {
        return create(rate, capacity, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrueBi, ChemicalTankBuilder.SLURRY.alwaysTrue);
    }

    public static RateLimitSlurryHandler create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NotNull Slurry, @NotNull AutomationType> canExtract,
          BiPredicate<@NotNull Slurry, @NotNull AutomationType> canInsert, Predicate<@NotNull Slurry> isValid) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Slurry validity check cannot be null");
        return new RateLimitSlurryHandler(listener -> new RateLimitSlurryTank(rate, capacity, canExtract, canInsert, isValid, listener));
    }

    private final ISlurryTank tank;

    private RateLimitSlurryHandler(Function<IContentsListener, ISlurryTank> tankProvider) {
        this.tank = tankProvider.apply(this);
    }

    @Override
    protected List<ISlurryTank> getInitialTanks() {
        return Collections.singletonList(tank);
    }
}