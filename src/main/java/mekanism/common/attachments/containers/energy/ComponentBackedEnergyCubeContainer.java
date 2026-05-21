package mekanism.common.attachments.containers.energy;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.ItemBlockEnergyCube;
import mekanism.common.tier.EnergyCubeTier;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedEnergyCubeContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedEnergyCubeContainer create(ContainerType<?, ?, ?> ignored, ItemAccess attachedAccess, int containerIndex) {
        if (!(attachedAccess.getResource().getItem() instanceof ItemBlockEnergyCube item)) {
            throw new IllegalStateException("Attached to should always be an energy cube item");
        }
        return new ComponentBackedEnergyCubeContainer(attachedAccess, containerIndex, item.getTier());
    }

    private final boolean isCreative;

    private ComponentBackedEnergyCubeContainer(ItemAccess attachedAccess, int containerIndex, EnergyCubeTier tier) {
        super(attachedAccess, containerIndex, ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), tier::getOutput, tier::getMaxEnergy);
        isCreative = tier == EnergyCubeTier.CREATIVE;
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long insert(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes)
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(amount, simulation, automationType);
            }
        }
        return super.insert(amount, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long extract(@Range(from = 0, to = Long.MAX_VALUE) long amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.extract(amount, simulation, automationType);
            }
        }
        return super.extract(amount, transaction, automationType);
    }
}