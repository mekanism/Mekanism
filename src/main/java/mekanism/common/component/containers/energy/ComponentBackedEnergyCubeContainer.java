package mekanism.common.component.containers.energy;

import java.util.Objects;
import mekanism.api.AutomationType;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tier.EnergyCubeTier;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

public class ComponentBackedEnergyCubeContainer extends ComponentBackedEnergyContainer {

    public static ComponentBackedEnergyCubeContainer create(ItemAccess attachedAccess) {
        EnergyCubeTier tier = attachedAccess.getResource().get(MekanismDataComponents.ENERGY_CUBE_TIER);
        Objects.requireNonNull(tier, "Attached to should always have an energy cube tier defined");
        return new ComponentBackedEnergyCubeContainer(attachedAccess, tier);
    }

    private final boolean isCreative;

    private ComponentBackedEnergyCubeContainer(ItemAccess attachedAccess, EnergyCubeTier tier) {
        super(attachedAccess, ConstantPredicates.alwaysTrue(), ConstantPredicates.alwaysTrue(), tier::getCapacity, tier::getTransferRate);
        isCreative = tier.isCreative();
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes)
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.insert(amount, simulation, automationType);
            }
        }
        return super.insert(amount, transaction, automationType);
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int extract(@Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.extract(amount, simulation, automationType);
            }
        }
        return super.extract(amount, transaction, automationType);
    }
}