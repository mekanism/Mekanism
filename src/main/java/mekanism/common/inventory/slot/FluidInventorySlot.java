package mekanism.common.inventory.slot;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import mekanism.api.annotations.NonNull;
import mekanism.common.util.FluidContainerUtils;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class FluidInventorySlot extends BasicInventorySlot {

    private static final Predicate<@NonNull ItemStack> requireEmpty = item -> !FluidUtil.getFluidContained(item).isPresent();
    private static final Predicate<@NonNull ItemStack> validator = FluidContainerUtils::isFluidContainer;

    @Nonnull
    private final IFluidHandler fluidHandler;

    //TODO: Use the passed in tank to validate
    private FluidInventorySlot(@Nonnull IFluidHandler fluidHandler, Predicate<@NonNull ItemStack> canExtract, Predicate<@NonNull ItemStack> canInsert) {
        super(canExtract, canInsert, validator);
        this.fluidHandler = fluidHandler;
    }

    public static FluidInventorySlot output(@Nonnull IFluidHandler fluidHandler) {
        //TODO: Replace the requireEmpty to make it so that if the item's container is not full but contains the same type then it can accept it
        return new FluidInventorySlot(fluidHandler, item -> true, requireEmpty);
    }

    //TODO: Make it so that the fluid handler fills
}