package mekanism.common.capabilities.chemical.item;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.infuse.IInfusionTank;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.inventory.AutomationType;
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank.RateLimitInfusionTank;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RateLimitInfusionHandler extends ItemStackMekanismInfusionHandler {

    public static RateLimitInfusionHandler create(LongSupplier rate, LongSupplier capacity) {
        return create(rate, capacity, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrueBi, ChemicalTankBuilder.INFUSION.alwaysTrue);
    }

    public static RateLimitInfusionHandler create(LongSupplier rate, LongSupplier capacity, BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull InfuseType, @NonNull AutomationType> canInsert, Predicate<@NonNull InfuseType> isValid) {
        Objects.requireNonNull(rate, "Rate supplier cannot be null");
        Objects.requireNonNull(capacity, "Capacity supplier cannot be null");
        Objects.requireNonNull(canExtract, "Extraction validity check cannot be null");
        Objects.requireNonNull(canInsert, "Insertion validity check cannot be null");
        Objects.requireNonNull(isValid, "Infuse type validity check cannot be null");
        return new RateLimitInfusionHandler(listener -> new RateLimitInfusionTank(rate, capacity, canExtract, canInsert, isValid, listener));
    }

    private final IInfusionTank tank;

    private RateLimitInfusionHandler(Function<IContentsListener, IInfusionTank> tankProvider) {
        this.tank = tankProvider.apply(this);
    }

    @Override
    protected List<IInfusionTank> getInitialTanks() {
        return Collections.singletonList(tank);
    }
}