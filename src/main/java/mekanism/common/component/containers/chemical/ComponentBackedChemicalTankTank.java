package mekanism.common.component.containers.chemical;

import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.component.containers.resource.AttachedResources;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/// Special handling for the Chemical Tank block item.
public class ComponentBackedChemicalTankTank extends ComponentBackedChemicalTank {

    private final boolean isCreative;

    public static ComponentBackedChemicalTankTank create(ItemAccess attachedAccess, int tankIndex) {
        if (!(attachedAccess.getResource().getItem() instanceof ItemBlockChemicalTank item)) {
            throw new IllegalStateException("Attached to should always be a chemical tank item");
        }
        return new ComponentBackedChemicalTankTank(attachedAccess, tankIndex, item.getTier());
    }

    private ComponentBackedChemicalTankTank(ItemAccess attachedAccess, int tankIndex, ChemicalTankTier tier) {
        super(attachedAccess, tankIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              tier::getCapacity, tier::getTransferRate, tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null);
        isCreative = tier == ChemicalTankTier.CREATIVE;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int insert(AttachedResources<ChemicalResource> attached, ChemicalResource currentType, @Range(from = 0, to = Long.MAX_VALUE) long currentAmount,
          long capacity, ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes)
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(attached, currentType, currentAmount, capacity, resource, amount, simulation, automationType);
            }
        }
        return super.insert(attached, currentType, currentAmount, capacity, resource, amount, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    protected int extract(AttachedResources<ChemicalResource> attached, ChemicalResource currentType, @Range(from = 0, to = Long.MAX_VALUE) long currentAmount,
          ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            try (Transaction simulation = Transaction.open(transaction)) {
                //Use a sub transaction that is not committed to effectively just simulate what will happen without making any changes
                return super.extract(attached, currentType, currentAmount, resource, amount, simulation, automationType);
            }
        }
        return super.extract(attached, currentType, currentAmount, resource, amount, transaction, automationType);
    }
}