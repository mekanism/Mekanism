package mekanism.common.base.handler;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mcp.MethodsReturnNonnullByDefault;
import mekanism.api.Action;
import mekanism.api.gas.BasicGasTank;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IMekanismGasHandler;
import mekanism.api.inventory.AutomationType;
import mekanism.common.tier.GasTankTier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GasTankGasTank extends BasicGasTank {

    public static GasTankGasTank create(GasTankTier tier, @Nullable IMekanismGasHandler gasHandler) {
        Objects.requireNonNull(tier, "Gas tank tier cannot be null");
        return new GasTankGasTank(tier, gasHandler);
    }

    private boolean isCreative;

    private GasTankGasTank(GasTankTier tier, @Nullable IMekanismGasHandler gasHandler) {
        super(tier.getStorage(), alwaysTrueBi, alwaysTrueBi, alwaysTrue, gasHandler);
        isCreative = tier == GasTankTier.CREATIVE;
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
    public GasStack extract(int amount, Action action, AutomationType automationType) {
        return super.extract(amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(int, Action)}, as both {@link #growStack(int, Action)} and {@link #shrinkStack(int, Action)} are wrapped through
     * this method.
     */
    @Override
    public int setStackSize(int amount, Action action) {
        return super.setStackSize(amount, action.combine(!isCreative));
    }
}