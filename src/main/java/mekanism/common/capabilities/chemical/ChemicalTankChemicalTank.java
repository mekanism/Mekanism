package mekanism.common.capabilities.chemical;

import java.util.Objects;
import java.util.function.LongSupplier;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.tier.ChemicalTankTier;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ChemicalTankChemicalTank extends BasicChemicalTank {

    public static ChemicalTankChemicalTank create(ChemicalTankTier tier, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tier, "Chemical tank tier cannot be null");
        return new ChemicalTankChemicalTank(tier, listener);
    }

    private final boolean isCreative;
    private final LongSupplier rate;

    private ChemicalTankChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
        super(tier.getStorage(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null, listener, null);
        isCreative = tier == ChemicalTankTier.CREATIVE;
        rate = tier::getOutput;
    }

    @Override
    protected long getInsertRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.getAsLong() : super.getInsertRate(automationType);
    }

    @Override
    protected long getExtractRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.getAsLong() : super.getExtractRate(automationType);
    }

    @Override
    public ChemicalStack insert(ChemicalStack stack, Action action, AutomationType automationType) {
        if (isCreative && isEmpty() && action.execute() && automationType != AutomationType.EXTERNAL) {
            //If a player manually inserts into a creative tank (or internally, via a GasInventorySlot), that is empty we need to allow setting the type,
            // Note: We check that it is not external insertion because an empty creative tanks acts as a "void" for automation
            ChemicalStack simulatedRemainder = super.insert(stack, Action.SIMULATE, automationType);
            if (simulatedRemainder.isEmpty()) {
                //If we are able to insert it then set perform the action of setting it to full
                setStackUnchecked(stack.copyWithAmount(getCapacity()));
            }
            return simulatedRemainder;
        }
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public ChemicalStack extract(long amount, Action action, AutomationType automationType) {
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