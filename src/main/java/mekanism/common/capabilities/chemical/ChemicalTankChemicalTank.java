package mekanism.common.capabilities.chemical;

import com.google.common.primitives.Ints;
import java.util.Objects;
import java.util.function.IntSupplier;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.tier.ChemicalTankTier;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ChemicalTankChemicalTank extends BasicChemicalTank {

    public static ChemicalTankChemicalTank create(ChemicalTankTier tier, @Nullable IContentsListener listener) {
        Objects.requireNonNull(tier, "Chemical tank tier cannot be null");
        return new ChemicalTankChemicalTank(tier, listener);
    }

    private final boolean isCreative;
    private final IntSupplier rate;

    private ChemicalTankChemicalTank(ChemicalTankTier tier, @Nullable IContentsListener listener) {
        super(tier.getStorage(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null, listener);
        isCreative = tier == ChemicalTankTier.CREATIVE;
        //TODO - 26.1: Make getOutput return an int
        rate = () -> Ints.saturatedCast(tier.getOutput());
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int getInsertionRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.getAsInt() : super.getInsertionRate(automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int getExtractionRate(@Nullable AutomationType automationType) {
        //Only limit the internal rate to change the speed at which this can be filled from an item
        return automationType == AutomationType.INTERNAL ? rate.getAsInt() : super.getExtractionRate(automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            if (isEmpty() && automationType != AutomationType.EXTERNAL) {
                //If a player manually inserts into a creative tank (or internally, via a ChemicalInventorySlot), that is empty we need to allow setting the type,
                // Note: We check that it is not external insertion because an empty creative tanks acts as a "void" for automation
                try (Transaction simulation = Transaction.open(transaction)) {
                    if (super.insert(resource, amount, simulation, automationType) == 0) {
                        return 0;
                    }
                }
                //If we managed to insert anything, set the contents to the maximum amount of that item type
                updateSnapshots(transaction);
                // Note: We just set it as unchecked as we have already validated it
                setContentsUnchecked(resource, capacityAsLong(resource));
                //Return that we accepted the entire amount we were passed
                return amount;
            }
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(resource, amount, simulation, automationType);
            }
        }
        return super.insert(resource, amount, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.extract(resource, amount, simulation, automationType);
            }
        }
        return super.extract(resource, amount, transaction, automationType);
    }
}