package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.IContentsListener;
import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.functions.ConstantPredicates;
import mekanism.common.util.MekanismUtils;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class FuelInventorySlot extends BasicInventorySlot {

    public static FuelInventorySlot forFuel(ToIntFunction<@NotNull ItemStack> fuelValue, @Nullable IContentsListener listener, int x, int y) {
        Objects.requireNonNull(fuelValue, "Fuel value calculator cannot be null");
        return new FuelInventorySlot(stack -> fuelValue.applyAsInt(stack) == 0, stack -> fuelValue.applyAsInt(stack) != 0,
              ConstantPredicates.alwaysTrue(), listener, x, y);
    }

    private FuelInventorySlot(Predicate<@NotNull ItemStack> canExtract, Predicate<@NotNull ItemStack> canInsert, Predicate<@NotNull ItemStack> validator,
          @Nullable IContentsListener listener, int x, int y) {
        super((stack, automationType) -> automationType == AutomationType.MANUAL || canExtract.test(stack), (stack, automationType) -> canInsert.test(stack), validator,
              listener, x, y);
    }

    public int burn() {
        if (isEmpty()) {
            return 0;
        }
        int burnTime = current.getBurnTime(null) / 2;
        if (burnTime != 0) {
            if (current.hasCraftingRemainingItem()) {
                if (current.getCount() > 1) {
                    //If we have a container but have more than a single stack of it somehow just exit
                    return 0;
                }
                //If the item has a container, then replace it with the container
                setStack(current.getCraftingRemainingItem());
            } else {
                //Otherwise, shrink the size of the stack by one
                MekanismUtils.logMismatchedStackSize(shrinkStack(1, Action.EXECUTE), 1);
            }
        }
        return burnTime;
    }
}