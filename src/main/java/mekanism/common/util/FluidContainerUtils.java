package mekanism.common.util;

import javax.annotation.Nonnull;
import mekanism.api.Action;
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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;

public final class FluidContainerUtils {

    public static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
    }

    public static boolean canDrain(@Nonnull FluidStack tankFluid, @Nonnull FluidStack drainFluid) {
        return !tankFluid.isEmpty() && (drainFluid.isEmpty() || tankFluid.isFluidEqual(drainFluid));
    }

    public static boolean canFill(@Nonnull FluidStack tankFluid, @Nonnull FluidStack fillFluid) {
        return tankFluid.isEmpty() || tankFluid.isFluidEqual(fillFluid);
    }

    public static FluidStack extractFluid(FluidTank tileTank, TileEntityMekanism tile, int slotID) {
        return extractFluid(tileTank, tile, slotID, FluidChecker.check(tileTank.getFluid()));
    }

    public static FluidStack extractFluid(FluidTank tileTank, TileEntityMekanism tile, int slotID, FluidChecker checker) {
        return new LazyOptionalHelper<>(FluidUtil.getFluidHandler(tile.getStackInSlot(slotID))).getIfPresent(handler -> {
            FluidStack ret = extractFluid(tileTank.getCapacity() - tileTank.getFluidAmount(), handler, checker);
            tile.setStackInSlot(slotID, handler.getContainer(), null);
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

    public static int insertFluid(@Nonnull FluidStack fluid, IFluidHandler handler) {
        if (fluid.isEmpty() || handler == null) {
            return 0;
        }
        return handler.fill(fluid, FluidAction.EXECUTE);
    }

    public static void handleContainerItemFill(TileEntityMekanism tileEntity, FluidTank tank, IInventorySlot inSlot, IInventorySlot outSlot) {
        tank.setFluid(handleContainerItemFill(tileEntity, tank.getFluid(), inSlot, outSlot));
    }

    public static FluidStack handleContainerItemFill(TileEntity tileEntity, @Nonnull FluidStack stack, IInventorySlot inSlot, IInventorySlot outSlot) {
        if (!stack.isEmpty()) {
            ItemStack inputCopy = StackUtils.size(inSlot.getStack(), 1);
            LazyOptionalHelper<IFluidHandlerItem> handlerHelper = new LazyOptionalHelper<>(FluidUtil.getFluidHandler(inputCopy));
            int drained = 0;
            if (handlerHelper.isPresent()) {
                IFluidHandlerItem handler = handlerHelper.getValue();
                drained = insertFluid(stack, handler);
                inputCopy = handler.getContainer();
            }
            ItemStack outputStack = outSlot.getStack();
            if (!outputStack.isEmpty() && (!ItemHandlerHelper.canItemStacksStack(outputStack, inputCopy) || outputStack.getCount() == outSlot.getLimit(outputStack))) {
                return stack;
            }
            stack.setAmount(stack.getAmount() - drained);
            if (outputStack.isEmpty()) {
                outSlot.setStack(inputCopy);
            } else if (ItemHandlerHelper.canItemStacksStack(outputStack, inputCopy)) {
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

    public static void handleContainerItemEmpty(TileEntityMekanism tileEntity, FluidTank tank, IInventorySlot inSlot, IInventorySlot outSlot) {
        handleContainerItemEmpty(tileEntity, tank, inSlot, outSlot, null);
    }

    public static void handleContainerItemEmpty(TileEntityMekanism tileEntity, FluidTank tank, IInventorySlot inSlot, IInventorySlot outSlot, FluidChecker checker) {
        tank.setFluid(handleContainerItemEmpty(tileEntity, tank.getFluid(), tank.getCapacity() - tank.getFluidAmount(), inSlot, outSlot, checker));
    }

    public static FluidStack handleContainerItemEmpty(TileEntity tileEntity, @Nonnull FluidStack stored, int needed, IInventorySlot inSlot, IInventorySlot outSlot,
          final FluidChecker checker) {
        final Fluid storedFinal = stored.getFluid();
        final ItemStack input = StackUtils.size(inSlot.getStack(), 1);
        LazyOptionalHelper<IFluidHandlerItem> handlerHelper = new LazyOptionalHelper<>(FluidUtil.getFluidHandler(input));

        if (!handlerHelper.isPresent()) {
            return stored;
        }
        IFluidHandlerItem handler = handlerHelper.getValue();
        FluidStack ret = extractFluid(needed, handler, new FluidChecker() {
            @Override
            public boolean isValid(Fluid f) {
                return (checker == null || checker.isValid(f)) && (storedFinal == Fluids.EMPTY || storedFinal == f);
            }
        });

        ItemStack inputCopy = handler.getContainer();
        ItemStack outputStack = outSlot.getStack();
        if (!FluidUtil.getFluidContained(inputCopy).isPresent() && !inputCopy.isEmpty()) {
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

        if (!FluidUtil.getFluidContained(inputCopy).isPresent() || needed == 0) {
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
          IInventorySlot inSlot, IInventorySlot outSlot, final FluidChecker checker) {
        //TODO: Can these two methods be cleaned up by offloading checks to the IInventorySlots
        if (editMode == ContainerEditMode.FILL || (editMode == ContainerEditMode.BOTH && !FluidUtil.getFluidContained(inSlot.getStack()).isPresent())) {
            return handleContainerItemFill(tileEntity, stack, inSlot, outSlot);
        } else if (editMode == ContainerEditMode.EMPTY || editMode == ContainerEditMode.BOTH) {
            return handleContainerItemEmpty(tileEntity, stack, needed, inSlot, outSlot, checker);
        }
        return stack;
    }

    public enum ContainerEditMode implements IHasTranslationKey {
        BOTH("mekanism.fluidedit.both"),
        FILL("mekanism.fluidedit.fill"),
        EMPTY("mekanism.fluidedit.empty");

        private String display;

        ContainerEditMode(String s) {
            display = s;
        }

        @Override
        public String getTranslationKey() {
            return display;
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