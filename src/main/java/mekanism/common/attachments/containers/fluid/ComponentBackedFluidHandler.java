package mekanism.common.attachments.containers.fluid;

import java.util.List;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.ExtendedFluidHandlerUtils;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.fluid.IMekanismFluidHandler;
import mekanism.common.attachments.containers.ComponentBackedHandler;
import mekanism.common.attachments.containers.ContainerType;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class ComponentBackedFluidHandler extends ComponentBackedHandler<FluidStack, IExtendedFluidTank, AttachedFluids> implements IMekanismFluidHandler, IFluidHandlerItem {

    public ComponentBackedFluidHandler(ItemStack attachedTo) {
        super(attachedTo);
    }

    @Override
    protected ContainerType<IExtendedFluidTank, AttachedFluids, ?> containerType() {
        return ContainerType.FLUID;
    }

    @Override
    public List<IExtendedFluidTank> getFluidTanks(@Nullable Direction side) {
        return getContainers();
    }

    @Nullable
    @Override
    public IExtendedFluidTank getFluidTank(int tank, @Nullable Direction side) {
        return getContainer(tank);
    }

    @Override
    public int getTanks(@Nullable Direction side) {
        return containerCount();
    }

    @Override
    public FluidStack getFluidInTank(int tank, @Nullable Direction side) {
        AttachedFluids attachedFluids = getAttached();
        return attachedFluids == null ? FluidStack.EMPTY : attachedFluids.get(tank);
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, @Nullable Direction side, Action action) {
        //TODO - 1.20.5: Can we optimize this any further? Maybe by somehow only initializing the fluid tanks as necessary/we actually get to iterating against them?
        return ExtendedFluidHandlerUtils.insert(stack, side, this::getFluidTanks, action, AutomationType.handler(side));
    }

    @Override
    public FluidStack extractFluid(int amount, @Nullable Direction side, Action action) {
        //TODO - 1.20.5: Can we optimize this any further? Maybe by somehow only initializing the fluid tanks as necessary/we actually get to iterating against them?
        return ExtendedFluidHandlerUtils.extract(amount, side, this::getFluidTanks, action, AutomationType.handler(side));
    }

    @Override
    public FluidStack extractFluid(FluidStack stack, @Nullable Direction side, Action action) {
        //TODO - 1.20.5: Can we optimize this any further? Maybe by somehow only initializing the fluid tanks as necessary/we actually get to iterating against them?
        return ExtendedFluidHandlerUtils.extract(stack, side, this::getFluidTanks, action, AutomationType.handler(side));
    }

    @Override
    public ItemStack getContainer() {
        return attachedTo;
    }
}