package mekanism.common.attachments.containers.chemical;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.chemical.ChemicalResource;
import mekanism.api.chemical.attribute.ChemicalAttributeValidator;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.ItemBlockChemicalTank;
import mekanism.common.tier.ChemicalTankTier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

/**
 * Special handling for the Chemical Tank block item.
 */
@NothingNullByDefault
public class ComponentBackedChemicalTankTank extends ComponentBackedChemicalTank {

    private final boolean isCreative;

    public static ComponentBackedChemicalTankTank create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int tankIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockChemicalTank item)) {
            throw new IllegalStateException("Attached to should always be a chemical tank item");
        }
        return new ComponentBackedChemicalTankTank(attachedTo, tankIndex, item.getTier());
    }

    private ComponentBackedChemicalTankTank(ItemStack attachedTo, int tankIndex, ChemicalTankTier tier) {
        super(attachedTo, tankIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(),
              tier::getTransferRate, tier::getCapacity, tier == ChemicalTankTier.CREATIVE ? ChemicalAttributeValidator.ALWAYS_ALLOW : null);
        isCreative = tier == ChemicalTankTier.CREATIVE;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(ChemicalResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes)
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