package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandlerWrapper;
import mekanism.api.chemical.IChemicalTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.capabilities.MergedTank;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class HybridInventorySlot extends BasicInventorySlot implements IFluidHandlerSlot {

    public static HybridInventorySlot inputOrDrain(MergedTank mergedTank, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        Predicate<@NonNull ItemStack> gasInsertPredicate = GasInventorySlot.getDrainInsertPredicate(mergedTank.getGasTank(), GasInventorySlot::getCapabilityWrapper);
        //TODO: Merged Tank - Fix predicates
        return new HybridInventorySlot(mergedTank, (stack, automationType) -> {
            //if this is a fluid item, we won't allow extraction- it will go to output slot after being processed
            if (FluidUtil.getFluidHandler(stack).isPresent()) {
                return automationType != AutomationType.EXTERNAL;
            }
            // gas items can be extracted after being filled
            return automationType != AutomationType.EXTERNAL || gasInsertPredicate.negate().test(stack);
        }, (stack, automationType) -> {
            // gas slot or fluid slot insertion check acceptable
            return gasInsertPredicate.test(stack) || FluidInventorySlot.getInputPredicate(mergedTank.getFluidTank()).test(stack);
        }, (stack) -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent() || FluidUtil.getFluidHandler(stack).isPresent(), inventory, x, y);
    }

    public static HybridInventorySlot outputOrFill(MergedTank mergedTank, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(mergedTank, "Merged tank cannot be null");
        //TODO: Merged Tank - Fix predicates
        return new HybridInventorySlot(mergedTank, (stack, automationType) -> {
            if (stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent()) {
                return automationType != AutomationType.EXTERNAL || GasInventorySlot.getFillExtractPredicate(mergedTank.getGasTank(), GasInventorySlot::getCapabilityWrapper).test(stack);
            }
            // always allow extraction if we're a fluid container item
            return true;
        }, (stack, automationType) -> {
            if (stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent()) {
                return GasInventorySlot.fillInsertCheck(mergedTank.getGasTank(), GasInventorySlot.getCapabilityWrapper(stack));
            }
            // if we're not a gas container, we're an output fluid container- only allow internal/manual insertion
            return automationType != AutomationType.EXTERNAL;
            // always validate, as we could have any kind of fluid container in the output slot
        }, alwaysTrue, inventory, x, y);
    }

    private final MergedTank mergedTank;

    // used by IFluidHandlerSlot
    private boolean isDraining;
    private boolean isFilling;

    private HybridInventorySlot(MergedTank mergedTank, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        this.mergedTank = mergedTank;
    }

    public void drainChemicalTank() {
        //TODO: Merged Tank - Go through all the different chemical tanks
        interactChemicalTank(mergedTank.getGasTank(), GasInventorySlot::getCapabilityWrapper, false);
    }

    public void fillChemicalTank() {
        //TODO: Merged Tank - Go through all the different chemical tanks??
        interactChemicalTank(mergedTank.getGasTank(), GasInventorySlot::getCapabilityWrapper, true);
    }

    private <CHEMICAL extends Chemical<CHEMICAL>, STACK extends ChemicalStack<CHEMICAL>, TANK extends IChemicalTank<CHEMICAL, STACK>> void interactChemicalTank(
          TANK tank, Function<ItemStack, IChemicalHandlerWrapper<CHEMICAL, STACK>> wrapperFunction, boolean fill) {
        if (fill) {
            ChemicalInventorySlot.fillChemicalTank(this, tank, wrapperFunction.apply(current));
        } else {
            ChemicalInventorySlot.drainChemicalTank(this, tank, wrapperFunction.apply(current));
        }
    }

    @Override
    public IExtendedFluidTank getFluidTank() {
        return mergedTank.getFluidTank();
    }

    @Override
    public boolean isDraining() {
        return isDraining;
    }

    @Override
    public boolean isFilling() {
        return isFilling;
    }

    @Override
    public void setDraining(boolean draining) {
        isDraining = draining;
    }

    @Override
    public void setFilling(boolean filling) {
        isFilling = filling;
    }
}
