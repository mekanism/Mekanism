package mekanism.generators.common.slot;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.common.inventory.slot.FluidInventorySlot;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Extension of FluidInventorySlot to make it be able to handle raw items as fuels
 */
@NothingNullByDefault
public class FluidFuelInventorySlot extends FluidInventorySlot {

    public static FluidFuelInventorySlot forFuel(IExtendedFluidTank fluidTank, ToIntFunction<@NotNull ItemStack> fuelValue,
          Int2ObjectFunction<@NotNull FluidStack> fuelCreator, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Objects.requireNonNull(fuelCreator, "Fuel fluid stack creator cannot be null");
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        return new FluidFuelInventorySlot(fluidTank, fuelValue, fuelCreator, stack -> {
            Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
            if (cap.isPresent()) {
                IFluidHandlerItem fluidHandlerItem = cap.get();
                for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                    if (fluidTank.isFluidValid(fluidHandlerItem.getFluidInTank(tank))) {
                        //False if the items contents are still valid
                        return false;
                    }
                }
                //Only allow extraction if our item is out of fluid, but also verify there is no conversion for it
            }
            //Always allow extraction if something went horribly wrong, and we are not a chemical item AND we can't provide a valid type of chemical
            // This might happen after a reload for example
            return fuelValue.applyAsInt(stack) == 0;
        }, stack -> {
            Optional<IFluidHandlerItem> cap = FluidUtil.getFluidHandler(stack).resolve();
            if (cap.isPresent()) {
                IFluidHandlerItem fluidHandlerItem = cap.get();
                for (int tank = 0; tank < fluidHandlerItem.getTanks(); tank++) {
                    FluidStack fluidInTank = fluidHandlerItem.getFluidInTank(tank);
                    if (!fluidInTank.isEmpty() && fluidTank.insert(fluidInTank, Action.SIMULATE, AutomationType.INTERNAL).getAmount() < fluidInTank.getAmount()) {
                        //True if we can fill the tank with any of our contents
                        // Note: We need to recheck the fact the chemical is not empty in case the item has multiple tanks and only some of the chemicals are valid
                        return true;
                    }
                }
            }
            //Note: We recheck about this having a fuel value and that it is still valid as the fuel value might have changed, such as after a reload
            return fuelValue.applyAsInt(stack) > 0;
        }, listener, x, y);
    }

    private final Int2ObjectFunction<@NotNull FluidStack> fuelCreator;
    private final ToIntFunction<@NotNull ItemStack> fuelValue;

    private FluidFuelInventorySlot(IExtendedFluidTank fluidTank, ToIntFunction<@NotNull ItemStack> fuelValue, Int2ObjectFunction<@NotNull FluidStack> fuelCreator,
          Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, @Nullable IContentsListener listener, int x, int y) {
        super(fluidTank, canExtract, canInsert, alwaysTrue, listener, x, y);
        //Note: We pass alwaysTrue as the validator, so that if a mod only exposes a fluid handler on the filled item
        // then we don't have it all of a sudden being invalid after it is emptied
        this.fuelCreator = fuelCreator;
        this.fuelValue = fuelValue;
    }

    /**
     * Fills tank from slot, allowing for the item to also be converted to chemical if need be
     */
    public void fillOrBurn() {
        if (!isEmpty()) {
            int needed = fluidTank.getNeeded();
            //Fill the tank from the item
            if (needed > 0 && !fillTank()) {
                //If filling from item failed, try doing it by conversion
                int fuel = fuelValue.applyAsInt(current);
                if (fuel > 0 && fuel <= needed) {
                    boolean hasContainer = current.hasCraftingRemainingItem();
                    if (hasContainer && current.getCount() > 1) {
                        //If we have a container but have more than a single stack of it somehow just exit
                        return;
                    }
                    fluidTank.insert(fuelCreator.apply(fuel), Action.EXECUTE, AutomationType.INTERNAL);
                    if (hasContainer) {
                        //If the item has a container, then replace it with the container
                        setStack(current.getCraftingRemainingItem());
                    } else {
                        //Otherwise, shrink the size of the stack by one
                        MekanismUtils.logMismatchedStackSize(shrinkStack(1, Action.EXECUTE), 1);
                    }
                }
            }
        }
    }
}