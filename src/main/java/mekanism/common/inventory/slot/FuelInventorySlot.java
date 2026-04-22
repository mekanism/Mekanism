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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.block.entity.FuelValues;
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

    public int burn(FuelValues fuelValues) {
        if (isEmpty()) {
            return 0;
        }
        int burnTime = current.getBurnTime(null, fuelValues) / 2;
        if (burnTime != 0) {
            UseRemainder remainder = current.get(DataComponents.USE_REMAINDER);
            //TODO - 26.1: Should we also validate that the remainder isn't the existing stack?
            if (remainder != null) {
                if (current.count() > 1) {
                    //If we have a container but have more than a single stack of it somehow just exit
                    //TODO - 26.1: Can UseRemainder#convertIntoRemainder be used to allow handling when there is more than a single item in the stack?
                    return 0;
                }
                //If the item has a container, then replace it with the container
                setStack(remainder.convertInto().create());
            } else {
                //Otherwise, shrink the size of the stack by one
                MekanismUtils.logMismatchedStackSize(shrinkStack(1, Action.EXECUTE), 1);
            }
        }
        return burnTime;
    }
}