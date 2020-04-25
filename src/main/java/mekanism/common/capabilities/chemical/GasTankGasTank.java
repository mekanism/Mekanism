package mekanism.common.capabilities.chemical;

import java.util.Objects;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.chemical.gas.BasicGasTank;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IMekanismGasHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tier.GasTankTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasTankGasTank extends BasicGasTank {

    public static GasTankGasTank create(GasTankTier tier, @Nullable IMekanismGasHandler gasHandler) {
        Objects.requireNonNull(tier, "Gas tank tier cannot be null");
        return new GasTankGasTank(tier, gasHandler);
    }

    private final boolean isCreative;
    private final LongSupplier rate;

    private GasTankGasTank(GasTankTier tier, @Nullable IMekanismGasHandler gasHandler) {
        super(tier.getStorage(), alwaysTrueBi, alwaysTrueBi, alwaysTrue, tier == GasTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null, gasHandler);
        isCreative = tier == GasTankTier.CREATIVE;
        rate = tier::getOutput;
    }

    @Override
    protected long getRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate so as to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.getAsLong() : super.getRate(automationType);
    }

    @Override
    public GasStack insert(GasStack stack, Action action, AutomationType automationType) {
        if (isCreative && isEmpty() && action.execute() && automationType != AutomationType.EXTERNAL) {
            //If a player manually inserts into a creative tank (or internally, via a GasInventorySlot), that is empty we need to allow setting the type,
            // Note: We check that it is not external insertion because an empty creative tanks acts as a "void" for automation
            GasStack simulatedRemainder = super.insert(stack, Action.SIMULATE, automationType);
            if (simulatedRemainder.isEmpty()) {
                //If we are able to insert it then set perform the action of setting it to full
                setStackUnchecked(createStack(stack, getCapacity()));
            }
            return simulatedRemainder;
        }
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public GasStack extract(long amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(long, Action)}, as both {@link #growStack(long, Action)} and {@link #shrinkStack(long, Action)} are wrapped through
     * this method.
     */
    @Override
    public long setStackSize(long amount, Action action) {
        return super.setStackSize(amount, action.combine(!isCreative));
    }
}