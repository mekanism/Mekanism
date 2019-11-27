package mekanism.common.util;

import java.util.Optional;
import javax.annotation.Nonnull;
import mekanism.api.Action;
import mekanism.api.IIncrementalEnum;
import mekanism.api.inventory.slot.IInventorySlot;
import mekanism.api.text.IHasTranslationKey;
import mekanism.common.base.LazyOptionalHelper;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;

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
    public static FluidStack extractFluid(FluidTank tileTank, IInventorySlot slot, FluidChecker checker) {
        return new LazyOptionalHelper<>(FluidUtil.getFluidHandler(slot.getStack())).getIfPresent(handler -> {
            FluidStack ret = extractFluid(tileTank.getCapacity() - tileTank.getFluidAmount(), handler, checker);
            slot.setStack(handler.getContainer());
            return ret;
        });
    }

    @Nonnull
    public static FluidStack extractFluid(int needed, IFluidHandlerItem handler, FluidChecker checker) {
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

    private static FluidStack handleContainerItemFill(TileEntity tileEntity, @Nonnull FluidStack stack, IInventorySlot inSlot, IInventorySlot outSlot) {
        if (!stack.isEmpty()) {
            ItemStack inputCopy = StackUtils.size(inSlot.getStack(), 1);
            Optional<IFluidHandlerItem> fluidHandlerItem = LazyOptionalHelper.toOptional(FluidUtil.getFluidHandler(inputCopy));
            int drained = 0;
            if (fluidHandlerItem.isPresent()) {
                IFluidHandlerItem handler = fluidHandlerItem.get();
                drained = handler.fill(stack, FluidAction.EXECUTE);
                inputCopy = handler.getContainer();
            }
            if (outSlot.isEmpty()) {
                stack.setAmount(stack.getAmount() - drained);
                outSlot.setStack(inputCopy);
            } else {
                ItemStack outputStack = outSlot.getStack();
                if (!ItemHandlerHelper.canItemStacksStack(outputStack, inputCopy) || outputStack.getCount() >= outSlot.getLimit(outputStack)) {
                    //We won't be able to move our container to the output slot so exit
                    return stack;
                }
                stack.setAmount(stack.getAmount() - drained);
                if (outSlot.growStack(1, Action.EXECUTE) != 1) {
                    //TODO: Print warning about failing to increase size of stack
                }
            }
            if (inSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                //TODO: Print warning about failing to shrink size of stack
            }
            tileEntity.markDirty();
        }
        return stack;
    }

    private static FluidStack handleContainerItemEmpty(TileEntity tileEntity, @Nonnull FluidStack stored, int needed, IInventorySlot inSlot, IInventorySlot outSlot) {
        final Fluid storedFinal = stored.getFluid();
        final ItemStack input = StackUtils.size(inSlot.getStack(), 1);
        Optional<IFluidHandlerItem> fluidHandlerItem = LazyOptionalHelper.toOptional(FluidUtil.getFluidHandler(input));
        if (!fluidHandlerItem.isPresent()) {
            return stored;
        }
        IFluidHandlerItem handler = fluidHandlerItem.get();
        FluidStack ret = extractFluid(needed, handler, new FluidChecker() {
            @Override
            public boolean isValid(Fluid f) {
                return storedFinal == Fluids.EMPTY || storedFinal == f;
            }
        });

        ItemStack inputCopy = handler.getContainer();
        ItemStack outputStack = outSlot.getStack();
        LazyOptionalHelper<FluidStack> containerFluidHelper = new LazyOptionalHelper<>(FluidUtil.getFluidContained(inputCopy));
        if (!containerFluidHelper.matches(fluidStack -> !fluidStack.isEmpty()) && !inputCopy.isEmpty()) {
            if (!outputStack.isEmpty() && (!ItemHandlerHelper.canItemStacksStack(outputStack, inputCopy) || outputStack.getCount() == outSlot.getLimit(outputStack))) {
                return stored;
            }
        }

        if (!ret.isEmpty()) {
            if (stored.isEmpty()) {
                stored = ret;
            } else {
                stored.setAmount(stored.getAmount() + ret.getAmount());
            }
            needed -= ret.getAmount();
            tileEntity.markDirty();
        }

        if (!containerFluidHelper.matches(fluidStack -> !fluidStack.isEmpty()) || needed == 0) {
            if (!inputCopy.isEmpty()) {
                if (outputStack.isEmpty()) {
                    outSlot.setStack(inputCopy);
                } else if (ItemHandlerHelper.canItemStacksStack(outputStack, inputCopy)) {
                    if (outSlot.growStack(1, Action.EXECUTE) != 1) {
                        //TODO: Print warning about failing to increase size of stack
                    }
                }
            }
            if (inSlot.shrinkStack(1, Action.EXECUTE) != 1) {
                //TODO: Print warning about failing to shrink size of stack
            }
            tileEntity.markDirty();
        } else {
            inSlot.setStack(inputCopy);
        }
        return stored;
    }

    public static FluidStack handleContainerItem(TileEntityMekanism tileEntity, ContainerEditMode editMode, @Nonnull FluidStack stack, int needed,
          IInventorySlot inSlot, IInventorySlot outSlot) {
        //TODO: Can these two methods be cleaned up by offloading checks to the IInventorySlots
        if (editMode == ContainerEditMode.FILL || (editMode == ContainerEditMode.BOTH &&
                                                   !new LazyOptionalHelper<>(FluidUtil.getFluidContained(inSlot.getStack())).matches(fluidStack -> !fluidStack.isEmpty()))) {
            //If our mode is fill or we have an empty container and support either mode, then fill
            return handleContainerItemFill(tileEntity, stack, inSlot, outSlot);
        } else if (editMode == ContainerEditMode.EMPTY || editMode == ContainerEditMode.BOTH) {
            //Otherwise if our mode is to empty, or it is both and our container was not empty, then drain
            return handleContainerItemEmpty(tileEntity, stack, needed, inSlot, outSlot);
        }
        return stack;
    }

    public enum ContainerEditMode implements IIncrementalEnum<ContainerEditMode>, IHasTranslationKey {
        BOTH("mekanism.fluidedit.both"),
        FILL("mekanism.fluidedit.fill"),
        EMPTY("mekanism.fluidedit.empty");

        private static final ContainerEditMode[] MODES = values();
        private String display;

        ContainerEditMode(String s) {
            display = s;
        }

        @Override
        public String getTranslationKey() {
            return display;
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