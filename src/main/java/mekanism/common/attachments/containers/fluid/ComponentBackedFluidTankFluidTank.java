package mekanism.common.attachments.containers.fluid;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.attachments.containers.ContainerType;
import mekanism.common.item.block.machine.ItemBlockFluidTank;
import mekanism.common.tier.FluidTankTier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

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
        super(attachedTo, tankIndex, ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrueBi(), ConstantPredicates.alwaysTrue(), tier::getOutput, tier::getStorage);
        isCreative = tier == FluidTankTier.CREATIVE;
    }

    @Override
    public FluidStack insert(FluidStack stack, Action action, AutomationType automationType) {
        return super.insert(stack, action.combine(!isCreative), automationType);
    }

    @Override
    public FluidStack extract(AttachedFluids attachedFluids, FluidStack stored, int amount, Action action, AutomationType automationType) {
        return super.extract(attachedFluids, stored, amount, action.combine(!isCreative), automationType);
    }

    /**
     * {@inheritDoc}
     *
     * Note: We are only patching {@link #setStackSize(AttachedFluids, FluidStack, int, Action)}, as both {@link #growStack(int, Action)} and
     * {@link #shrinkStack(int, Action)} are wrapped through this method.
     */
    @Override
    public int setStackSize(AttachedFluids attachedFluids, FluidStack stored, int amount, Action action) {
        return super.setStackSize(attachedFluids, stored, amount, action.combine(!isCreative));
    }
}