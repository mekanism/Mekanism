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

    public ComponentBackedFluidHandler(ItemStack attachedTo, int totalTanks) {
        super(attachedTo, totalTanks);
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
        return size();
    }

    @Override
    public FluidStack getFluidInTank(int tank, @Nullable Direction side) {
        return getContents(tank);
    }

    @Override
    public FluidStack insertFluid(FluidStack stack, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.insert(stack, action, AutomationType.handler(side), size(), this);
    }

    @Override
    public FluidStack extractFluid(int amount, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.extract(amount, action, AutomationType.handler(side), size(), this);
    }

    @Override
    public FluidStack extractFluid(FluidStack stack, @Nullable Direction side, Action action) {
        return ExtendedFluidHandlerUtils.extract(stack, action, AutomationType.handler(side), size(), this);
    }

    @Override
    public ItemStack getContainer() {
        return attachedTo;
    }
}