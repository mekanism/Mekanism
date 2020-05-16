package mekanism.common.inventory.slot;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import mekanism.api.annotations.NonNull;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.api.fluid.IExtendedFluidTank;
import mekanism.api.inventory.AutomationType;
import mekanism.api.inventory.IMekanismInventory;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;

public class HybridInventorySlot extends BasicInventorySlot implements IFluidHandlerSlot {

    private IGasTank gasTank;
    private IExtendedFluidTank fluidTank;

    // used by IFluidHandlerSlot
    private boolean isDraining;
    private boolean isFilling;

    private HybridInventorySlot(IGasTank gasTank, IExtendedFluidTank fluidTank, BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canExtract,
          BiPredicate<@NonNull ItemStack, @NonNull AutomationType> canInsert, Predicate<@NonNull ItemStack> validator, @Nullable IMekanismInventory inventory, int x, int y) {
        super(canExtract, canInsert, validator, inventory, x, y);
        this.gasTank = gasTank;
        this.fluidTank = fluidTank;
    }

    public static HybridInventorySlot inputOrDrain(IGasTank gasTank, IExtendedFluidTank fluidTank, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        Predicate<@NonNull ItemStack> gasInsertPredicate = GasInventorySlot.getDrainInsertPredicate(gasTank, GasInventorySlot::getCapabilityWrapper);
        return new HybridInventorySlot(gasTank, fluidTank,
              (stack, automationType) -> {
                  //if this is a fluid item, we won't allow extraction- it will go to output slot after being processed
                  if (FluidUtil.getFluidHandler(stack).isPresent()) {
                      return automationType != AutomationType.EXTERNAL;
                  } else {
                      // gas items can be extracted after being filled
                      return automationType != AutomationType.EXTERNAL || gasInsertPredicate.negate().test(stack);
                  }
              },
              (stack, automationType) -> {
                  // gas slot or fluid slot insertion check acceptable
                  return gasInsertPredicate.test(stack) ||
                         FluidInventorySlot.getInputPredicate(fluidTank).test(stack);
              },
              (stack) -> stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent() || FluidUtil.getFluidHandler(stack).isPresent(), inventory, x, y);
    }


    public static HybridInventorySlot outputOrFill(IGasTank gasTank, IExtendedFluidTank fluidTank, @Nullable IMekanismInventory inventory, int x, int y) {
        Objects.requireNonNull(gasTank, "Gas tank cannot be null");
        Objects.requireNonNull(fluidTank, "Fluid tank cannot be null");
        return new HybridInventorySlot(gasTank, fluidTank,
              (stack, automationType) -> {
                  if (stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent()) {
                      return automationType != AutomationType.EXTERNAL || GasInventorySlot.getFillExtractPredicate(gasTank, GasInventorySlot::getCapabilityWrapper).test(stack);
                  }
                  // always allow extraction if we're a fluid container item
                  return true;
              },
              (stack, automationType) -> {
                  if (stack.getCapability(Capabilities.GAS_HANDLER_CAPABILITY).isPresent()) {
                      return GasInventorySlot.fillInsertCheck(gasTank, GasInventorySlot.getCapabilityWrapper(stack));
                  }
                  // if we're not a gas container, we're an output fluid container- only allow internal/manual insertion
                  return automationType != AutomationType.EXTERNAL;
                  // always validate, as we could have any kind of fluid container in the output slot
              }, alwaysTrue, inventory, x, y);
    }

    public void drainGasTank() {
        ChemicalInventorySlot.drainChemicalTank(this, gasTank, GasInventorySlot.getCapabilityWrapper(current));
    }

    public void fillGasTank() {
        ChemicalInventorySlot.fillChemicalTank(this, gasTank, GasInventorySlot.getCapabilityWrapper(current));
    }

    @Override
    public IExtendedFluidTank getFluidTank() {
        return fluidTank;
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
