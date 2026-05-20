package mekanism.common.attachments.containers.fluid;

import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.tier.FluidTankTier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

@NothingNullByDefault
public class ComponentBackedFluidTankFluidTank extends ComponentBackedFluidTank {

    private final boolean isCreative;

    public static ComponentBackedFluidTankFluidTank create(ContainerType<?, ?, ?> ignored, ItemStack attachedTo, int tankIndex) {
        if (!(attachedTo.getItem() instanceof ItemBlockFluidTank item)) {
            throw new IllegalStateException("Attached to should always be a fluid tank item");
        }
        return new ComponentBackedFluidTankFluidTank(attachedTo, tankIndex, item.getTier());
    }

    private ComponentBackedFluidTankFluidTank(ItemStack attachedTo, int tankIndex, FluidTankTier tier) {
        super(attachedTo, tankIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), tier::getTransferRate, tier::getCapacity);
        isCreative = tier == FluidTankTier.CREATIVE;
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int insert(FluidResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
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
    public int extract(FluidResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction, AutomationType automationType) {
        if (isCreative) {
            //Return the result without actually changing the contents (accepting without providing any changes
            try (Transaction simulation = Transaction.open(transaction)) {
                return super.extract(resource, amount, simulation, automationType);
            }
        }
        return super.extract(resource, amount, transaction, automationType);
    }
}