package mekanism.common.util;

import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.IIncrementalEnum;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.text.IHasTextComponent;
import mekanism.common.MekanismLang;
import mekanism.common.base.ILangEntry;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public final class FluidContainerUtils {

    public static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && FluidUtil.getFluidHandler(stack).isPresent();
    }

    public static boolean canDrain(@Nonnull FluidStack tankFluid, @Nonnull FluidStack drainFluid) {
        return !tankFluid.isEmpty() && (drainFluid.isEmpty() || tankFluid.isFluidEqual(drainFluid));
    }

    public static boolean canFill(@Nonnull FluidStack tankFluid, @Nonnull FluidStack fillFluid) {
        return tankFluid.isEmpty() || tankFluid.isFluidEqual(fillFluid);
    }

    //TODO: Evaluate usages of these extract methods
    @Nonnull
    public static FluidStack extractFluid(FluidTank tileTank, IInventorySlot slot, FluidChecker checker) {
        Optional<IFluidHandlerItem> fluidHandlerItem = MekanismUtils.toOptional(FluidUtil.getFluidHandler(slot.getStack()));
        if (fluidHandlerItem.isPresent()) {
            IFluidHandlerItem handler = fluidHandlerItem.get();
            FluidStack ret = extractFluid(tileTank.getSpace(), handler, checker);
            slot.setStack(handler.getContainer());
            return ret;
        }
        return FluidStack.EMPTY;
    }

    @Nonnull
    private static FluidStack extractFluid(int needed, IFluidHandlerItem handler, FluidChecker checker) {
        if (handler == null) {
            return FluidStack.EMPTY;
        }
        FluidStack fluidStack = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
        if (fluidStack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (checker != null && !checker.isValid(fluidStack.getFluid())) {
            return FluidStack.EMPTY;
        }
        return handler.drain(needed, FluidAction.EXECUTE);
    }

    public enum ContainerEditMode implements IIncrementalEnum<ContainerEditMode>, IHasTextComponent {
        BOTH(MekanismLang.FLUID_CONTAINER_BOTH),
        FILL(MekanismLang.FLUID_CONTAINER_FILL),
        EMPTY(MekanismLang.FLUID_CONTAINER_EMPTY);

        private static final ContainerEditMode[] MODES = values();
        private final ILangEntry langEntry;

        ContainerEditMode(ILangEntry langEntry) {
            this.langEntry = langEntry;
        }

        @Override
        public ITextComponent getTextComponent() {
            return langEntry.translate();
        }

        @Nonnull
        @Override
        public ContainerEditMode byIndex(int index) {
            return byIndexStatic(index);
        }

        public static ContainerEditMode byIndexStatic(int index) {
            //TODO: Is it more efficient to check if index is negative and then just do the normal mod way?
            return MODES[Math.floorMod(index, MODES.length)];
        }
    }

    public static class FluidChecker {

        public static FluidChecker check(@Nonnull FluidStack fluid) {
            final Fluid type = fluid.getFluid();

            return new FluidChecker() {
                @Override
                public boolean isValid(Fluid f) {
                    return type == Fluids.EMPTY || type == f;
                }
            };
        }

        public static FluidChecker check(@Nonnull Fluid type) {
            return new FluidChecker() {
                @Override
                public boolean isValid(Fluid f) {
                    return type == Fluids.EMPTY || type == f;
                }
            };
        }

        public boolean isValid(Fluid f) {
            return true;
        }
    }
}