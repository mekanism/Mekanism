package mekanism.common.capabilities.merged;

import java.util.function.BooleanSupplier;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.common.capabilities.ResourceContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/**
 * Helper class for wrapping a chemical tank for use in a multi chemical type. Disallowing interacting with various tanks if other tanks have contents. For example only
 * one chemical tank of a {@link MergedTank} can have a chemical in it at any time.
 */
@NothingNullByDefault
public class ChemicalTankWrapper extends ResourceContainerWrapper<ChemicalResource, IChemicalTank> implements IChemicalTank {

    private final BooleanSupplier insertCheck;
    private final MergedTank mergedTank;

    public ChemicalTankWrapper(MergedTank mergedTank, IChemicalTank internal, BooleanSupplier insertCheck) {
        super(internal);
        this.mergedTank = mergedTank;
        this.insertCheck = insertCheck;
    }

    /**
     * Gets the merged chemical tank.
     */
    public MergedTank getMergedTank() {
        return mergedTank;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        //Only allow inserting if we pass the check
        return insertCheck.getAsBoolean() ? super.insert(resource, amount, transaction, automationType) : 0;
    }

    @Override
    public ChemicalAttributeValidator getAttributeValidator() {
        return internal.getAttributeValidator();
    }
}